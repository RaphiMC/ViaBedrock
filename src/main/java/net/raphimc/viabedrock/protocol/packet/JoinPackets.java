/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viabedrock.protocol.packet;

import com.google.common.base.Joiner;
import com.vdurmont.semver4j.Semver;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntImmutablePair;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.BitSets;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.platform.ViaBedrockConfig;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.Dimension;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.*;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEventType;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerInfoUpdateAction;
import net.raphimc.viabedrock.protocol.model.*;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.GameTypeRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.*;
import java.util.logging.Level;

public class JoinPackets {

    private static final PacketHandler BIOME_DEFINITION_LIST_HANDLER = wrapper -> {
        if (wrapper.isCancelled()) return;

        wrapper.user().get(GameSessionStorage.class).setBedrockBiomeDefinitions((CompoundTag) wrapper.read(BedrockTypes.NETWORK_TAG)); // biome definitions
    };

    private static final PacketHandler COMPRESSED_BIOME_DEFINITION_LIST_HANDLER = wrapper -> {
        if (wrapper.isCancelled()) return;

        // Compressed biome definitions are used for the clientside generation of the world. Should not be sent as we tell the server that the client doesn't support it.
        BedrockProtocol.kickForIllegalState(wrapper.user(), "Compressed biome definitions are not supported.");
    };

    private static final PacketHandler DIMENSION_DATA_HANDLER = wrapper -> {
        if (wrapper.isCancelled()) return;

        final int count = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // entry count
        for (int i = 0; i < count; i++) {
            final String dimensionIdentifier = wrapper.read(BedrockTypes.STRING); // dimension identifier
            final int maximumHeight = wrapper.read(BedrockTypes.VAR_INT); // maximum height
            final int minimumHeight = wrapper.read(BedrockTypes.VAR_INT); // minimum height
            wrapper.read(BedrockTypes.VAR_INT); // generator type

            if (!dimensionIdentifier.equals("minecraft:overworld")) {
                continue; // Mojang client currently only supports overworld
            }
            wrapper.user().get(GameSessionStorage.class).putBedrockDimensionDefinition(dimensionIdentifier, new IntIntImmutablePair(minimumHeight, maximumHeight));
        }
    };

