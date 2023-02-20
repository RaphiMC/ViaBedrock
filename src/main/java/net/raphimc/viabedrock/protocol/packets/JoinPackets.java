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
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import net.raphimc.viabedrock.api.JsonUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.MovePlayerMode;
import net.raphimc.viabedrock.protocol.data.enums.java.DimensionKeys;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEvents;
import net.raphimc.viabedrock.protocol.model.BlockProperties;
import net.raphimc.viabedrock.protocol.model.Experiment;
import net.raphimc.viabedrock.protocol.model.Position2f;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.DimensionIdRewriter;
import net.raphimc.viabedrock.protocol.rewriter.GameTypeRewriter;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class JoinPackets {

    private static final int DEFAULT_VIEW_DISTANCE = 8;

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.START_GAME, null, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.cancel(); // We need to fix the order of the packets
                    final SpawnPositionStorage spawnPositionStorage = wrapper.user().get(SpawnPositionStorage.class);
                    final GameSessionStorage gameSessionStorage = wrapper.user().get(GameSessionStorage.class);
                    final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

                    if (gameSessionStorage.getJavaRegistries() != null) {
                        BedrockProtocol.kickForIllegalState(wrapper.user(), "Received StartGame packet twice");
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
                    final Position defaultSpawnPosition = wrapper.read(BedrockTypes.POSITION_3I); // default spawn position
                    wrapper.read(Type.BOOLEAN); // achievements disabled
                    final boolean isWorldEditor = wrapper.read(Type.BOOLEAN); // world editor
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
                    wrapper.read(BedrockTypes.VAR_INT); // player permission
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
                    if (wrapper.read(Type.BOOLEAN)) { // enable experimental game play
                        wrapper.read(Type.BOOLEAN); // force experimental game play
                    }
                    final byte chatRestrictionLevel = wrapper.read(Type.BYTE); // chat restriction level
                    wrapper.read(Type.BOOLEAN); // disabling player interactions

                    // Continue reading start game packet
                    wrapper.read(BedrockTypes.STRING); // level id
                    wrapper.read(BedrockTypes.STRING); // level name
                    wrapper.read(BedrockTypes.STRING); // premium world template id
                    wrapper.read(BedrockTypes.VAR_INT); // movement mode
                    wrapper.read(BedrockTypes.VAR_INT); // rewind history size
                    wrapper.read(Type.BOOLEAN); // is trial
                    wrapper.read(Type.BOOLEAN); // server authoritative block breaking
                    wrapper.read(BedrockTypes.LONG_LE); // current tick
                    wrapper.read(BedrockTypes.VAR_INT); // enchantment seed
                    final BlockProperties[] blockProperties = wrapper.read(BedrockTypes.BLOCK_PROPERTIES_ARRAY); // block properties
                    wrapper.read(BedrockTypes.ITEM_ENTRY_ARRAY); // item entries
                    wrapper.read(BedrockTypes.STRING); // multiplayer correlation id
                    wrapper.read(Type.BOOLEAN); // inventories server authoritative
                    final String serverEngine = wrapper.read(BedrockTypes.STRING); // server engine
                    wrapper.read(BedrockTypes.TAG); // player property data
                    wrapper.read(BedrockTypes.LONG_LE); // block registry checksum
                    wrapper.read(BedrockTypes.UUID); // world template id
                    wrapper.read(Type.BOOLEAN); // client side generation

                    if (isWorldEditor) {
                        final PacketWrapper disconnect = PacketWrapper.create(ClientboundPackets1_19_3.DISCONNECT, wrapper.user());
                        disconnect.write(Type.COMPONENT, JsonUtil.textToComponent(BedrockProtocol.MAPPINGS.getTranslations().get("disconnectionScreen.editor.mismatchEditorWorld"))); // reason
                        disconnect.send(BedrockProtocol.class);
                        return;
                    }

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
                    gameSessionStorage.setBedrockVanillaVersion(version);

                    final CompoundTag registries = BedrockProtocol.MAPPINGS.getRegistries().clone();
                    final CompoundTag dimensionRegistry = registries.get("minecraft:dimension_type");
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

                    gameSessionStorage.setJavaRegistries(registries);

                    wrapper.user().put(new BlockStateRewriter(wrapper.user(), blockProperties));
                    wrapper.user().put(new ChunkTracker(wrapper.user(), dimensionId));
                    wrapper.user().put(new ChatSettingsStorage(wrapper.user(), chatRestrictionLevel >= 1, commandsEnabled));
                    spawnPositionStorage.setSpawnPosition(dimensionId, defaultSpawnPosition);
                    final int javaEntityId = entityTracker.addClientPlayer(uniqueEntityId, runtimeEntityId).javaId();
                    entityTracker.getClientPlayer().setPosition(new Position3f(playerPosition.x(), playerPosition.y() + 1.62F, playerPosition.z()));
                    entityTracker.getClientPlayer().setRotation(new Position3f(playerRotation.x(), playerRotation.y(), 0F));
                    entityTracker.getClientPlayer().setOnGround(false);

                    final PacketWrapper joinGame = PacketWrapper.create(ClientboundPackets1_19_3.JOIN_GAME, wrapper.user());
                    joinGame.write(Type.INT, javaEntityId); // entity id
                    joinGame.write(Type.BOOLEAN, false); // hardcore
                    joinGame.write(Type.UNSIGNED_BYTE, GameTypeRewriter.getEffectiveGameMode(playerGameType, levelGameType)); // gamemode
                    joinGame.write(Type.BYTE, (byte) -1); // previous gamemode
                    joinGame.write(Type.STRING_ARRAY, new String[]{DimensionKeys.OVERWORLD, DimensionKeys.NETHER, DimensionKeys.END}); // dimension types
                    joinGame.write(Type.NBT, registries); // registries
                    joinGame.write(Type.STRING, DimensionIdRewriter.dimensionIdToDimensionKey(dimensionId)); // dimension type
                    joinGame.write(Type.STRING, DimensionIdRewriter.dimensionIdToDimensionKey(dimensionId)); // dimension id
                    joinGame.write(Type.LONG, 0L); // hashed seed
                    joinGame.write(Type.VAR_INT, 100); // max players
                    joinGame.write(Type.VAR_INT, DEFAULT_VIEW_DISTANCE); // view distance
                    joinGame.write(Type.VAR_INT, DEFAULT_VIEW_DISTANCE); // simulation distance
                    joinGame.write(Type.BOOLEAN, false); // reduced debug info
                    joinGame.write(Type.BOOLEAN, true); // show death screen
                    joinGame.write(Type.BOOLEAN, false); // is debug
                    joinGame.write(Type.BOOLEAN, generatorId == 2); // is flat
                    joinGame.write(Type.OPTIONAL_GLOBAL_POSITION, null); // last death location
                    joinGame.send(BedrockProtocol.class);

                    final PacketWrapper brandPluginMessage = PacketWrapper.create(ClientboundPackets1_19_3.PLUGIN_MESSAGE, wrapper.user());
                    brandPluginMessage.write(Type.STRING, "minecraft:brand"); // channel
                    brandPluginMessage.write(Type.STRING, "Bedrock" + (!serverEngine.isEmpty() ? " @" + serverEngine : "") + " v: " + vanillaVersion); // content
                    brandPluginMessage.send(BedrockProtocol.class);

                    final PacketWrapper serverDifficulty = PacketWrapper.create(ClientboundPackets1_19_3.SERVER_DIFFICULTY, wrapper.user());
                    serverDifficulty.write(Type.UNSIGNED_BYTE, (short) difficulty); // difficulty
                    serverDifficulty.write(Type.BOOLEAN, false); // locked
                    serverDifficulty.send(BedrockProtocol.class);

                    final PacketWrapper tags = PacketWrapper.create(ClientboundPackets1_19_3.TAGS, wrapper.user());
                    tags.write(Type.VAR_INT, BedrockProtocol.MAPPINGS.getTags().size()); // number of registries
                    for (Map.Entry<String, Tag> registryEntry : BedrockProtocol.MAPPINGS.getTags().entrySet()) {
                        final CompoundTag tag = (CompoundTag) registryEntry.getValue();
                        tags.write(Type.STRING, registryEntry.getKey()); // registry key
                        tags.write(Type.VAR_INT, tag.size()); // number of tags
                        for (Map.Entry<String, Tag> tagEntry : tag.entrySet()) {
                            tags.write(Type.STRING, tagEntry.getKey()); // tag name
                            tags.write(Type.VAR_INT_ARRAY_PRIMITIVE, ((IntArrayTag) tagEntry.getValue()).getValue()); // tag ids
                        }
                    }
                    tags.send(BedrockProtocol.class);

                    if (rainLevel > 0F || lightningLevel > 0F) {
                        final PacketWrapper rainStartGameEvent = PacketWrapper.create(ClientboundPackets1_19_3.GAME_EVENT, wrapper.user());
                        rainStartGameEvent.write(Type.VAR_INT, GameEvents.RAIN_STARTED); // event id
                        rainStartGameEvent.write(Type.FLOAT, 0F); // value
                        rainStartGameEvent.send(BedrockProtocol.class);

                        if (rainLevel > 0F) {
                            final PacketWrapper rainStrengthGameEvent = PacketWrapper.create(ClientboundPackets1_19_3.GAME_EVENT, wrapper.user());
                            rainStrengthGameEvent.write(Type.VAR_INT, GameEvents.RAIN_GRADIENT_CHANGED); // event id
                            rainStrengthGameEvent.write(Type.FLOAT, rainLevel); // value
                            rainStrengthGameEvent.send(BedrockProtocol.class);
                        }
                        if (lightningLevel > 0F) {
                            final PacketWrapper thunderStrengthGameEvent = PacketWrapper.create(ClientboundPackets1_19_3.GAME_EVENT, wrapper.user());
                            thunderStrengthGameEvent.write(Type.VAR_INT, GameEvents.THUNDER_GRADIENT_CHANGED); // event id
                            thunderStrengthGameEvent.write(Type.FLOAT, lightningLevel); // value
                            thunderStrengthGameEvent.send(BedrockProtocol.class);
                        }
                    }

                    entityTracker.getClientPlayer().sendPlayerPositionPacketToClient(wrapper.user());

                    final PacketWrapper requestChunkRadius = PacketWrapper.create(ServerboundBedrockPackets.REQUEST_CHUNK_RADIUS, wrapper.user());
                    requestChunkRadius.write(BedrockTypes.VAR_INT, DEFAULT_VIEW_DISTANCE); // radius
                    requestChunkRadius.sendToServer(BedrockProtocol.class);

                    final PacketWrapper tickSync = PacketWrapper.create(ServerboundBedrockPackets.TICK_SYNC, wrapper.user());
                    tickSync.write(BedrockTypes.LONG_LE, 0L); // request timestamp
                    tickSync.write(BedrockTypes.LONG_LE, 0L); // response timestamp
                    tickSync.sendToServer(BedrockProtocol.class);

                    entityTracker.getClientPlayer().sendMovementPacketToServer(wrapper.user(), MovePlayerMode.NORMAL);
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.BIOME_DEFINITION_LIST, null, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    wrapper.user().get(GameSessionStorage.class).setBedrockBiomeDefinitions((CompoundTag) wrapper.read(BedrockTypes.TAG)); // biome definitions
                });
            }
        });
    }

}
