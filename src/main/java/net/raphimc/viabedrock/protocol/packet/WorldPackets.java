/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.*;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.IntObjectPair;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.util.MathUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.chunk.BedrockChunk;
import net.raphimc.viabedrock.api.chunk.BlockEntityWithBlockState;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockBiomeArray;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockDataPalette;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSection;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSectionImpl;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.Dimension;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ServerboundLoadingScreenPacketType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.SpawnPositionType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.SubChunkPacket_HeightMapDataType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.SubChunkPacket_SubChunkRequestResult;
import net.raphimc.viabedrock.protocol.data.enums.java.Relative;
import net.raphimc.viabedrock.protocol.model.BlockChangeEntry;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.array.ByteArrayType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

public class WorldPackets {

    private static final PacketHandler UPDATE_BLOCK_HANDLER = wrapper -> {
        final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
        final BlockPosition position = wrapper.get(Types.BLOCK_POSITION1_14, 0);
        final int blockState = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // block state
        wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // flags
        final int layer = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // layer
        if (layer < 0 || layer > 1) {
            wrapper.cancel();
            return;
        }

        final IntObjectPair<BlockEntity> remappedBlock = chunkTracker.handleBlockChange(position, layer, blockState);
        if (remappedBlock == null) {
            wrapper.cancel();
            return;
        }

        wrapper.write(Types.VAR_INT, remappedBlock.keyInt()); // block state

        if (remappedBlock.value() != null) {
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();
            PacketFactory.sendJavaBlockEntityData(wrapper.user(), position, remappedBlock.value());
        }
    };

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.SET_SPAWN_POSITION, ClientboundPackets1_21_9.SET_DEFAULT_SPAWN_POSITION, wrapper -> {
            final int rawType = wrapper.read(BedrockTypes.VAR_INT); // type
            final SpawnPositionType type = SpawnPositionType.getByValue(rawType);
            if (type == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown SpawnPositionType: " + rawType);
                wrapper.cancel();
                return;
            }
            final BlockPosition compassPosition = wrapper.read(BedrockTypes.BLOCK_POSITION); // compass position
            final Dimension dimension = Dimension.getByValue(wrapper.read(BedrockTypes.VAR_INT)); // dimension
            if (dimension == null) {
                wrapper.cancel();
                return;
            }
            wrapper.read(BedrockTypes.BLOCK_POSITION); // spawn position

            switch (type) {
                case WorldSpawn -> {
                    wrapper.write(Types.GLOBAL_POSITION, new GlobalBlockPosition(dimension.getKey(), compassPosition.x(), compassPosition.y(), compassPosition.z()));
                    wrapper.write(Types.FLOAT, 0F); // yaw
                    wrapper.write(Types.FLOAT, 0F); // pitch
                }
                case PlayerRespawn -> wrapper.cancel();
                default -> throw new IllegalStateException("Unhandled SpawnPositionType: " + type);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CHANGE_DIMENSION, ClientboundPackets1_21_9.RESPAWN, wrapper -> {
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);

            final Dimension dimension = Dimension.values()[wrapper.read(BedrockTypes.VAR_INT)]; // dimension
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            wrapper.read(Types.BOOLEAN); // respawn
            final Long loadingScreenId;
            if (wrapper.read(Types.BOOLEAN)) { // has loading screen id
                loadingScreenId = wrapper.read(BedrockTypes.UNSIGNED_INT_LE); // loading screen id
            } else {
                loadingScreenId = null;
            }

            if (dimension == wrapper.user().get(ChunkTracker.class).getDimension()) {
                // Bedrock client gets stuck in loading terrain until a proper CHANGE_DIMENSION packet is received
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received CHANGE_DIMENSION packet for the same dimension");
            }

            wrapper.user().put(new ChunkTracker(wrapper.user(), dimension));
            final EntityTracker oldEntityTracker = wrapper.user().get(EntityTracker.class);
            final ClientPlayerEntity clientPlayer = oldEntityTracker.getClientPlayer();
            oldEntityTracker.prepareForRespawn();
            final EntityTracker newEntityTracker = new EntityTracker(wrapper.user());
            newEntityTracker.addEntity(clientPlayer);
            wrapper.user().put(newEntityTracker);

            PacketFactory.sendBedrockLoadingScreen(wrapper.user(), ServerboundLoadingScreenPacketType.StartLoadingScreen, loadingScreenId);
            clientPlayer.setPosition(new Position3f(position.x(), position.y() + clientPlayer.eyeOffset(), position.z()));
            clientPlayer.setDimensionChangeInfo(new ClientPlayerEntity.DimensionChangeInfo(loadingScreenId));
            if (inventoryTracker.isContainerOpen()) {
                inventoryTracker.setCurrentContainerClosed(true);
            }
            if (inventoryTracker.getCurrentForm() != null) {
                inventoryTracker.closeCurrentForm();
            }

            wrapper.write(Types.VAR_INT, dimension.ordinal()); // dimension id
            wrapper.write(Types.STRING, dimension.getKey()); // dimension name
            wrapper.write(Types.LONG, 0L); // hashed seed
            wrapper.write(Types.BYTE, (byte) clientPlayer.javaGameMode().ordinal()); // game mode
            wrapper.write(Types.BYTE, (byte) -1); // previous game mode
            wrapper.write(Types.BOOLEAN, false); // is debug
            wrapper.write(Types.BOOLEAN, gameSession.isFlatGenerator()); // is flat
            wrapper.write(Types.OPTIONAL_GLOBAL_POSITION, null); // last death position
            wrapper.write(Types.VAR_INT, 0); // portal cooldown
            wrapper.write(Types.VAR_INT, 64); // sea level
            wrapper.write(Types.BYTE, (byte) 0x03); // keep data mask
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();
            clientPlayer.sendPlayerPositionPacketToClient(Relative.NONE);
            clientPlayer.sendAttribute("minecraft:health"); // Java client always resets health on respawn, but Bedrock client keeps health when switching dimensions
            clientPlayer.sendEffects(); // Java client always resets effects on respawn. Resend them
            clientPlayer.setAbilities(clientPlayer.abilities()); // Java client always resets abilities on respawn. Resend them
            PacketFactory.sendJavaContainerSetContent(wrapper.user(), inventoryTracker.getInventoryContainer()); // Java client always resets inventory on respawn. Resend it
            inventoryTracker.getInventoryContainer().sendSelectedHotbarSlotToClient(); // Java client always resets selected hotbar slot on respawn. Resend it
        });
        protocol.registerClientbound(ClientboundBedrockPackets.LEVEL_CHUNK, null, wrapper -> {
            wrapper.cancel();
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

            final int chunkX = wrapper.read(BedrockTypes.VAR_INT); // chunk x
            final int chunkZ = wrapper.read(BedrockTypes.VAR_INT); // chunk z
            final Dimension dimension = Dimension.getByValue(wrapper.read(BedrockTypes.VAR_INT)); // dimension
            if (dimension != chunkTracker.getDimension()) {
                return;
            }
            final int sectionCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // sub chunk count
            if (sectionCount < -2) { // Bedrock client ignores this packet
                return;
            }

            final int startY = chunkTracker.getMinY() >> 4;
            final int endY = chunkTracker.getMaxY() >> 4;
            int requestSectionCount = 0;
            if (sectionCount == -2) {
                requestSectionCount = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE) + 1; // count
            } else if (sectionCount == -1) {
                requestSectionCount = endY - startY;
            }

            final BedrockChunk previousChunk = chunkTracker.getChunk(chunkX, chunkZ);
            if (previousChunk != null) {
                chunkTracker.unloadChunk(new ChunkPosition(chunkX, chunkZ));
                if (previousChunk.isRequestSubChunks()) {
                    requestSectionCount = endY - startY;
                }
            }

            final BedrockChunk chunk = chunkTracker.createChunk(chunkX, chunkZ, sectionCount < 0 ? requestSectionCount : sectionCount);
            if (chunk == null) {
                return;
            }
            chunk.setRequestSubChunks(sectionCount < 0);

            final int fRequestSectionCount = requestSectionCount;
            final Consumer<byte[]> dataConsumer = combinedData -> {
                try {
                    if (fRequestSectionCount > 0) {
                        chunkTracker.requestSubChunks(chunkX, chunkZ, startY, MathUtil.clamp(startY + fRequestSectionCount, startY + 1, endY));
                    }
                    final ByteBuf dataBuf = Unpooled.wrappedBuffer(combinedData);

                    final BedrockChunkSection[] sections = chunk.getSections();
                    final List<BlockEntity> blockEntities = chunk.blockEntities();
                    try {
                        for (int i = 0; i < sectionCount; i++) {
                            sections[i].mergeWith(chunkTracker.handleBlockPalette(BedrockTypes.CHUNK_SECTION.read(dataBuf))); // chunk section
                            sections[i].applyPendingBlockUpdates(chunkTracker.airId());
                        }
                        if (gameSession.getBedrockVanillaVersion().isLowerThan("1.18.0")) {
                            final byte[] biomeData = new byte[256];
                            dataBuf.readBytes(biomeData);
                            for (ChunkSection section : sections) {
                                section.addPalette(PaletteType.BIOMES, new BedrockBiomeArray(biomeData));
                            }
                        } else {
                            for (int i = 0; i < sections.length; i++) {
                                BedrockDataPalette biomePalette = BedrockTypes.RUNTIME_DATA_PALETTE.read(dataBuf); // biome palette
                                if (biomePalette == null) {
                                    if (i == 0) {
                                        throw new RuntimeException("First biome palette can not point to previous biome palette");
                                    }
                                    biomePalette = ((BedrockDataPalette) sections[i - 1].palette(PaletteType.BIOMES)).clone();
                                }
                                sections[i].addPalette(PaletteType.BIOMES, biomePalette);
                            }
                        }

                        dataBuf.skipBytes(1); // border blocks
                        while (dataBuf.isReadable()) {
                            final Tag tag = BedrockTypes.NETWORK_TAG.read(dataBuf); // block entity tag
                            if (tag instanceof CompoundTag) { // Ignore non-compound tags
                                blockEntities.add(new BedrockBlockEntity((CompoundTag) tag));
                            }
                        }
                    } catch (IndexOutOfBoundsException ignored) {
                        // Bedrock client stops reading at whatever point and loads whatever it has read successfully
                    } catch (Throwable e) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error reading chunk data", e);
                    }

                    if (!chunk.isRequestSubChunks()) {
                        chunkTracker.sendChunk(chunkX, chunkZ);
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Error handling chunk data", e);
                }
            };

            if (wrapper.read(Types.BOOLEAN)) { // caching enabled
                final Long[] blobs = wrapper.read(BedrockTypes.LONG_ARRAY); // blob ids
                final int expectedLength = sectionCount < 0 ? 1 : sectionCount + 1;
                if (blobs.length != expectedLength) { // Bedrock client writes random memory contents into the request and most likely crashes
                    throw new IllegalStateException("Invalid blob count: " + blobs.length + " (expected " + expectedLength + ")");
                }
                final byte[] data = wrapper.read(BedrockTypes.BYTE_ARRAY); // data
                wrapper.user().get(BlobCache.class).getBlob(blobs).thenAccept(blob -> {
                    final byte[] combinedData = new byte[data.length + blob.length];
                    System.arraycopy(blob, 0, combinedData, 0, blob.length);
                    System.arraycopy(data, 0, combinedData, blob.length, data.length);
                    dataConsumer.accept(combinedData);
                });
            } else {
                dataConsumer.accept(wrapper.read(BedrockTypes.BYTE_ARRAY)); // data
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SUB_CHUNK, null, wrapper -> {
            wrapper.cancel();
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);

            final boolean cachingEnabled = wrapper.read(Types.BOOLEAN); // caching enabled
            final Dimension dimension = Dimension.getByValue(wrapper.read(BedrockTypes.VAR_INT)); // dimension
            if (dimension != chunkTracker.getDimension()) {
                return;
            }
            final BlockPosition center = wrapper.read(BedrockTypes.POSITION_3I); // center position
            final long count = wrapper.read(BedrockTypes.UNSIGNED_INT_LE); // count

            for (long i = 0; i < count; i++) {
                final BlockPosition offset = wrapper.read(BedrockTypes.SUB_CHUNK_OFFSET); // offset
                final SubChunkPacket_SubChunkRequestResult result = SubChunkPacket_SubChunkRequestResult.getByValue(wrapper.read(Types.BYTE), SubChunkPacket_SubChunkRequestResult.Undefined); // result
                final byte[] data = result != SubChunkPacket_SubChunkRequestResult.SuccessAllAir || !cachingEnabled ? wrapper.read(BedrockTypes.BYTE_ARRAY) : new byte[0]; // data
                final SubChunkPacket_HeightMapDataType heightmapResult = SubChunkPacket_HeightMapDataType.getByValue(wrapper.read(Types.BYTE), SubChunkPacket_HeightMapDataType.NoData); // heightmap result
                if (heightmapResult == SubChunkPacket_HeightMapDataType.HasData) {
                    wrapper.read(new ByteArrayType(256)); // heightmap data
                }
                final SubChunkPacket_HeightMapDataType renderHeightmapResult = SubChunkPacket_HeightMapDataType.getByValue(wrapper.read(Types.BYTE), SubChunkPacket_HeightMapDataType.NoData); // render heightmap result
                if (renderHeightmapResult == SubChunkPacket_HeightMapDataType.HasData) {
                    wrapper.read(new ByteArrayType(256)); // render heightmap data
                }

                final BlockPosition absolute = new BlockPosition(center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z());
                final Consumer<byte[]> dataConsumer = combinedData -> {
                    try {
                        if (result == SubChunkPacket_SubChunkRequestResult.SuccessAllAir) {
                            if (chunkTracker.mergeSubChunk(absolute.x(), absolute.y(), absolute.z(), new BedrockChunkSectionImpl(), new ArrayList<>())) {
                                chunkTracker.sendChunkInNextTick(absolute.x(), absolute.z());
                            }
                        } else if (result == SubChunkPacket_SubChunkRequestResult.Success) {
                            final ByteBuf dataBuf = Unpooled.wrappedBuffer(combinedData);

                            BedrockChunkSection section = new BedrockChunkSectionImpl();
                            final List<BedrockBlockEntity> blockEntities = new ArrayList<>();
                            try {
                                section = BedrockTypes.CHUNK_SECTION.read(dataBuf); // chunk section
                                while (dataBuf.isReadable()) {
                                    final Tag tag = BedrockTypes.NETWORK_TAG.read(dataBuf); // block entity tag
                                    if (tag instanceof CompoundTag) { // Ignore non-compound tags
                                        blockEntities.add(new BedrockBlockEntity((CompoundTag) tag));
                                    }
                                }
                            } catch (IndexOutOfBoundsException ignored) {
                                // Bedrock client stops reading at whatever point and loads whatever it has read successfully
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
                    wrapper.user().get(BlobCache.class).getBlob(hash).thenAccept(blob -> {
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
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_BLOCK, ClientboundPackets1_21_9.BLOCK_UPDATE, new PacketHandlers() {
            @Override
            protected void register() {
                map(BedrockTypes.BLOCK_POSITION, Types.BLOCK_POSITION1_14); // position
                handler(UPDATE_BLOCK_HANDLER);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_BLOCK_SYNCED, ClientboundPackets1_21_9.BLOCK_UPDATE, new PacketHandlers() {
            @Override
            protected void register() {
                map(BedrockTypes.BLOCK_POSITION, Types.BLOCK_POSITION1_14); // position
                handler(UPDATE_BLOCK_HANDLER);
                read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
                read(BedrockTypes.UNSIGNED_VAR_LONG); // block sync type
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_SUB_CHUNK_BLOCKS, null, wrapper -> {
            wrapper.cancel(); // Need multiple packets because offsets can go over chunk boundaries
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            wrapper.read(BedrockTypes.BLOCK_POSITION); // position | Seems to be unused by the Bedrock client
            final BlockChangeEntry[][] blockUpdatesArray = new BlockChangeEntry[2][];
            blockUpdatesArray[0] = wrapper.read(BedrockTypes.BLOCK_CHANGE_ENTRY_ARRAY); // standard blocks
            blockUpdatesArray[1] = wrapper.read(BedrockTypes.BLOCK_CHANGE_ENTRY_ARRAY); // extra blocks

            final Map<BlockPosition, List<BlockChangeRecord>> blockChanges = new HashMap<>();
            final Map<BlockPosition, BlockEntity> blockEntities = new HashMap<>();
            for (int layer = 0; layer < blockUpdatesArray.length; layer++) {
                for (BlockChangeEntry entry : blockUpdatesArray[layer]) {
                    final IntObjectPair<BlockEntity> remappedBlock = chunkTracker.handleBlockChange(entry.position(), layer, entry.blockState());
                    if (remappedBlock == null) {
                        continue;
                    }
                    if (remappedBlock.value() != null) {
                        blockEntities.put(entry.position(), remappedBlock.value());
                    }

                    final BlockPosition chunkPosition = new BlockPosition(entry.position().x() >> 4, entry.position().y() >> 4, entry.position().z() >> 4);
                    final BlockPosition relative = new BlockPosition(entry.position().x() & 0xF, entry.position().y() & 0xF, entry.position().z() & 0xF);
                    blockChanges.computeIfAbsent(chunkPosition, k -> new ArrayList<>()).add(new BlockChangeRecord1_16_2(relative.x(), relative.y(), relative.z(), remappedBlock.keyInt()));
                }
            }

            for (Map.Entry<BlockPosition, List<BlockChangeRecord>> entry : blockChanges.entrySet()) {
                final BlockPosition chunkPosition = entry.getKey();
                final List<BlockChangeRecord> changes = entry.getValue();
                final long chunkKey = (chunkPosition.x() & 0x3FFFFFL) << 42 | (chunkPosition.z() & 0x3FFFFFL) << 20 | (chunkPosition.y() & 0xFFFL);

                final PacketWrapper multiBlockChange = wrapper.create(ClientboundPackets1_21_9.SECTION_BLOCKS_UPDATE);
                multiBlockChange.write(Types.LONG, chunkKey); // chunk position
                multiBlockChange.write(Types.VAR_LONG_BLOCK_CHANGE_ARRAY, changes.toArray(new BlockChangeRecord[0])); // block change records
                multiBlockChange.send(BedrockProtocol.class);
            }
            for (Map.Entry<BlockPosition, BlockEntity> entry : blockEntities.entrySet()) {
                PacketFactory.sendJavaBlockEntityData(wrapper.user(), entry.getKey(), entry.getValue());
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.BLOCK_ENTITY_DATA, ClientboundPackets1_21_9.BLOCK_ENTITY_DATA, new PacketHandlers() {
            @Override
            protected void register() {
                map(BedrockTypes.BLOCK_POSITION, Types.BLOCK_POSITION1_14); // position
                handler(wrapper -> {
                    final Tag tag = wrapper.read(BedrockTypes.NETWORK_TAG); // block entity tag
                    if (!(tag instanceof CompoundTag)) {
                        wrapper.cancel();
                        return;
                    }

                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
                    final BedrockBlockEntity bedrockBlockEntity = new BedrockBlockEntity(wrapper.get(Types.BLOCK_POSITION1_14, 0), (CompoundTag) tag);
                    chunkTracker.addBlockEntity(bedrockBlockEntity);

                    final BlockEntity javaBlockEntity = BlockEntityRewriter.toJava(wrapper.user(), chunkTracker.getBlockState(bedrockBlockEntity.position()), bedrockBlockEntity);
                    if (javaBlockEntity instanceof BlockEntityWithBlockState blockEntityWithBlockState) {
                        PacketFactory.sendJavaBlockUpdate(wrapper.user(), bedrockBlockEntity.position(), blockEntityWithBlockState.blockState());
                    }

                    if (javaBlockEntity != null && javaBlockEntity.tag() != null) {
                        wrapper.write(Types.VAR_INT, javaBlockEntity.typeId()); // type
                        wrapper.write(Types.COMPOUND_TAG, javaBlockEntity.tag()); // block entity tag
                    } else {
                        wrapper.cancel();
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.NETWORK_CHUNK_PUBLISHER_UPDATE, ClientboundPackets1_21_9.SET_CHUNK_CACHE_RADIUS, wrapper -> {
            final BlockPosition position = wrapper.read(BedrockTypes.POSITION_3I); // center position
            final int radius = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT) >> 4; // radius
            wrapper.write(Types.VAR_INT, radius); // radius

            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            chunkTracker.setRadius(radius);
            chunkTracker.setCenter(position.x() >> 4, position.z() >> 4);

            final PacketWrapper updateViewPosition = wrapper.create(ClientboundPackets1_21_9.SET_CHUNK_CACHE_CENTER);
            updateViewPosition.write(Types.VAR_INT, position.x() >> 4); // chunk x
            updateViewPosition.write(Types.VAR_INT, position.z() >> 4); // chunk z
            updateViewPosition.send(BedrockProtocol.class);

            final int count = wrapper.read(BedrockTypes.INT_LE); // server built chunks count
            for (int i = 0; i < count; i++) {
                wrapper.read(BedrockTypes.VAR_INT); // chunk x
                wrapper.read(BedrockTypes.VAR_INT); // chunk z
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CHUNK_RADIUS_UPDATED, ClientboundPackets1_21_9.SET_CHUNK_CACHE_RADIUS, new PacketHandlers() {
            @Override
            public void register() {
                map(BedrockTypes.VAR_INT, Types.VAR_INT); // radius
                handler(wrapper -> wrapper.user().get(ChunkTracker.class).setRadius(wrapper.get(Types.VAR_INT, 0)));
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_TIME, ClientboundPackets1_21_9.SET_TIME, wrapper -> {
            wrapper.write(Types.LONG, wrapper.user().get(GameSessionStorage.class).getLevelTime()); // level time
            final long bedrockTime = wrapper.read(BedrockTypes.VAR_INT); // time of day
            wrapper.write(Types.LONG, bedrockTime >= 0 ? bedrockTime % 24000L : 24000 + (bedrockTime % 24000L)); // time of day
            wrapper.write(Types.BOOLEAN, wrapper.user().get(GameRulesStorage.class).<Boolean>getGameRule("doDayLightCycle")); // do day light cycle
        });
    }

}