    private static final PacketHandler REQUIRE_UNINITIALIZED_WORLD_HANDLER = wrapper -> {
        if (!wrapper.user().get(ChunkTracker.class).isEmpty()) {
            wrapper.cancel();
        } else if (!wrapper.user().get(EntityTracker.class).isEmpty()) {
            wrapper.cancel();
        }

        if (wrapper.isCancelled()) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to change world properties after the world was already loaded");
        }
    };

    private static final PacketHandler RECONFIGURE_HANDLER = wrapper -> {
        if (wrapper.isCancelled()) return;
        wrapper.cancel();

        wrapper.user().put(new ChunkTracker(wrapper.user(), wrapper.user().get(ChunkTracker.class).getDimension()));
        if (wrapper.user().getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_20_2)) {
            final PacketWrapper startConfiguration = PacketWrapper.create(ClientboundPackets1_21.START_CONFIGURATION, wrapper.user());
            startConfiguration.send(BedrockProtocol.class);
            wrapper.user().getProtocolInfo().setServerState(State.CONFIGURATION);

            handleGameJoin(wrapper.user());
        } else {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Skipping reconfigure packet as it is not supported by the client. This may cause issues.");
        }
    };

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientboundTransition(ClientboundBedrockPackets.PLAY_STATUS,
                State.LOGIN, (PacketHandler) wrapper -> {
                    final int rawStatus = wrapper.read(Types.INT); // status
                    final PlayStatus status = PlayStatus.getByValue(rawStatus);
                    if (status == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown PlayStatus: " + rawStatus);
                        wrapper.cancel();
                        return;
                    }

                    if (status == PlayStatus.LoginSuccess) {
                        wrapper.setPacketType(ClientboundLoginPackets.GAME_PROFILE);
                        final AuthChainData authChainData = wrapper.user().get(AuthChainData.class);
                        wrapper.write(Types.UUID, authChainData.getIdentity()); // uuid
                        wrapper.write(Types.STRING, authChainData.getDisplayName()); // username
                        wrapper.write(Types.VAR_INT, 0); // properties length
                        wrapper.write(Types.BOOLEAN, true); // strict error handling

                        final ProtocolInfo info = wrapper.user().getProtocolInfo();
                        info.setUsername(authChainData.getDisplayName());
                        info.setUuid(authChainData.getIdentity());

                        // Parts of BaseProtocol1_7 GAME_PROFILE handler
                        Via.getManager().getConnectionManager().onLoginSuccess(wrapper.user());
                        if (!info.getPipeline().hasNonBaseProtocols()) {
                            wrapper.user().setActive(false);
                        }
                        if (Via.getManager().isDebug()) {
                            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "{0} logged in with protocol {1}, Route: {2}", new Object[]{info.getUsername(), info.protocolVersion().getName(), Joiner.on(", ").join(info.getPipeline().pipes(), ", ")});
                        }

                        sendClientCacheStatus(wrapper.user());
                    } else {
                        wrapper.setPacketType(ClientboundLoginPackets.LOGIN_DISCONNECT);
                        writePlayStatusKickMessage(wrapper, status);
                    }
                }, State.PLAY, (PacketHandler) wrapper -> {
                    final int rawStatus = wrapper.read(Types.INT); // status
                    final PlayStatus status = PlayStatus.getByValue(rawStatus);
                    if (status == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown PlayStatus: " + rawStatus);
                        wrapper.cancel();
                        return;
                    }

                    if (status == PlayStatus.LoginSuccess) {
                        wrapper.cancel();
                        sendClientCacheStatus(wrapper.user());
                    } else if (status == PlayStatus.PlayerSpawn) {
                        wrapper.cancel();
                        final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                        final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
                        if (clientPlayer.isInitiallySpawned()) return;

                        final PacketWrapper interact = PacketWrapper.create(ServerboundBedrockPackets.INTERACT, wrapper.user());
                        interact.write(Types.BYTE, (byte) InteractPacket_Action.InteractUpdate.getValue()); // action
                        interact.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // runtime entity id
                        interact.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // mouse position
                        interact.sendToServer(BedrockProtocol.class);

                        // TODO: Mob Equipment with current held item

                        final PacketWrapper emoteList = PacketWrapper.create(ServerboundBedrockPackets.EMOTE_LIST, wrapper.user());
                        emoteList.write(BedrockTypes.VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                        emoteList.write(BedrockTypes.UUID_ARRAY, new UUID[0]); // emote ids
                        emoteList.sendToServer(BedrockProtocol.class);

                        clientPlayer.setRotation(new Position3f(clientPlayer.rotation().x(), clientPlayer.rotation().y(), clientPlayer.rotation().y()));
                        clientPlayer.setInitiallySpawned();
                        if (gameSession.getMovementMode() == ServerAuthMovementMode.ClientAuthoritative) {
                            clientPlayer.sendMovePlayerPacketToServer(PlayerPositionModeComponent_PositionMode.Normal);
                        } else {
                            clientPlayer.sendAuthInputPacketToServer(ClientPlayMode.Normal);
                        }

                        final PacketWrapper setLocalPlayerAsInitialized = PacketWrapper.create(ServerboundBedrockPackets.SET_LOCAL_PLAYER_AS_INITIALIZED, wrapper.user());
                        setLocalPlayerAsInitialized.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                        setLocalPlayerAsInitialized.sendToServer(BedrockProtocol.class);

                        PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.LEVEL_CHUNKS_LOAD_START, 0F);
                    } else {
                        wrapper.setPacketType(ClientboundPackets1_21.DISCONNECT);
                        writePlayStatusKickMessage(wrapper, status);
                    }
                }, State.CONFIGURATION, (PacketHandler) wrapper -> {
                    final int rawStatus = wrapper.read(Types.INT); // status
                    final PlayStatus status = PlayStatus.getByValue(rawStatus);
                    if (status == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown PlayStatus: " + rawStatus);
                        wrapper.cancel();
                        return;
                    }

                    if (status == PlayStatus.LoginSuccess) {
                        wrapper.cancel();
                        sendClientCacheStatus(wrapper.user());
                    } else {
                        wrapper.setPacketType(ClientboundConfigurationPackets1_21.DISCONNECT);
                        writePlayStatusKickMessage(wrapper, status);
                    }
                }
        );
        protocol.registerClientboundTransition(ClientboundBedrockPackets.START_GAME,
                State.CONFIGURATION, (PacketHandler) wrapper -> {
                    wrapper.cancel();
                    ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

                    if (resourcePacksStorage == null || !resourcePacksStorage.hasFinishedLoading()) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Pack negotiation not completed before joining game. Skipping resource pack loading");
                        resourcePacksStorage = new ResourcePacksStorage();
                        resourcePacksStorage.setCompletedTransfer();
                        resourcePacksStorage.setPackStack(new UUID[0], new UUID[0]);
                        wrapper.user().put(resourcePacksStorage);
                    }

                    final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
                    final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
                    final int playerGameType = wrapper.read(BedrockTypes.VAR_INT); // player game type
                    final Position3f playerPosition = wrapper.read(BedrockTypes.POSITION_3F); // player position
                    final Position2f playerRotation = wrapper.read(BedrockTypes.POSITION_2F); // player rotation

                    // Level settings
                    wrapper.read(BedrockTypes.LONG_LE); // seed
                    wrapper.read(BedrockTypes.SHORT_LE); // spawn biome type
                    wrapper.read(BedrockTypes.STRING); // custom biome name
                    final Dimension dimension = Dimension.values()[wrapper.read(BedrockTypes.VAR_INT)]; // dimension
                    final GeneratorType generatorType = GeneratorType.getByValue(wrapper.read(BedrockTypes.VAR_INT), GeneratorType.Undefined); // generator id
                    final int levelGameType = wrapper.read(BedrockTypes.VAR_INT); // level game type
                    final boolean hardcore = wrapper.read(Types.BOOLEAN); // hardcore
                    final Difficulty difficulty = Difficulty.getByValue(wrapper.read(BedrockTypes.VAR_INT), Difficulty.Unknown); // difficulty
                    wrapper.read(BedrockTypes.BLOCK_POSITION); // default spawn position
                    wrapper.read(Types.BOOLEAN); // achievements disabled
                    final Editor_WorldType editorWorldType = Editor_WorldType.getByValue(wrapper.read(BedrockTypes.VAR_INT)); // world editor type
                    wrapper.read(Types.BOOLEAN); // created in world editor
                    wrapper.read(Types.BOOLEAN); // exported from world editor
                    final int currentTime = wrapper.read(BedrockTypes.VAR_INT); // day cycle stop time
                    wrapper.read(BedrockTypes.VAR_INT); // education edition offers
                    wrapper.read(Types.BOOLEAN); // education features enabled
                    wrapper.read(BedrockTypes.STRING); // education product id
                    final float rainLevel = wrapper.read(BedrockTypes.FLOAT_LE); // rain level
                    final float lightningLevel = wrapper.read(BedrockTypes.FLOAT_LE); // lightning level
                    wrapper.read(Types.BOOLEAN); // platform locked content confirmed
                    wrapper.read(Types.BOOLEAN); // multiplayer game
                    wrapper.read(Types.BOOLEAN); // is broadcasting to lan
                    wrapper.read(BedrockTypes.VAR_INT); // Xbox Live broadcast mode
                    wrapper.read(BedrockTypes.VAR_INT); // platform broadcast mode
                    final boolean commandsEnabled = wrapper.read(Types.BOOLEAN); // commands enabled
                    wrapper.read(Types.BOOLEAN); // texture packs required
                    final GameRule[] gameRules = wrapper.read(BedrockTypes.GAME_RULE_ARRAY); // game rules
                    final Experiment[] experiments = wrapper.read(BedrockTypes.EXPERIMENT_ARRAY); // experiments
                    wrapper.read(Types.BOOLEAN); // experiments previously toggled
                    wrapper.read(Types.BOOLEAN); // bonus chest enabled
                    wrapper.read(Types.BOOLEAN); // start with map enabled
                    final int playerPermission = wrapper.read(BedrockTypes.VAR_INT); // player permission
                    wrapper.read(BedrockTypes.INT_LE); // server chunk tick range
                    wrapper.read(Types.BOOLEAN); // behavior pack locked
                    wrapper.read(Types.BOOLEAN); // resource pack locked
                    wrapper.read(Types.BOOLEAN); // from locked world template
                    wrapper.read(Types.BOOLEAN); // using msa gamer tags only
                    wrapper.read(Types.BOOLEAN); // from world template
                    wrapper.read(Types.BOOLEAN); // world template option locked
                    wrapper.read(Types.BOOLEAN); // only spawn v1 villagers
                    wrapper.read(Types.BOOLEAN); // disable personas
                    wrapper.read(Types.BOOLEAN); // disable custom skins
                    wrapper.read(Types.BOOLEAN); // mute emote chat
                    final String vanillaVersion = wrapper.read(BedrockTypes.STRING); // vanilla version
                    wrapper.read(BedrockTypes.INT_LE); // limited world width
                    wrapper.read(BedrockTypes.INT_LE); // limited world height
                    wrapper.read(Types.BOOLEAN); // nether type
                    wrapper.read(BedrockTypes.EDUCATION_URI_RESOURCE); // education shared uri
                    wrapper.read(Types.BOOLEAN); // enable experimental game play
                    final ChatRestrictionLevel chatRestrictionLevel = ChatRestrictionLevel.getByValue(wrapper.read(Types.BYTE), ChatRestrictionLevel.Disabled); // chat restriction level
                    wrapper.read(Types.BOOLEAN); // disabling player interactions
                    wrapper.read(BedrockTypes.STRING); // server id
                    wrapper.read(BedrockTypes.STRING); // world id
                    wrapper.read(BedrockTypes.STRING); // scenario id

                    // Continue reading start game packet
                    wrapper.read(BedrockTypes.STRING); // level id
                    final String levelName = wrapper.read(BedrockTypes.STRING); // level name
                    wrapper.read(BedrockTypes.STRING); // premium world template id
                    wrapper.read(Types.BOOLEAN); // is trial
                    final ServerAuthMovementMode movementMode = ServerAuthMovementMode.getByValue(wrapper.read(BedrockTypes.VAR_INT) & 255, ServerAuthMovementMode.ServerAuthoritativeWithRewind); // movement mode
                    wrapper.read(BedrockTypes.VAR_INT); // rewind history size
                    wrapper.read(Types.BOOLEAN); // server authoritative block breaking
                    final long levelTime = wrapper.read(BedrockTypes.LONG_LE); // current level time
                    wrapper.read(BedrockTypes.VAR_INT); // enchantment seed
                    final BlockProperties[] blockProperties = wrapper.read(BedrockTypes.BLOCK_PROPERTIES_ARRAY); // block properties
                    final ItemEntry[] itemEntries = wrapper.read(BedrockTypes.ITEM_ENTRY_ARRAY); // item entries
                    wrapper.read(BedrockTypes.STRING); // multiplayer correlation id
                    wrapper.read(Types.BOOLEAN); // server authoritative inventories
                    final String serverEngine = wrapper.read(BedrockTypes.STRING); // server engine
                    wrapper.read(BedrockTypes.NETWORK_TAG); // player property data
                    wrapper.read(BedrockTypes.LONG_LE); // block registry checksum
                    wrapper.read(BedrockTypes.UUID); // world template id
                    wrapper.read(Types.BOOLEAN); // client side generation
                    final boolean hashedRuntimeBlockIds = wrapper.read(Types.BOOLEAN); // use hashed block runtime ids
                    wrapper.read(Types.BOOLEAN); // server authoritative sounds

                    if (editorWorldType == Editor_WorldType.EditorProject) {
                        final PacketWrapper disconnect = PacketWrapper.create(ClientboundConfigurationPackets1_21.DISCONNECT, wrapper.user());
                        PacketFactory.writeJavaDisconnect(wrapper, resourcePacksStorage.getTranslations().get("disconnectionScreen.editor.mismatchEditorWorld"));
                        disconnect.send(BedrockProtocol.class);
                        return;
                    }

                    ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Server feature version: " + vanillaVersion);
                    Semver version;
                    try {
                        if (vanillaVersion.equals("*")) {
                            version = new Semver("99.99.99");
                        } else {
                            version = new Semver(vanillaVersion, Semver.SemverType.LOOSE);
                        }
                    } catch (Throwable e) {
                        ViaBedrock.getPlatform().getLogger().log(Level.SEVERE, "Invalid vanilla version: " + vanillaVersion);
                        version = new Semver("99.99.99");
                    }

                    final List<String> enabledFeatures = new ArrayList<>();
                    for (Experiment experiment : experiments) {
                        if (experiment.enabled()) {
                            if (BedrockProtocol.MAPPINGS.getBedrockToJavaExperimentalFeatures().containsKey(experiment.name())) {
                                enabledFeatures.add(BedrockProtocol.MAPPINGS.getBedrockToJavaExperimentalFeatures().get(experiment.name()));
                            } else {
                                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "This server uses an unsupported experimental feature: " + experiment.name());
                            }
                        }
                    }

                    if (movementMode == ServerAuthMovementMode.ServerAuthoritativeWithRewind) {
                        ViaBedrock.getPlatform().getLogger().log(Level.SEVERE, "This server uses server authoritative movement with rewind. This is not supported.");
                    } else if (movementMode == ServerAuthMovementMode.ServerAuthoritative) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "This server uses server authoritative movement. This is not stable yet.");
                    }

                    gameSession.setBedrockVanillaVersion(version);
                    gameSession.setFlatGenerator(generatorType == GeneratorType.Flat);
                    gameSession.setMovementMode(movementMode);
                    gameSession.setLevelGameType(levelGameType);
                    gameSession.setLevelTime(levelTime);
                    gameSession.setHardcoreMode(hardcore);
                    gameSession.setChatRestrictionLevel(chatRestrictionLevel);
                    gameSession.setCommandsEnabled(commandsEnabled);

                    final PlayerAbilities playerAbilities = new PlayerAbilities(uniqueEntityId, (byte) playerPermission, (byte) CommandPermissionLevel.Any.getValue(), new HashMap<>());
                    final ClientPlayerEntity clientPlayer = new ClientPlayerEntity(wrapper.user(), runtimeEntityId, wrapper.user().getProtocolInfo().getUuid(), playerAbilities);
                    clientPlayer.setPosition(new Position3f(playerPosition.x(), playerPosition.y() + clientPlayer.eyeOffset(), playerPosition.z()));
                    clientPlayer.setRotation(new Position3f(playerRotation.x(), playerRotation.y(), 0F));
                    clientPlayer.setOnGround(false);
                    clientPlayer.setGameType(playerGameType);
                    clientPlayer.setName(wrapper.user().getProtocolInfo().getUsername());

                    wrapper.user().put(new JoinGameStorage(levelName, difficulty, rainLevel, lightningLevel, currentTime));
                    wrapper.user().put(new GameRulesStorage(wrapper.user(), gameRules));
                    wrapper.user().put(new BlockStateRewriter(blockProperties, hashedRuntimeBlockIds));
                    wrapper.user().put(new ItemRewriter(wrapper.user(), itemEntries));
                    wrapper.user().put(new ChunkTracker(wrapper.user(), dimension));
                    final EntityTracker entityTracker = new EntityTracker(wrapper.user());
                    entityTracker.addEntity(clientPlayer, false);
                    wrapper.user().put(entityTracker);

                    final PacketWrapper brandCustomPayload = PacketWrapper.create(ClientboundConfigurationPackets1_21.CUSTOM_PAYLOAD, wrapper.user());
                    brandCustomPayload.write(Types.STRING, "minecraft:brand"); // channel
                    brandCustomPayload.write(Types.STRING, "Bedrock" + (!serverEngine.isEmpty() ? " @" + serverEngine : "") + " v: " + vanillaVersion); // content
                    brandCustomPayload.send(BedrockProtocol.class);

                    if (!enabledFeatures.isEmpty()) {
                        enabledFeatures.add("minecraft:vanilla");
                        final PacketWrapper updateEnabledFeatures = PacketWrapper.create(ClientboundConfigurationPackets1_21.UPDATE_ENABLED_FEATURES, wrapper.user());
                        updateEnabledFeatures.write(Types.STRING_ARRAY, enabledFeatures.toArray(new String[0])); // enabled features
                        updateEnabledFeatures.send(BedrockProtocol.class);
                    }

                    handleGameJoin(wrapper.user());
                    sendStartGameResponsePackets(wrapper.user());
                }, State.LOGIN, (PacketHandler) wrapper -> {
                    wrapper.cancel();
                    sendStartGameResponsePackets(wrapper.user());
                }, State.PLAY, (PacketHandler) PacketWrapper::cancel // Mojang client ignores multiple start game packets
        );
        protocol.registerClientboundTransition(ClientboundBedrockPackets.BIOME_DEFINITION_LIST,
                // Biomes are technically data driven, but the client seems to ignore most of the defined data and instead uses hardcoded values.
                State.CONFIGURATION, new PacketHandlers() {
                    @Override
                    protected void register() {
                        handler(PacketWrapper::cancel);
                    }
                }, State.PLAY, new PacketHandlers() {
                    @Override
                    protected void register() {
                        handler(REQUIRE_UNINITIALIZED_WORLD_HANDLER);
                        handler(PacketWrapper::cancel);
                    }
                }
        );
        protocol.registerClientboundTransition(ClientboundBedrockPackets.COMPRESSED_BIOME_DEFINITION_LIST,
                State.CONFIGURATION, new PacketHandlers() {
                    @Override
                    protected void register() {
                        handler(PacketWrapper::cancel);
                    }
                }, State.PLAY, new PacketHandlers() {
                    @Override
                    protected void register() {
                        handler(REQUIRE_UNINITIALIZED_WORLD_HANDLER);
                        handler(PacketWrapper::cancel);
                    }
                }
        );
        protocol.registerClientboundTransition(ClientboundBedrockPackets.DIMENSION_DATA,
                State.CONFIGURATION, new PacketHandlers() {
                    @Override
                    protected void register() {
                        handler(DIMENSION_DATA_HANDLER);
                        handler(PacketWrapper::cancel);
                    }
                }, State.PLAY, new PacketHandlers() {
                    @Override
                    protected void register() {
                        handler(REQUIRE_UNINITIALIZED_WORLD_HANDLER);
                        handler(DIMENSION_DATA_HANDLER);
                        handler(RECONFIGURE_HANDLER);
                    }
                }
        );
    }

    private static void sendClientCacheStatus(final UserConnection user) {
        final PacketWrapper clientCacheStatus = PacketWrapper.create(ServerboundBedrockPackets.CLIENT_CACHE_STATUS, user);
        clientCacheStatus.write(Types.BOOLEAN, !ViaBedrock.getConfig().getBlobCacheMode().equals(ViaBedrockConfig.BlobCacheMode.DISABLED)); // is supported
        clientCacheStatus.sendToServer(BedrockProtocol.class);
    }

    private static void writePlayStatusKickMessage(final PacketWrapper wrapper, final PlayStatus status) {
        final Map<String, String> translations = BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePack().content().getLang("texts/en_US.lang");

        switch (status) {
            case LoginFailed_ClientOld:
                PacketFactory.writeJavaDisconnect(wrapper, translations.get("disconnectionScreen.outdatedClient"));
                break;
            case LoginFailed_ServerOld:
                PacketFactory.writeJavaDisconnect(wrapper, translations.get("disconnectionScreen.outdatedServer"));
                break;
            case LoginFailed_InvalidTenant:
                PacketFactory.writeJavaDisconnect(wrapper, translations.get("disconnectionScreen.invalidTenant"));
                break;
            case LoginFailed_EditionMismatchEduToVanilla:
                PacketFactory.writeJavaDisconnect(wrapper, translations.get("disconnectionScreen.editionMismatchEduToVanilla"));
                break;
            case LoginFailed_EditionMismatchVanillaToEdu:
                PacketFactory.writeJavaDisconnect(wrapper, translations.get("disconnectionScreen.editionMismatchVanillaToEdu"));
                break;
            case LoginFailed_ServerFullSubClient:
            case LoginFailed_EditorMismatchVanillaToEditor:
                PacketFactory.writeJavaDisconnect(wrapper, translations.get("disconnectionScreen.serverFull") + "\n\n\n\n" + translations.get("disconnectionScreen.serverFull.title"));
                break;
            case LoginFailed_EditorMismatchEditorToVanilla:
                PacketFactory.writeJavaDisconnect(wrapper, translations.get("disconnectionScreen.editor.mismatchEditorToVanilla"));
                break;
            default:
                throw new IllegalStateException("Unhandled PlayStatus: " + status);
            case PlayerSpawn:
            case LoginSuccess:
                wrapper.cancel();
                break;
        }
    }

    private static void handleGameJoin(final UserConnection user) {
        final JoinGameStorage joinGameStorage = user.get(JoinGameStorage.class);
        final GameSessionStorage gameSession = user.get(GameSessionStorage.class);
        final ClientSettingsStorage clientSettingsStorage = user.get(ClientSettingsStorage.class);
        final GameRulesStorage gameRulesStorage = user.get(GameRulesStorage.class);
        final ChunkTracker chunkTracker = user.get(ChunkTracker.class);
        final ClientPlayerEntity clientPlayer = user.get(EntityTracker.class).getClientPlayer();

        for (Map.Entry<String, Tag> registry : gameSession.getJavaRegistries().entrySet()) {
            final CompoundTag registryTag = (CompoundTag) registry.getValue();
            final PacketWrapper registryData = PacketWrapper.create(ClientboundConfigurationPackets1_21.REGISTRY_DATA, user);
            registryData.write(Types.STRING, registry.getKey()); // registry key
            final List<RegistryEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Tag> entry : registryTag.entrySet()) {
                entries.add(new RegistryEntry(entry.getKey(), entry.getValue()));
            }
            registryData.write(Types.REGISTRY_ENTRY_ARRAY, entries.toArray(new RegistryEntry[0])); // registry entries
            registryData.send(BedrockProtocol.class);
        }

        final PacketWrapper updateTags = PacketWrapper.create(ClientboundConfigurationPackets1_21.UPDATE_TAGS, user);
        updateTags.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getJavaTags().size()); // number of registries
        for (Map.Entry<String, Tag> registryEntry : BedrockProtocol.MAPPINGS.getJavaTags().entrySet()) {
            final CompoundTag tag = (CompoundTag) registryEntry.getValue();
            updateTags.write(Types.STRING, registryEntry.getKey()); // registry key
            updateTags.write(Types.VAR_INT, tag.size()); // number of tags
            for (Map.Entry<String, Tag> tagEntry : tag.entrySet()) {
                updateTags.write(Types.STRING, tagEntry.getKey()); // tag name
                updateTags.write(Types.VAR_INT_ARRAY_PRIMITIVE, ((IntArrayTag) tagEntry.getValue()).getValue()); // tag ids
            }
        }
        updateTags.send(BedrockProtocol.class);

        final PacketWrapper finishConfiguration = PacketWrapper.create(ClientboundConfigurationPackets1_21.FINISH_CONFIGURATION, user);
        finishConfiguration.send(BedrockProtocol.class);
        user.getProtocolInfo().setServerState(State.PLAY);

        final PacketWrapper joinGame = PacketWrapper.create(ClientboundPackets1_21.LOGIN, user);
        joinGame.write(Types.INT, clientPlayer.javaId()); // entity id
        joinGame.write(Types.BOOLEAN, gameSession.isHardcoreMode()); // hardcore
        joinGame.write(Types.STRING_ARRAY, Dimension.getDimensionKeys()); // dimension types
        joinGame.write(Types.VAR_INT, 100); // max players
        joinGame.write(Types.VAR_INT, clientSettingsStorage.viewDistance()); // view distance
        joinGame.write(Types.VAR_INT, clientSettingsStorage.viewDistance()); // simulation distance
        joinGame.write(Types.BOOLEAN, ViaBedrock.getConfig().shouldTranslateShowCoordinatesGameRule() && !gameRulesStorage.<Boolean>getGameRule("showCoordinates")); // reduced debug info
        joinGame.write(Types.BOOLEAN, !gameRulesStorage.<Boolean>getGameRule("doImmediateRespawn")); // show death screen
        joinGame.write(Types.BOOLEAN, gameRulesStorage.getGameRule("doLimitedCrafting")); // limited crafting
        joinGame.write(Types.VAR_INT, chunkTracker.getDimension().ordinal()); // dimension id
        joinGame.write(Types.STRING, chunkTracker.getDimension().getKey()); // dimension name
        joinGame.write(Types.LONG, 0L); // hashed seed
        joinGame.write(Types.BYTE, GameTypeRewriter.getEffectiveGameMode(clientPlayer.gameType(), gameSession.getLevelGameType())); // game mode
        joinGame.write(Types.BYTE, (byte) -1); // previous game mode
        joinGame.write(Types.BOOLEAN, false); // is debug
        joinGame.write(Types.BOOLEAN, gameSession.isFlatGenerator()); // is flat
        joinGame.write(Types.OPTIONAL_GLOBAL_POSITION, null); // last death location
        joinGame.write(Types.VAR_INT, 0); // portal cooldown
        joinGame.write(Types.BOOLEAN, false); // enforce secure chat
        joinGame.send(BedrockProtocol.class);

        clientPlayer.createTeam();
        clientPlayer.onAbilitiesChanged();
        clientPlayer.sendPlayerPositionPacketToClient(false);

        final PacketWrapper serverDifficulty = PacketWrapper.create(ClientboundPackets1_21.CHANGE_DIFFICULTY, user);
        serverDifficulty.write(Types.UNSIGNED_BYTE, (short) joinGameStorage.difficulty().getValue()); // difficulty
        serverDifficulty.write(Types.BOOLEAN, false); // locked
        serverDifficulty.send(BedrockProtocol.class);

        final PacketWrapper tabList = PacketWrapper.create(ClientboundPackets1_21.TAB_LIST, user);
        tabList.write(Types.TAG, TextUtil.stringToNbt(joinGameStorage.levelName() + "\n")); // header
        tabList.write(Types.TAG, TextUtil.stringToNbt("§aViaBedrock §3v" + ViaBedrock.VERSION + "\n§7https://github.com/RaphiMC/ViaBedrock")); // footer
        tabList.send(BedrockProtocol.class);

        final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_21.PLAYER_INFO_UPDATE, user);
        playerInfoUpdate.write(Types.PROFILE_ACTIONS_ENUM, BitSets.create(6, PlayerInfoUpdateAction.ADD_PLAYER.ordinal(), PlayerInfoUpdateAction.UPDATE_GAME_MODE.ordinal())); // actions
        playerInfoUpdate.write(Types.VAR_INT, 1); // length
        playerInfoUpdate.write(Types.UUID, clientPlayer.javaUuid()); // uuid
        playerInfoUpdate.write(Types.STRING, StringUtil.encodeUUID(clientPlayer.javaUuid())); // username
        playerInfoUpdate.write(Types.VAR_INT, 0); // property count
        playerInfoUpdate.write(Types.VAR_INT, (int) GameTypeRewriter.getEffectiveGameMode(clientPlayer.gameType(), gameSession.getLevelGameType())); // game mode
        playerInfoUpdate.send(BedrockProtocol.class);

        if (joinGameStorage.rainLevel() > 0F || joinGameStorage.lightningLevel() > 0F) {
            final PacketWrapper rainStartGameEvent = PacketWrapper.create(ClientboundPackets1_21.GAME_EVENT, user);
            rainStartGameEvent.write(Types.UNSIGNED_BYTE, (short) GameEventType.START_RAINING.ordinal()); // event id
            rainStartGameEvent.write(Types.FLOAT, 0F); // value
            rainStartGameEvent.send(BedrockProtocol.class);

            if (joinGameStorage.rainLevel() > 0F) {
                final PacketWrapper rainStrengthGameEvent = PacketWrapper.create(ClientboundPackets1_21.GAME_EVENT, user);
                rainStrengthGameEvent.write(Types.UNSIGNED_BYTE, (short) GameEventType.RAIN_LEVEL_CHANGE.ordinal()); // event id
                rainStrengthGameEvent.write(Types.FLOAT, joinGameStorage.rainLevel()); // value
                rainStrengthGameEvent.send(BedrockProtocol.class);
            }
            if (joinGameStorage.lightningLevel() > 0F) {
                final PacketWrapper thunderStrengthGameEvent = PacketWrapper.create(ClientboundPackets1_21.GAME_EVENT, user);
                thunderStrengthGameEvent.write(Types.UNSIGNED_BYTE, (short) GameEventType.THUNDER_LEVEL_CHANGE.ordinal()); // event id
                thunderStrengthGameEvent.write(Types.FLOAT, joinGameStorage.lightningLevel()); // value
                thunderStrengthGameEvent.send(BedrockProtocol.class);
            }
        }

        final PacketWrapper setTime = PacketWrapper.create(ClientboundBedrockPackets.SET_TIME, user);
        setTime.write(BedrockTypes.VAR_INT, joinGameStorage.currentTime()); // time of day
        setTime.send(BedrockProtocol.class, false);
    }

    private static void sendStartGameResponsePackets(final UserConnection user) {
        final GameSessionStorage gameSession = user.get(GameSessionStorage.class);
        if (gameSession.hasSentStartGameResponsePackets()) return;
        gameSession.setSentStartGameResponsePackets(true);

        final PacketWrapper requestChunkRadius = PacketWrapper.create(ServerboundBedrockPackets.REQUEST_CHUNK_RADIUS, user);
        requestChunkRadius.write(BedrockTypes.VAR_INT, user.get(ClientSettingsStorage.class).viewDistance()); // radius
        requestChunkRadius.write(Types.BYTE, ProtocolConstants.BEDROCK_REQUEST_CHUNK_RADIUS_MAX_RADIUS); // max radius
        requestChunkRadius.sendToServer(BedrockProtocol.class);
    }

}
