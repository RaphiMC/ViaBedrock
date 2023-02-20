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

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.util.MathUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockChunk;
import net.raphimc.viabedrock.api.chunk.RawBlockEntity;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockBiomeArray;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockDataPalette;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSection;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSectionImpl;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.SubChunkResult;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.SpawnPositionStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.ByteArrayType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class WorldPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.SET_SPAWN_POSITION, ClientboundPackets1_19_3.SPAWN_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final SpawnPositionStorage spawnPositionStorage = wrapper.user().get(SpawnPositionStorage.class);
                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);

                    final int type = wrapper.read(BedrockTypes.VAR_INT); // type
                    if (type != 1) { // WORLD_SPAWN
                        wrapper.cancel();
                        return;
                    }

                    final Position compassPosition = wrapper.read(BedrockTypes.POSITION_3I); // compass position
                    final int dimensionId = wrapper.read(BedrockTypes.VAR_INT); // dimension
                    wrapper.read(BedrockTypes.POSITION_3I); // spawn position

                    spawnPositionStorage.setSpawnPosition(dimensionId, compassPosition);
                    if (chunkTracker.getDimensionId() != dimensionId) {
                        wrapper.cancel();
                        return;
                    }

                    wrapper.write(Type.POSITION1_14, compassPosition); // position
                    wrapper.write(Type.FLOAT, 0F); // angle
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.LEVEL_CHUNK, ClientboundPackets1_19_3.CHUNK_DATA, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

                    final int chunkX = wrapper.read(BedrockTypes.VAR_INT); // chunk x
                    final int chunkZ = wrapper.read(BedrockTypes.VAR_INT); // chunk z
                    int sectionCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // sub chunk count
                    if (sectionCount < -2) { // Mojang client silently ignores this packet
                        wrapper.cancel();
                        return;
                    }

                    final BedrockChunk previousChunk = chunkTracker.getChunk(chunkX, chunkZ);
                    final int startY = chunkTracker.getMinY() >> 4;
                    final int endY = chunkTracker.getWorldHeight() >> 4;

                    if (sectionCount < 0 && (previousChunk == null || !previousChunk.isRequestSubChunks())) {
                        if (sectionCount == -1) {
                            chunkTracker.requestSubChunks(chunkX, chunkZ, startY, endY + startY);
                        } else if (sectionCount == -2) {
                            final int count = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE); // count
                            chunkTracker.requestSubChunks(chunkX, chunkZ, startY, MathUtil.clamp(startY + count + 1, startY + 1, endY + startY));
                        }

                        chunkTracker.unloadChunk(chunkX, chunkZ);
                    }

                    final boolean cachingEnabled = wrapper.read(Type.BOOLEAN); // caching enabled
                    if (cachingEnabled) {
                        wrapper.read(BedrockTypes.LONG_ARRAY); // blob ids
                        BedrockProtocol.kickForIllegalState(wrapper.user(), "Chunk Caching is not supported yet");
                        wrapper.cancel();
                        return;
                    }

                    final byte[] data = wrapper.read(BedrockTypes.BYTE_ARRAY); // data
                    final ByteBuf dataBuf = Unpooled.wrappedBuffer(data);

                    final BedrockChunkSection[] bedrockSections = new BedrockChunkSection[chunkTracker.getWorldHeight() >> 4];
                    final List<BlockEntity> blockEntities = new ArrayList<>();
                    if (dataBuf.isReadable()) {
                        try {
                            for (int i = 0; i < sectionCount; i++) {
                                bedrockSections[i] = BedrockTypes.CHUNK_SECTION.read(dataBuf); // chunk section
                            }
                            if (gameSession.getBedrockVanillaVersion().isLowerThan("1.18.0")) {
                                final byte[] biomeData = new byte[256];
                                dataBuf.readBytes(biomeData);
                                for (int i = 0; i < bedrockSections.length; i++) {
                                    if (bedrockSections[i] == null) {
                                        bedrockSections[i] = new BedrockChunkSectionImpl();
                                    }
                                    bedrockSections[i].addPalette(PaletteType.BIOMES, new BedrockBiomeArray(biomeData));
                                }
                            } else {
                                for (int i = 0; i < bedrockSections.length; i++) {
                                    if (bedrockSections[i] == null) {
                                        bedrockSections[i] = new BedrockChunkSectionImpl();
                                    }
                                    BedrockDataPalette biomePalette = BedrockTypes.BIOME_PALETTE.read(dataBuf); // biome palette
                                    if (biomePalette == null) {
                                        if (i == 0) {
                                            throw new RuntimeException("First biome palette can not point to previous biome palette");
                                        }
                                        biomePalette = ((BedrockDataPalette) bedrockSections[i - 1].palette(PaletteType.BIOMES)).clone();
                                    }
                                    if (biomePalette.hasTagPalette()) {
                                        throw new RuntimeException("Biome palette can not have tag palette");
                                    }
                                    bedrockSections[i].addPalette(PaletteType.BIOMES, biomePalette);
                                }
                            }

                            dataBuf.skipBytes(1); // border blocks
                            while (dataBuf.isReadable()) {
                                blockEntities.add(new RawBlockEntity((CompoundTag) BedrockTypes.TAG.read(dataBuf))); // block entity tag
                            }
                        } catch (Throwable e) {
                            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error reading chunk data", e);
                        }
                    }

                    if (previousChunk != null && previousChunk.isRequestSubChunks()) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received chunk overwriting sub chunk requested chunk");
                        chunkTracker.unloadChunk(chunkX, chunkZ);
                        chunkTracker.requestSubChunks(chunkX, chunkZ, startY, endY + startY);
                        wrapper.cancel();
                    } else {
                        final BedrockChunk bedrockChunk = new BedrockChunk(chunkX, chunkZ, bedrockSections, new CompoundTag(), blockEntities);
                        bedrockChunk.setRequestSubChunks(sectionCount < 0);
                        chunkTracker.storeChunk(bedrockChunk);
                        chunkTracker.writeChunk(wrapper, chunkX, chunkZ);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SUB_CHUNK, ClientboundPackets1_19_3.CHUNK_DATA, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.cancel(); // We might need to send multiple chunk packets
                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);

                    final boolean cachingEnabled = wrapper.read(Type.BOOLEAN); // caching enabled
                    if (cachingEnabled) {
                        BedrockProtocol.kickForIllegalState(wrapper.user(), "Chunk Caching is not yet supported");
                        wrapper.cancel();
                        return;
                    }

                    final int dimensionId = wrapper.read(BedrockTypes.VAR_INT); // dimension id
                    if (dimensionId != chunkTracker.getDimensionId()) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received sub chunk for wrong dimension");
                        wrapper.cancel();
                        return;
                    }

                    final Position center = wrapper.read(BedrockTypes.POSITION_3I); // center position
                    final Set<Long> updatedChunks = new HashSet<>();
                    final long count = wrapper.read(BedrockTypes.UNSIGNED_INT_LE); // count
                    for (long i = 0; i < count; i++) {
                        final Position offset = wrapper.read(BedrockTypes.SUB_CHUNK_OFFSET); // offset
                        final Position absolute = new Position(center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z());
                        final byte result = wrapper.read(Type.BYTE); // result

                        if (result != SubChunkResult.SUCCESS_ALL_AIR || !cachingEnabled) {
                            final byte[] data = wrapper.read(BedrockTypes.BYTE_ARRAY); // data
                            final ByteBuf dataBuf = Unpooled.wrappedBuffer(data);

                            if (result == SubChunkResult.SUCCESS) {
                                BedrockChunkSection bedrockSection = null;
                                final List<BlockEntity> blockEntities = new ArrayList<>();
                                try {
                                    bedrockSection = BedrockTypes.CHUNK_SECTION.read(dataBuf); // chunk section
                                    while (dataBuf.isReadable()) {
                                        blockEntities.add(new RawBlockEntity((CompoundTag) BedrockTypes.TAG.read(dataBuf))); // block entity tag
                                    }
                                } catch (Throwable e) {
                                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error reading sub chunk data", e);
                                }
                                if (chunkTracker.mergeChunkSection(absolute.x(), absolute.y(), absolute.z(), bedrockSection, blockEntities)) {
                                    updatedChunks.add(chunkTracker.chunkKey(absolute.x(), absolute.z()));
                                }
                            } else if (result != SubChunkResult.SUCCESS_ALL_AIR) {
                                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received sub chunk with result " + result);
                                chunkTracker.requestSubChunk(absolute.x(), absolute.y(), absolute.z());
                            }
                        } else if (result == SubChunkResult.SUCCESS_ALL_AIR) {
                            if (chunkTracker.mergeChunkSection(absolute.x(), absolute.y(), absolute.z(), null, new ArrayList<>())) {
                                updatedChunks.add(chunkTracker.chunkKey(absolute.x(), absolute.z()));
                            }
                        }
                        final byte heightmapType = wrapper.read(Type.BYTE); // heightmap type
                        if (heightmapType == 1) {
                            wrapper.read(new ByteArrayType(256)); // heightmap
                        }
                        if (cachingEnabled) {
                            wrapper.read(BedrockTypes.LONG_LE); // blob id
                        }
                    }

                    for (long chunk : updatedChunks) {
                        final int chunkX = (int) (chunk >> 32);
                        final int chunkZ = (int) chunk;
                        chunkTracker.sendChunk(chunkX, chunkZ);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.NETWORK_CHUNK_PUBLISHER_UPDATE, ClientboundPackets1_19_3.UPDATE_VIEW_DISTANCE, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final Position position = wrapper.read(BedrockTypes.POSITION_3I); // center position
                    final int radius = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT) >> 4; // radius

                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
                    chunkTracker.setRadius(radius);
                    chunkTracker.setCenter(position.x() >> 4, position.z() >> 4);

                    wrapper.write(Type.VAR_INT, radius); // radius

                    // First update the view position. This won't rebuild the internal chunk map
                    final PacketWrapper updateViewPosition = wrapper.create(ClientboundPackets1_19_3.UPDATE_VIEW_POSITION);
                    updateViewPosition.write(Type.VAR_INT, position.x() >> 4); // chunk x
                    updateViewPosition.write(Type.VAR_INT, position.z() >> 4); // chunk z
                    updateViewPosition.send(BedrockProtocol.class);
                    // Set the view distance to radius + 1. This will rebuild the internal chunk map
                    final PacketWrapper updateViewDistance = wrapper.create(ClientboundPackets1_19_3.UPDATE_VIEW_DISTANCE);
                    updateViewDistance.write(Type.VAR_INT, radius + 1); // radius
                    updateViewDistance.send(BedrockProtocol.class);
                    // The real view distance packet will be received after that negating the previous packet

                    // TODO: What to do with this?
                    final int count = wrapper.read(BedrockTypes.INT_LE); // saved chunks count
                    for (int i = 0; i < count; i++) {
                        wrapper.read(BedrockTypes.VAR_INT); // chunk x
                        wrapper.read(BedrockTypes.VAR_INT); // chunk z
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CHUNK_RADIUS_UPDATED, ClientboundPackets1_19_3.UPDATE_VIEW_DISTANCE, new PacketHandlers() {
            @Override
            public void register() {
                map(BedrockTypes.VAR_INT, Type.VAR_INT); // radius
                handler(wrapper -> {
                    wrapper.user().get(ChunkTracker.class).setRadius(wrapper.get(Type.VAR_INT, 0));
                });
            }
        });
        // TODO: Dimension change -> store spawn position
        protocol.registerClientbound(ClientboundBedrockPackets.SET_TIME, ClientboundPackets1_19_3.TIME_UPDATE, new PacketHandlers() {
            @Override
            public void register() {
                map(BedrockTypes.VAR_INT, Type.LONG); // game time
                handler(wrapper -> {
                    wrapper.write(Type.LONG, wrapper.get(Type.LONG, 0) % 24000L); // time of day

                    if (!wrapper.user().has(ChunkTracker.class)) { // Bedrock servers might send this packet before the world is initialized
                        wrapper.cancel();
                    }
                });
            }
        });
    }

}
