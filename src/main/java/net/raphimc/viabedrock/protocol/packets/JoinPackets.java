/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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

import com.vdurmont.semver4j.Semver;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.BitSets;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.BiomeRegistry;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.MovePlayerModes;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ServerMovementModes;
import net.raphimc.viabedrock.protocol.data.enums.java.DimensionKeys;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEvents;
import net.raphimc.viabedrock.protocol.model.*;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.DimensionIdRewriter;
import net.raphimc.viabedrock.protocol.rewriter.GameTypeRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.JavaTypes;

import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class JoinPackets {

    public static final int DEFAULT_VIEW_DISTANCE = 6;

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.START_GAME, null, wrapper -> {
            wrapper.cancel(); // We need to fix the order of the packets
            final SpawnPositionStorage spawnPositionStorage = wrapper.user().get(SpawnPositionStorage.class);
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);

            if (wrapper.user().has(GameSessionStorage.class)) {
                return; // Mojang client silently ignores multiple start game packets
            }

            if (resourcePacksStorage == null || !resourcePacksStorage.hasCompletedTransfer()) {
                BedrockProtocol.kickForIllegalState(wrapper.user(), "Pack negotiation not completed");
                return;
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
            final int dimensionId = wrapper.read(BedrockTypes.VAR_INT); // dimension id
            final int generatorId = wrapper.read(BedrockTypes.VAR_INT); // generator id
            final int levelGameType = wrapper.read(BedrockTypes.VAR_INT); // level game type
            final int difficulty = wrapper.read(BedrockTypes.VAR_INT); // difficulty
            final Position defaultSpawnPosition = wrapper.read(BedrockTypes.BLOCK_POSITION); // default spawn position
            wrapper.read(Type.BOOLEAN); // achievements disabled
            final int editorWorldType = wrapper.read(BedrockTypes.VAR_INT); // world editor type
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
            final byte chatRestrictionLevel = wrapper.read(Type.BYTE); // chat restriction level
            wrapper.read(Type.BOOLEAN); // disabling player interactions

            // Continue reading start game packet
            wrapper.read(BedrockTypes.STRING); // level id
            final String levelName = wrapper.read(BedrockTypes.STRING); // level name
            wrapper.read(BedrockTypes.STRING); // premium world template id
            wrapper.read(Type.BOOLEAN); // is trial
            final int movementMode = wrapper.read(BedrockTypes.VAR_INT); // movement mode
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

            if (editorWorldType != 0) {
                final PacketWrapper disconnect = PacketWrapper.create(ClientboundPackets1_19_4.DISCONNECT, wrapper.user());
                disconnect.write(Type.COMPONENT, TextUtil.stringToGson(resourcePacksStorage.getTranslations().get("disconnectionScreen.editor.mismatchEditorWorld"))); // reason
                disconnect.send(BedrockProtocol.class);
                return;
            }

            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Server feature version: " + vanillaVersion);

            for (Experiment experiment : experiments) {
                if (experiment.enabled()) {
                    Via.getPlatform().getLogger().log(Level.WARNING, "This server uses an experimental feature: " + experiment.name());
                }
            }

            Semver version;
            try {
                if (vanillaVersion.equals("*")) {
                    version = new Semver("99.99.99");
                } else {
                    version = new Semver(vanillaVersion, Semver.SemverType.LOOSE);
                }
            } catch (Throwable e) {
                Via.getPlatform().getLogger().log(Level.SEVERE, "Invalid vanilla version: " + vanillaVersion);
                version = new Semver("99.99.99");
            }

            final CompoundTag registries = BedrockProtocol.MAPPINGS.getJavaRegistries().clone();
            final CompoundTag dimensionRegistry = registries.get("minecraft:dimension_type");
            final CompoundTag biomeRegistry = registries.get("minecraft:worldgen/biome");
            final ListTag dimensions = dimensionRegistry.get("value");
            final Map<String, CompoundTag> dimensionMap = dimensions.getValue()
                    .stream()
                    .map(CompoundTag.class::cast)
                    .collect(Collectors.toMap(tag -> tag.get("name").getValue().toString(), tag -> tag.get("element")));

            dimensionMap.get("minecraft:the_nether").put("min_y", new IntTag(0));
            dimensionMap.get("minecraft:the_nether").put("height", new IntTag(128));
            if (version.isLowerThan("1.18.0")) {
                dimensionMap.get("minecraft:overworld").put("min_y", new IntTag(0));
                dimensionMap.get("minecraft:overworld").put("height", new IntTag(256));
                dimensionMap.get("minecraft:overworld").put("logical_height", new IntTag(256));
                dimensionMap.get("minecraft:overworld_caves").put("min_y", new IntTag(0));
                dimensionMap.get("minecraft:overworld_caves").put("height", new IntTag(256));
                dimensionMap.get("minecraft:overworld_caves").put("logical_height", new IntTag(256));
            }

            biomeRegistry.put("value", BiomeRegistry.buildJavaBiomeRegistry(BedrockProtocol.MAPPINGS.getBedrockBiomeDefinitions()));

            final GameSessionStorage gameSession = new GameSessionStorage(wrapper.user());
            wrapper.user().put(gameSession);
            gameSession.setBedrockVanillaVersion(version);
            gameSession.setJavaRegistries(registries);
            gameSession.setFlatGenerator(generatorId == 2);
            gameSession.setMovementMode(movementMode);
            gameSession.setLevelGameType(levelGameType);
            gameSession.setChatRestrictionLevel(chatRestrictionLevel);
            gameSession.setCommandsEnabled(commandsEnabled);
            gameSession.setPlayerPermission(playerPermission);

            if (movementMode >= ServerMovementModes.SERVER_WITH_REWIND) {
                ViaBedrock.getPlatform().getLogger().log(Level.SEVERE, "This server uses server authoritative movement with rewind. This is not supported.");
            } else if (movementMode >= ServerMovementModes.SERVER) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "This server uses server authoritative movement. This is not stable yet.");
            }

            spawnPositionStorage.setSpawnPosition(dimensionId, defaultSpawnPosition);

            final ClientPlayerEntity clientPlayer = new ClientPlayerEntity(wrapper.user(), uniqueEntityId, runtimeEntityId, 0, wrapper.user().getProtocolInfo().getUuid());
            clientPlayer.setPosition(new Position3f(playerPosition.x(), playerPosition.y() + clientPlayer.eyeOffset(), playerPosition.z()));
            clientPlayer.setRotation(new Position3f(playerRotation.x(), playerRotation.y(), 0F));
            clientPlayer.setOnGround(false);
            clientPlayer.setGameType(playerGameType);
            clientPlayer.setName(wrapper.user().getProtocolInfo().getUsername());

            final PacketWrapper joinGame = PacketWrapper.create(ClientboundPackets1_19_4.JOIN_GAME, wrapper.user());
            joinGame.write(Type.INT, clientPlayer.javaId()); // entity id
            joinGame.write(Type.BOOLEAN, false); // hardcore
            joinGame.write(Type.UNSIGNED_BYTE, GameTypeRewriter.getEffectiveGameMode(playerGameType, levelGameType)); // game mode
            joinGame.write(Type.BYTE, (byte) -1); // previous game mode
            joinGame.write(Type.STRING_ARRAY, new String[]{DimensionKeys.OVERWORLD, DimensionKeys.NETHER, DimensionKeys.END}); // dimension types
            joinGame.write(Type.NAMED_COMPOUND_TAG, registries.clone()); // registries
            joinGame.write(Type.STRING, DimensionIdRewriter.dimensionIdToDimensionKey(dimensionId)); // dimension type
            joinGame.write(Type.STRING, DimensionIdRewriter.dimensionIdToDimensionKey(dimensionId)); // dimension id
            joinGame.write(Type.LONG, 0L); // hashed seed
            joinGame.write(Type.VAR_INT, 100); // max players
            joinGame.write(Type.VAR_INT, DEFAULT_VIEW_DISTANCE); // view distance
            joinGame.write(Type.VAR_INT, DEFAULT_VIEW_DISTANCE); // simulation distance
            joinGame.write(Type.BOOLEAN, false); // reduced debug info
            joinGame.write(Type.BOOLEAN, true); // show death screen
            joinGame.write(Type.BOOLEAN, false); // is debug
            joinGame.write(Type.BOOLEAN, gameSession.isFlatGenerator()); // is flat
            joinGame.write(Type.OPTIONAL_GLOBAL_POSITION, null); // last death location
            joinGame.write(Type.VAR_INT, 0); // portal cooldown
            joinGame.send(BedrockProtocol.class);

            wrapper.user().put(new BlockStateRewriter(wrapper.user(), blockProperties, hashedRuntimeBlockIds));
            wrapper.user().put(new ItemRewriter(wrapper.user(), itemEntries));
            wrapper.user().put(new ChunkTracker(wrapper.user(), dimensionId));
            final EntityTracker entityTracker = new EntityTracker(wrapper.user());
            entityTracker.addEntity(clientPlayer);
            wrapper.user().put(entityTracker);

            final PacketWrapper brandPluginMessage = PacketWrapper.create(ClientboundPackets1_19_4.PLUGIN_MESSAGE, Unpooled.buffer(), wrapper.user());
            brandPluginMessage.write(Type.STRING, "minecraft:brand"); // channel
            brandPluginMessage.write(Type.STRING, "Bedrock" + (!serverEngine.isEmpty() ? " @" + serverEngine : "") + " v: " + vanillaVersion); // content
            brandPluginMessage.send(BedrockProtocol.class);

            final PacketWrapper serverDifficulty = PacketWrapper.create(ClientboundPackets1_19_4.SERVER_DIFFICULTY, wrapper.user());
            serverDifficulty.write(Type.UNSIGNED_BYTE, (short) difficulty); // difficulty
            serverDifficulty.write(Type.BOOLEAN, false); // locked
            serverDifficulty.send(BedrockProtocol.class);

            final PacketWrapper tags = PacketWrapper.create(ClientboundPackets1_19_4.TAGS, wrapper.user());
            tags.write(Type.VAR_INT, BedrockProtocol.MAPPINGS.getJavaTags().size()); // number of registries
            for (Map.Entry<String, Tag> registryEntry : BedrockProtocol.MAPPINGS.getJavaTags().entrySet()) {
                final CompoundTag tag = (CompoundTag) registryEntry.getValue();
                tags.write(Type.STRING, registryEntry.getKey()); // registry key
                tags.write(Type.VAR_INT, tag.size()); // number of tags
                for (Map.Entry<String, Tag> tagEntry : tag.entrySet()) {
                    tags.write(Type.STRING, tagEntry.getKey()); // tag name
                    tags.write(Type.VAR_INT_ARRAY_PRIMITIVE, ((IntArrayTag) tagEntry.getValue()).getValue()); // tag ids
                }
            }
            tags.send(BedrockProtocol.class);

            final PacketWrapper tabList = PacketWrapper.create(ClientboundPackets1_19_4.TAB_LIST, wrapper.user());
            tabList.write(Type.COMPONENT, TextUtil.stringToGson(levelName + "\n")); // header
            tabList.write(Type.COMPONENT, TextUtil.stringToGson("§aViaBedrock §3v" + ViaBedrock.VERSION + "\n§7https://github.com/RaphiMC/ViaBedrock")); // footer
            tabList.send(BedrockProtocol.class);

            final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_19_4.PLAYER_INFO_UPDATE, wrapper.user());
            playerInfoUpdate.write(JavaTypes.PROFILE_ACTIONS_ENUM, BitSets.create(6, 0, 2)); // actions | ADD_PLAYER, UPDATE_GAME_MODE
            playerInfoUpdate.write(Type.VAR_INT, 1); // length
            playerInfoUpdate.write(Type.UUID, clientPlayer.javaUuid()); // uuid
            playerInfoUpdate.write(Type.STRING, StringUtil.encodeUUID(clientPlayer.javaUuid())); // username
            playerInfoUpdate.write(Type.VAR_INT, 0); // property count
            playerInfoUpdate.write(Type.VAR_INT, (int) GameTypeRewriter.getEffectiveGameMode(playerGameType, levelGameType)); // game mode
            playerInfoUpdate.send(BedrockProtocol.class);

            if (rainLevel > 0F || lightningLevel > 0F) {
                final PacketWrapper rainStartGameEvent = PacketWrapper.create(ClientboundPackets1_19_4.GAME_EVENT, wrapper.user());
                rainStartGameEvent.write(Type.UNSIGNED_BYTE, GameEvents.RAIN_STARTED); // event id
                rainStartGameEvent.write(Type.FLOAT, 0F); // value
                rainStartGameEvent.send(BedrockProtocol.class);

                if (rainLevel > 0F) {
                    final PacketWrapper rainStrengthGameEvent = PacketWrapper.create(ClientboundPackets1_19_4.GAME_EVENT, wrapper.user());
                    rainStrengthGameEvent.write(Type.UNSIGNED_BYTE, GameEvents.RAIN_GRADIENT_CHANGED); // event id
                    rainStrengthGameEvent.write(Type.FLOAT, rainLevel); // value
                    rainStrengthGameEvent.send(BedrockProtocol.class);
                }
                if (lightningLevel > 0F) {
                    final PacketWrapper thunderStrengthGameEvent = PacketWrapper.create(ClientboundPackets1_19_4.GAME_EVENT, wrapper.user());
                    thunderStrengthGameEvent.write(Type.UNSIGNED_BYTE, GameEvents.THUNDER_GRADIENT_CHANGED); // event id
                    thunderStrengthGameEvent.write(Type.FLOAT, lightningLevel); // value
                    thunderStrengthGameEvent.send(BedrockProtocol.class);
                }
            }

            entityTracker.getClientPlayer().sendPlayerPositionPacketToClient(false);

            final PacketWrapper requestChunkRadius = PacketWrapper.create(ServerboundBedrockPackets.REQUEST_CHUNK_RADIUS, wrapper.user());
            requestChunkRadius.write(BedrockTypes.VAR_INT, DEFAULT_VIEW_DISTANCE); // radius
            requestChunkRadius.write(Type.UNSIGNED_BYTE, ProtocolConstants.REQUEST_CHUNK_RADIUS_MAX_RADIUS); // max radius
            requestChunkRadius.sendToServer(BedrockProtocol.class);

            final PacketWrapper tickSync = PacketWrapper.create(ServerboundBedrockPackets.TICK_SYNC, wrapper.user());
            tickSync.write(BedrockTypes.LONG_LE, 0L); // request timestamp
            tickSync.write(BedrockTypes.LONG_LE, 0L); // response timestamp
            tickSync.sendToServer(BedrockProtocol.class);

            if (gameSession.getMovementMode() == ServerMovementModes.CLIENT) {
                entityTracker.getClientPlayer().sendMovePlayerPacketToServer(MovePlayerModes.NORMAL);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.BIOME_DEFINITION_LIST, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().get(GameSessionStorage.class).setBedrockBiomeDefinitions((CompoundTag) wrapper.read(BedrockTypes.NETWORK_TAG)); // biome definitions
        });
        protocol.registerClientbound(ClientboundBedrockPackets.COMPRESSED_BIOME_DEFINITION_LIST, null, wrapper -> {
            // Compressed biome definitions are used for the clientside generation of the world. Should not be sent as we tell the server that the client doesn't support it.
            BedrockProtocol.kickForIllegalState(wrapper.user(), "Compressed biome definitions are not supported.");
        });
    }

}
