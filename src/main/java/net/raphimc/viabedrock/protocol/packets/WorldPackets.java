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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
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
import net.raphimc.viabedrock.protocol.providers.BlobCacheProvider;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.SpawnPositionStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.ByteArrayType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
                    wrapper.cancel();
                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

                    final int chunkX = wrapper.read(BedrockTypes.VAR_INT); // chunk x
                    final int chunkZ = wrapper.read(BedrockTypes.VAR_INT); // chunk z
                    final int sectionCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // sub chunk count
                    if (sectionCount < -2) { // Mojang client silently ignores this packet
                        return;
                    }

                    final int startY = chunkTracker.getMinY() >> 4;
                    final int endY = chunkTracker.getMaxY() >> 4;

                    int requestCount = 0;
                    if (sectionCount == -2) {
                        requestCount = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE) + 1; // count
                    } else if (sectionCount == -1) {
                        requestCount = endY - startY;
                    }

                    final BedrockChunk previousChunk = chunkTracker.getChunk(chunkX, chunkZ);
                    if (previousChunk != null) {
                        chunkTracker.unloadChunk(chunkX, chunkZ);
                    }

                    final BedrockChunk chunk = chunkTracker.createChunk(chunkX, chunkZ, sectionCount < 0 ? requestCount : sectionCount);
                    if (chunk == null) return;

                    chunk.setRequestSubChunks(sectionCount < 0);

                    final int fRequestCount = requestCount;
                    final Consumer<byte[]> dataConsumer = combinedData -> {
                        try {
                            if (fRequestCount > 0 && (previousChunk == null || !previousChunk.isRequestSubChunks())) {
                                chunkTracker.requestSubChunks(chunkX, chunkZ, startY, MathUtil.clamp(startY + fRequestCount, startY + 1, endY));
                            } else if (previousChunk != null && previousChunk.isRequestSubChunks()) {
                                chunkTracker.requestSubChunks(chunkX, chunkZ, startY, endY);
                            }

                            final ByteBuf dataBuf = Unpooled.wrappedBuffer(combinedData);

                            final BedrockChunkSection[] sections = chunk.getSections();
                            final List<BlockEntity> blockEntities = chunk.blockEntities();
                            if (dataBuf.isReadable()) {
                                try {
                                    for (int i = 0; i < sectionCount; i++) {
                                        sections[i].mergeWith(chunkTracker.handleBlockPalette(BedrockTypes.CHUNK_SECTION.read(dataBuf))); // chunk section
                                    }
                                    if (gameSession.getBedrockVanillaVersion().isLowerThan("1.18.0")) {
                                        final byte[] biomeData = new byte[256];
                                        dataBuf.readBytes(biomeData);
                                        for (ChunkSection section : sections) {
                                            section.addPalette(PaletteType.BIOMES, new BedrockBiomeArray(biomeData));
                                        }
                                    } else {
                                        for (int i = 0; i < sections.length; i++) {
                                            BedrockDataPalette biomePalette = BedrockTypes.BIOME_PALETTE.read(dataBuf); // biome palette
                                            if (biomePalette == null) {
                                                if (i == 0) {
                                                    throw new RuntimeException("First biome palette can not point to previous biome palette");
                                                }
                                                biomePalette = ((BedrockDataPalette) sections[i - 1].palette(PaletteType.BIOMES)).clone();
                                            }
                                            if (biomePalette.hasTagPalette()) {
                                                throw new RuntimeException("Biome palette can not have tag palette");
                                            }
                                            sections[i].addPalette(PaletteType.BIOMES, biomePalette);
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

                            if (!chunk.isRequestSubChunks()) {
                                chunkTracker.sendChunk(chunkX, chunkZ);
                            }
                        } catch (Throwable e) {
                            throw new RuntimeException("Error handling chunk data", e);
                        }
                    };

                    if (wrapper.read(Type.BOOLEAN)) { // caching enabled
                        final Long[] blobs = wrapper.read(BedrockTypes.LONG_ARRAY); // blob ids
                        final int expectedLength = sectionCount < 0 ? 1 : sectionCount + 1;
                        if (blobs.length != expectedLength) { // Mojang client writes random memory contents into the request and most likely crashes
                            BedrockProtocol.kickForIllegalState(wrapper.user(), "Invalid blob count: " + blobs.length + " (expected " + expectedLength + ")");
                            return;
                        }
                        final byte[] data = wrapper.read(BedrockTypes.BYTE_ARRAY); // data
                        Via.getManager().getProviders().get(BlobCacheProvider.class).getBlob(wrapper.user(), blobs).thenAccept(blob -> {
                            final byte[] combinedData = new byte[data.length + blob.length];
                            System.arraycopy(blob, 0, combinedData, 0, blob.length);
                            System.arraycopy(data, 0, combinedData, blob.length, data.length);
                            dataConsumer.accept(combinedData);
                        });
                    } else {
                        dataConsumer.accept(wrapper.read(BedrockTypes.BYTE_ARRAY)); // data
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SUB_CHUNK, ClientboundPackets1_19_3.CHUNK_DATA, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);

                    final boolean cachingEnabled = wrapper.read(Type.BOOLEAN); // caching enabled
                    final int dimensionId = wrapper.read(BedrockTypes.VAR_INT); // dimension id
                    if (dimensionId != chunkTracker.getDimensionId()) {
                        return;
                    }

                    final Position center = wrapper.read(BedrockTypes.POSITION_3I); // center position
                    final long count = wrapper.read(BedrockTypes.UNSIGNED_INT_LE); // count

                    for (long i = 0; i < count; i++) {
                        final Position offset = wrapper.read(BedrockTypes.SUB_CHUNK_OFFSET); // offset
                        final Position absolute = new Position(center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z());
                        final byte result = wrapper.read(Type.BYTE); // result
                        final byte[] data = result != SubChunkResult.SUCCESS_ALL_AIR || !cachingEnabled ? wrapper.read(BedrockTypes.BYTE_ARRAY) : new byte[0]; // data
                        final byte heightmapResult = wrapper.read(Type.BYTE); // heightmap result
                        final byte[] heightmapData = heightmapResult == 1 ? wrapper.read(new ByteArrayType(256)) : new byte[0]; // heightmap data

                        final Consumer<byte[]> dataConsumer = combinedData -> {
                            try {
                                if (result == SubChunkResult.SUCCESS_ALL_AIR) {
                                    if (chunkTracker.mergeSubChunk(absolute.x(), absolute.y(), absolute.z(), new BedrockChunkSectionImpl(), new ArrayList<>())) {
                                        chunkTracker.sendChunkInNextTick(absolute.x(), absolute.z());
                                    }
                                } else if (result == SubChunkResult.SUCCESS) {
                                    final ByteBuf dataBuf = Unpooled.wrappedBuffer(combinedData);

                                    BedrockChunkSection section = new BedrockChunkSectionImpl();
                                    final List<BlockEntity> blockEntities = new ArrayList<>();
                                    try {
                                        section = BedrockTypes.CHUNK_SECTION.read(dataBuf); // chunk section
                                        while (dataBuf.isReadable()) {
                                            blockEntities.add(new RawBlockEntity((CompoundTag) BedrockTypes.TAG.read(dataBuf))); // block entity tag
                                        }
                                    } catch (Throwable e) {
                                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error reading sub chunk data", e);
                                    }
                                    if (chunkTracker.mergeSubChunk(absolute.x(), absolute.y(), absolute.z(), section, blockEntities)) {
                                        chunkTracker.sendChunkInNextTick(absolute.x(), absolute.z());
                                    }
                                } else {
                                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received sub chunk with result " + result);
                                    chunkTracker.requestSubChunk(absolute.x(), absolute.y(), absolute.z());
                                }
                            } catch (Throwable e) {
                                throw new RuntimeException("Error handling sub chunk data", e);
                            }
                        };

                        if (cachingEnabled) {
                            final long hash = wrapper.read(BedrockTypes.LONG_LE); // blob id
                            Via.getManager().getProviders().get(BlobCacheProvider.class).getBlob(wrapper.user(), hash).thenAccept(blob -> {
                                if (data.length == 0) {
                                    dataConsumer.accept(blob);
                                } else if (blob.length == 0) {
                                    dataConsumer.accept(data);
                                } else {
                                    final byte[] combinedData = new byte[data.length + blob.length];
                                    System.arraycopy(blob, 0, combinedData, 0, blob.length);
                                    System.arraycopy(data, 0, combinedData, blob.length, data.length);
                                    dataConsumer.accept(combinedData);
                                }
                            });
                        } else {
                            dataConsumer.accept(data);
                        }
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
                    wrapper.write(Type.VAR_INT, radius); // radius

                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
                    chunkTracker.setRadius(radius);
                    chunkTracker.setCenter(position.x() >> 4, position.z() >> 4);

                    final PacketWrapper updateViewPosition = wrapper.create(ClientboundPackets1_19_3.UPDATE_VIEW_POSITION);
                    updateViewPosition.write(Type.VAR_INT, position.x() >> 4); // chunk x
                    updateViewPosition.write(Type.VAR_INT, position.z() >> 4); // chunk z
                    updateViewPosition.send(BedrockProtocol.class);

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
                handler(wrapper -> wrapper.user().get(ChunkTracker.class).setRadius(wrapper.get(Type.VAR_INT, 0)));
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
