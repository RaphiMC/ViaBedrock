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
package net.raphimc.viabedrock.protocol.packets;

import com.google.common.base.Joiner;
import com.vdurmont.semver4j.Semver;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntImmutablePair;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
            final PacketWrapper startConfiguration = PacketWrapper.create(ClientboundPackets1_20_3.START_CONFIGURATION, wrapper.user());
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
                    final int rawStatus = wrapper.read(Type.INT); // status
                    final PlayStatus status = PlayStatus.getByValue(rawStatus);
                    if (status == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown play status: " + rawStatus);
                        wrapper.cancel();
                        return;
                    }

                    if (status == PlayStatus.LoginSuccess) {
                        wrapper.setPacketType(ClientboundLoginPackets.GAME_PROFILE);
                        final AuthChainData authChainData = wrapper.user().get(AuthChainData.class);
                        wrapper.write(Type.UUID, authChainData.getIdentity()); // uuid
                        wrapper.write(Type.STRING, authChainData.getDisplayName()); // username
                        wrapper.write(Type.VAR_INT, 0); // properties length

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
                    final int rawStatus = wrapper.read(Type.INT); // status
                    final PlayStatus status = PlayStatus.getByValue(rawStatus);
                    if (status == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown play status: " + rawStatus);
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

                        if (clientPlayer.isInitiallySpawned()) {
                            if (clientPlayer.isChangingDimension()) {
                                clientPlayer.closeDownloadingTerrainScreen();
                            }

                            return;
                        }

                        final PacketWrapper interact = PacketWrapper.create(ServerboundBedrockPackets.INTERACT, wrapper.user());
                        interact.write(Type.UNSIGNED_BYTE, (short) InteractPacket_Action.InteractUpdate.getValue()); // action
                        interact.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
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
                        }

                        final PacketWrapper setLocalPlayerAsInitialized = PacketWrapper.create(ServerboundBedrockPackets.SET_LOCAL_PLAYER_AS_INITIALIZED, wrapper.user());
                        setLocalPlayerAsInitialized.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                        setLocalPlayerAsInitialized.sendToServer(BedrockProtocol.class);

                        clientPlayer.closeDownloadingTerrainScreen();
                    } else {
                        wrapper.setPacketType(ClientboundPackets1_20_3.DISCONNECT);
                        writePlayStatusKickMessage(wrapper, status);
                    }
                }, State.CONFIGURATION, (PacketHandler) wrapper -> {
                    final int rawStatus = wrapper.read(Type.INT); // status
                    final PlayStatus status = PlayStatus.getByValue(rawStatus);
                    if (status == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown play status: " + rawStatus);
                        wrapper.cancel();
                        return;
                    }

                    if (status == PlayStatus.LoginSuccess) {
                        wrapper.cancel();
                        sendClientCacheStatus(wrapper.user());
                    } else {
                        wrapper.setPacketType(ClientboundConfigurationPackets1_20_3.DISCONNECT);
                        writePlayStatusKickMessage(wrapper, status);
                    }
                }
        );
        protocol.registerClientboundTransition(ClientboundBedrockPackets.START_GAME,
                State.CONFIGURATION, (PacketHandler) wrapper -> {
                    wrapper.cancel(); // We need to fix the order of the packets
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
                    final int difficulty = wrapper.read(BedrockTypes.VAR_INT); // difficulty
                    wrapper.read(BedrockTypes.BLOCK_POSITION); // default spawn position
                    wrapper.read(Type.BOOLEAN); // achievements disabled
                    final Editor_WorldType editorWorldType = Editor_WorldType.getByValue(wrapper.read(BedrockTypes.VAR_INT)); // world editor type
                    wrapper.read(Type.BOOLEAN); // created in world editor
                    wrapper.read(Type.BOOLEAN); // exported from world editor
                    wrapper.read(BedrockTypes.VAR_INT); // day cycle stop time
                    wrapper.read(BedrockTypes.VAR_INT); // education edition offers
                    wrapper.read(Type.BOOLEAN); // education features enabled
                    wrapper.read(BedrockTypes.STRING); // education product id
                    final float rainLevel = wrapper.read(BedrockTypes.FLOAT_LE); // rain level
                    final float lightningLevel = wrapper.read(BedrockTypes.FLOAT_LE); // lightning level
                    wrapper.read(Type.BOOLEAN); // platform locked content confirmed
                    wrapper.read(Type.BOOLEAN); // multiplayer game
                    wrapper.read(Type.BOOLEAN); // is broadcasting to lan
                    wrapper.read(BedrockTypes.VAR_INT); // Xbox Live broadcast mode
                    wrapper.read(BedrockTypes.VAR_INT); // platform broadcast mode
                    final boolean commandsEnabled = wrapper.read(Type.BOOLEAN); // commands enabled
                    wrapper.read(Type.BOOLEAN); // texture packs required
                    wrapper.read(BedrockTypes.GAME_RULE_ARRAY); // game rules
                    final Experiment[] experiments = wrapper.read(BedrockTypes.EXPERIMENT_ARRAY); // experiments
                    wrapper.read(Type.BOOLEAN); // experiments previously toggled
                    wrapper.read(Type.BOOLEAN); // bonus chest enabled
                    wrapper.read(Type.BOOLEAN); // start with map enabled
                    final int playerPermission = wrapper.read(BedrockTypes.VAR_INT); // player permission
                    wrapper.read(BedrockTypes.INT_LE); // server chunk tick range
                    wrapper.read(Type.BOOLEAN); // behavior pack locked
                    wrapper.read(Type.BOOLEAN); // resource pack locked
                    wrapper.read(Type.BOOLEAN); // from locked world template
                    wrapper.read(Type.BOOLEAN); // using msa gamer tags only
                    wrapper.read(Type.BOOLEAN); // from world template
                    wrapper.read(Type.BOOLEAN); // world template option locked
                    wrapper.read(Type.BOOLEAN); // only spawn v1 villagers
                    wrapper.read(Type.BOOLEAN); // disable personas
                    wrapper.read(Type.BOOLEAN); // disable custom skins
                    wrapper.read(Type.BOOLEAN); // mute emote chat
                    final String vanillaVersion = wrapper.read(BedrockTypes.STRING); // vanilla version
                    wrapper.read(BedrockTypes.INT_LE); // limited world width
                    wrapper.read(BedrockTypes.INT_LE); // limited world height
                    wrapper.read(Type.BOOLEAN); // nether type
                    wrapper.read(BedrockTypes.EDUCATION_URI_RESOURCE); // education shared uri
                    wrapper.read(Type.BOOLEAN); // enable experimental game play
                    final ChatRestrictionLevel chatRestrictionLevel = ChatRestrictionLevel.getByValue(wrapper.read(Type.BYTE), ChatRestrictionLevel.Disabled); // chat restriction level
                    wrapper.read(Type.BOOLEAN); // disabling player interactions

                    // Continue reading start game packet
                    wrapper.read(BedrockTypes.STRING); // level id
                    final String levelName = wrapper.read(BedrockTypes.STRING); // level name
                    wrapper.read(BedrockTypes.STRING); // premium world template id
                    wrapper.read(Type.BOOLEAN); // is trial
                    final ServerAuthMovementMode movementMode = ServerAuthMovementMode.getByValue(wrapper.read(BedrockTypes.VAR_INT) & 255, ServerAuthMovementMode.ServerAuthoritativeWithRewind); // movement mode
                    wrapper.read(BedrockTypes.VAR_INT); // rewind history size
                    wrapper.read(Type.BOOLEAN); // server authoritative block breaking
                    wrapper.read(BedrockTypes.LONG_LE); // current tick
                    wrapper.read(BedrockTypes.VAR_INT); // enchantment seed
                    final BlockProperties[] blockProperties = wrapper.read(BedrockTypes.BLOCK_PROPERTIES_ARRAY); // block properties
                    final ItemEntry[] itemEntries = wrapper.read(BedrockTypes.ITEM_ENTRY_ARRAY); // item entries
                    wrapper.read(BedrockTypes.STRING); // multiplayer correlation id
                    wrapper.read(Type.BOOLEAN); // server authoritative inventories
                    final String serverEngine = wrapper.read(BedrockTypes.STRING); // server engine
                    wrapper.read(BedrockTypes.NETWORK_TAG); // player property data
                    wrapper.read(BedrockTypes.LONG_LE); // block registry checksum
                    wrapper.read(BedrockTypes.UUID); // world template id
                    wrapper.read(Type.BOOLEAN); // client side generation
                    final boolean hashedRuntimeBlockIds = wrapper.read(Type.BOOLEAN); // use hashed block runtime ids
                    wrapper.read(Type.BOOLEAN); // server authoritative sounds

                    if (editorWorldType == Editor_WorldType.EditorProject) {
                        final PacketWrapper disconnect = PacketWrapper.create(ClientboundConfigurationPackets1_20_3.DISCONNECT, wrapper.user());
                        PacketFactory.writeDisconnect(wrapper, resourcePacksStorage.getTranslations().get("disconnectionScreen.editor.mismatchEditorWorld"));
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
                    gameSession.setAbilities(new PlayerAbilities(uniqueEntityId, (byte) playerPermission, (byte) CommandPermissionLevel.Any.getValue(), new Int2ObjectOpenHashMap<>()));
                    gameSession.setChatRestrictionLevel(chatRestrictionLevel);
                    gameSession.setCommandsEnabled(commandsEnabled);

                    final ClientPlayerEntity clientPlayer = new ClientPlayerEntity(wrapper.user(), uniqueEntityId, runtimeEntityId, wrapper.user().getProtocolInfo().getUuid());
                    clientPlayer.setPosition(new Position3f(playerPosition.x(), playerPosition.y() + clientPlayer.eyeOffset(), playerPosition.z()));
                    clientPlayer.setRotation(new Position3f(playerRotation.x(), playerRotation.y(), 0F));
                    clientPlayer.setOnGround(false);
                    clientPlayer.setGameType(playerGameType);
                    clientPlayer.setName(wrapper.user().getProtocolInfo().getUsername());

                    wrapper.user().put(new JoinGameStorage(levelName, difficulty, rainLevel, lightningLevel));
                    wrapper.user().put(new BlockStateRewriter(blockProperties, hashedRuntimeBlockIds));
                    wrapper.user().put(new ItemRewriter(wrapper.user(), itemEntries));
                    wrapper.user().put(new ChunkTracker(wrapper.user(), dimension));
                    final EntityTracker entityTracker = new EntityTracker(wrapper.user());
                    entityTracker.addEntity(clientPlayer, false);
                    wrapper.user().put(entityTracker);

                    final PacketWrapper brandCustomPayload = PacketWrapper.create(ClientboundConfigurationPackets1_20_3.CUSTOM_PAYLOAD, wrapper.user());
                    brandCustomPayload.write(Type.STRING, "minecraft:brand"); // channel
                    brandCustomPayload.write(Type.STRING, "Bedrock" + (!serverEngine.isEmpty() ? " @" + serverEngine : "") + " v: " + vanillaVersion); // content
                    brandCustomPayload.send(BedrockProtocol.class);

                    if (!enabledFeatures.isEmpty()) {
                        enabledFeatures.add("minecraft:vanilla");
                        final PacketWrapper updateEnabledFeatures = PacketWrapper.create(ClientboundConfigurationPackets1_20_3.UPDATE_ENABLED_FEATURES, wrapper.user());
                        updateEnabledFeatures.write(Type.STRING_ARRAY, enabledFeatures.toArray(new String[0])); // enabled features
                        updateEnabledFeatures.send(BedrockProtocol.class);
                    }

                    handleGameJoin(wrapper.user());
                    sendStartGameResponsePackets(wrapper.user());

                    if (gameSession.getMovementMode() == ServerAuthMovementMode.ClientAuthoritative) {
                        clientPlayer.sendMovePlayerPacketToServer(PlayerPositionModeComponent_PositionMode.Normal);
                    }
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

    private static void sendClientCacheStatus(final UserConnection user) throws Exception {
        final PacketWrapper clientCacheStatus = PacketWrapper.create(ServerboundBedrockPackets.CLIENT_CACHE_STATUS, user);
        clientCacheStatus.write(Type.BOOLEAN, !ViaBedrock.getConfig().getBlobCacheMode().equals(ViaBedrockConfig.BlobCacheMode.DISABLED)); // is supported
        clientCacheStatus.sendToServer(BedrockProtocol.class);
    }

    private static void writePlayStatusKickMessage(final PacketWrapper wrapper, final PlayStatus status) {
        final Map<String, String> translations = BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePack().content().getLang("texts/en_US.lang");

        switch (status) {
            case LoginFailed_ClientOld:
                PacketFactory.writeDisconnect(wrapper, translations.get("disconnectionScreen.outdatedClient"));
                break;
            case LoginFailed_ServerOld:
                PacketFactory.writeDisconnect(wrapper, translations.get("disconnectionScreen.outdatedServer"));
                break;
            case LoginFailed_InvalidTenant:
                PacketFactory.writeDisconnect(wrapper, translations.get("disconnectionScreen.invalidTenant"));
                break;
            case LoginFailed_EditionMismatchEduToVanilla:
                PacketFactory.writeDisconnect(wrapper, translations.get("disconnectionScreen.editionMismatchEduToVanilla"));
                break;
            case LoginFailed_EditionMismatchVanillaToEdu:
                PacketFactory.writeDisconnect(wrapper, translations.get("disconnectionScreen.editionMismatchVanillaToEdu"));
                break;
            case LoginFailed_ServerFullSubClient:
            case LoginFailed_EditorMismatchVanillaToEditor:
                PacketFactory.writeDisconnect(wrapper, translations.get("disconnectionScreen.serverFull") + "\n\n\n\n" + translations.get("disconnectionScreen.serverFull.title"));
                break;
            case LoginFailed_EditorMismatchEditorToVanilla:
                PacketFactory.writeDisconnect(wrapper, translations.get("disconnectionScreen.editor.mismatchEditorToVanilla"));
                break;
            default:
                throw new IllegalStateException("Unhandled play status: " + status);
            case PlayerSpawn:
            case LoginSuccess:
                wrapper.cancel();
                break;
        }
    }

    private static void handleGameJoin(final UserConnection user) throws Exception {
        final JoinGameStorage joinGameStorage = user.get(JoinGameStorage.class);
        final GameSessionStorage gameSession = user.get(GameSessionStorage.class);
        final ClientSettingsStorage clientSettingsStorage = user.get(ClientSettingsStorage.class);
        final ChunkTracker chunkTracker = user.get(ChunkTracker.class);
        final ClientPlayerEntity clientPlayer = user.get(EntityTracker.class).getClientPlayer();

        final PacketWrapper registryData = PacketWrapper.create(ClientboundConfigurationPackets1_20_3.REGISTRY_DATA, user);
        registryData.write(Type.COMPOUND_TAG, gameSession.getJavaRegistries().copy()); // registries
        registryData.send(BedrockProtocol.class);

        final PacketWrapper updateTags = PacketWrapper.create(ClientboundConfigurationPackets1_20_3.UPDATE_TAGS, user);
        updateTags.write(Type.VAR_INT, BedrockProtocol.MAPPINGS.getJavaTags().size()); // number of registries
        for (Map.Entry<String, Tag> registryEntry : BedrockProtocol.MAPPINGS.getJavaTags().entrySet()) {
            final CompoundTag tag = (CompoundTag) registryEntry.getValue();
            updateTags.write(Type.STRING, registryEntry.getKey()); // registry key
            updateTags.write(Type.VAR_INT, tag.size()); // number of tags
            for (Map.Entry<String, Tag> tagEntry : tag.entrySet()) {
                updateTags.write(Type.STRING, tagEntry.getKey()); // tag name
                updateTags.write(Type.VAR_INT_ARRAY_PRIMITIVE, ((IntArrayTag) tagEntry.getValue()).getValue()); // tag ids
            }
        }
        updateTags.send(BedrockProtocol.class);

        final PacketWrapper finishConfiguration = PacketWrapper.create(ClientboundConfigurationPackets1_20_3.FINISH_CONFIGURATION, user);
        finishConfiguration.send(BedrockProtocol.class);
        user.getProtocolInfo().setServerState(State.PLAY);

        final PacketWrapper joinGame = PacketWrapper.create(ClientboundPackets1_20_3.JOIN_GAME, user);
        joinGame.write(Type.INT, clientPlayer.javaId()); // entity id
        joinGame.write(Type.BOOLEAN, false); // hardcore
        joinGame.write(Type.STRING_ARRAY, Dimension.getDimensionKeys()); // dimension types
        joinGame.write(Type.VAR_INT, 100); // max players
        joinGame.write(Type.VAR_INT, clientSettingsStorage.getViewDistance()); // view distance
        joinGame.write(Type.VAR_INT, clientSettingsStorage.getViewDistance()); // simulation distance
        joinGame.write(Type.BOOLEAN, false); // reduced debug info
        joinGame.write(Type.BOOLEAN, true); // show death screen
        joinGame.write(Type.BOOLEAN, false); // limited crafting
        joinGame.write(Type.STRING, chunkTracker.getDimension().getKey()); // dimension type
        joinGame.write(Type.STRING, chunkTracker.getDimension().getKey()); // dimension id
        joinGame.write(Type.LONG, 0L); // hashed seed
        joinGame.write(Type.BYTE, GameTypeRewriter.getEffectiveGameMode(clientPlayer.getGameType(), gameSession.getLevelGameType())); // game mode
        joinGame.write(Type.BYTE, (byte) -1); // previous game mode
        joinGame.write(Type.BOOLEAN, false); // is debug
        joinGame.write(Type.BOOLEAN, gameSession.isFlatGenerator()); // is flat
        joinGame.write(Type.OPTIONAL_GLOBAL_POSITION, null); // last death location
        joinGame.write(Type.VAR_INT, 0); // portal cooldown
        joinGame.send(BedrockProtocol.class);

        clientPlayer.createTeam();
        clientPlayer.sendPlayerPositionPacketToClient(false);

        final PacketWrapper serverDifficulty = PacketWrapper.create(ClientboundPackets1_20_3.SERVER_DIFFICULTY, user);
        serverDifficulty.write(Type.UNSIGNED_BYTE, (short) joinGameStorage.getDifficulty()); // difficulty
        serverDifficulty.write(Type.BOOLEAN, false); // locked
        serverDifficulty.send(BedrockProtocol.class);

        final PacketWrapper tabList = PacketWrapper.create(ClientboundPackets1_20_3.TAB_LIST, user);
        tabList.write(Type.TAG, TextUtil.stringToNbt(joinGameStorage.getLevelName() + "\n")); // header
        tabList.write(Type.TAG, TextUtil.stringToNbt("§aViaBedrock §3v" + ViaBedrock.VERSION + "\n§7https://github.com/RaphiMC/ViaBedrock")); // footer
        tabList.send(BedrockProtocol.class);

        final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_20_3.PLAYER_INFO_UPDATE, user);
        playerInfoUpdate.write(Type.PROFILE_ACTIONS_ENUM, BitSets.create(6, PlayerInfoUpdateAction.ADD_PLAYER.ordinal(), PlayerInfoUpdateAction.UPDATE_GAME_MODE.ordinal())); // actions
        playerInfoUpdate.write(Type.VAR_INT, 1); // length
        playerInfoUpdate.write(Type.UUID, clientPlayer.javaUuid()); // uuid
        playerInfoUpdate.write(Type.STRING, StringUtil.encodeUUID(clientPlayer.javaUuid())); // username
        playerInfoUpdate.write(Type.VAR_INT, 0); // property count
        playerInfoUpdate.write(Type.VAR_INT, (int) GameTypeRewriter.getEffectiveGameMode(clientPlayer.getGameType(), gameSession.getLevelGameType())); // game mode
        playerInfoUpdate.send(BedrockProtocol.class);

        ClientPlayerPackets.handleAbilitiesUpdate(user);

        if (joinGameStorage.getRainLevel() > 0F || joinGameStorage.getLightningLevel() > 0F) {
            final PacketWrapper rainStartGameEvent = PacketWrapper.create(ClientboundPackets1_20_3.GAME_EVENT, user);
            rainStartGameEvent.write(Type.UNSIGNED_BYTE, (short) GameEventType.START_RAINING.ordinal()); // event id
            rainStartGameEvent.write(Type.FLOAT, 0F); // value
            rainStartGameEvent.send(BedrockProtocol.class);

            if (joinGameStorage.getRainLevel() > 0F) {
                final PacketWrapper rainStrengthGameEvent = PacketWrapper.create(ClientboundPackets1_20_3.GAME_EVENT, user);
                rainStrengthGameEvent.write(Type.UNSIGNED_BYTE, (short) GameEventType.RAIN_LEVEL_CHANGE.ordinal()); // event id
                rainStrengthGameEvent.write(Type.FLOAT, joinGameStorage.getRainLevel()); // value
                rainStrengthGameEvent.send(BedrockProtocol.class);
            }
            if (joinGameStorage.getLightningLevel() > 0F) {
                final PacketWrapper thunderStrengthGameEvent = PacketWrapper.create(ClientboundPackets1_20_3.GAME_EVENT, user);
                thunderStrengthGameEvent.write(Type.UNSIGNED_BYTE, (short) GameEventType.THUNDER_LEVEL_CHANGE.ordinal()); // event id
                thunderStrengthGameEvent.write(Type.FLOAT, joinGameStorage.getLightningLevel()); // value
                thunderStrengthGameEvent.send(BedrockProtocol.class);
            }
        }
    }

    private static void sendStartGameResponsePackets(final UserConnection user) throws Exception {
        final GameSessionStorage gameSession = user.get(GameSessionStorage.class);
        if (gameSession.hasSentStartGameResponsePackets()) return;
        gameSession.setSentStartGameResponsePackets(true);

        final PacketWrapper requestChunkRadius = PacketWrapper.create(ServerboundBedrockPackets.REQUEST_CHUNK_RADIUS, user);
        requestChunkRadius.write(BedrockTypes.VAR_INT, user.get(ClientSettingsStorage.class).getViewDistance()); // radius
        requestChunkRadius.write(Type.UNSIGNED_BYTE, ProtocolConstants.BEDROCK_REQUEST_CHUNK_RADIUS_MAX_RADIUS); // max radius
        requestChunkRadius.sendToServer(BedrockProtocol.class);

        final PacketWrapper tickSync = PacketWrapper.create(ServerboundBedrockPackets.TICK_SYNC, user);
        tickSync.write(BedrockTypes.LONG_LE, 0L); // request timestamp
        tickSync.write(BedrockTypes.LONG_LE, 0L); // response timestamp
        tickSync.sendToServer(BedrockProtocol.class);
    }

}
