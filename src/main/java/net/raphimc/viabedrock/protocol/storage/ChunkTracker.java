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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.*;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntImmutablePair;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntPair;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.types.Chunk1_18Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viaversion.util.MathUtil;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.chunk.BedrockChunk;
import net.raphimc.viabedrock.api.chunk.BlockEntityWithBlockState;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockBlockArray;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockDataPalette;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSection;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSectionImpl;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.DimensionIdRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

// TODO: Feature: Block connections
public class ChunkTracker extends StoredObject {

    private static final byte[] FULL_LIGHT = new byte[2048];

    static {
        Arrays.fill(FULL_LIGHT, (byte) 0xFF);
    }

    private final int dimensionId;
    private final int minY;
    private final int worldHeight;
    private final Chunk1_18Type chunkType;

    private final Object chunkLock = new Object();
    private final Map<Long, BedrockChunk> chunks = new HashMap<>();
    private final Set<Long> dirtyChunks = new HashSet<>();

    private final Object subChunkLock = new Object();
    private final Set<SubChunkPosition> subChunkRequests = new HashSet<>();
    private final Set<SubChunkPosition> pendingSubChunks = new HashSet<>();

    private int centerX = 0;
    private int centerZ = 0;
    private int radius = 5;

    public ChunkTracker(final UserConnection user, final int dimensionId) {
        super(user);
        this.dimensionId = dimensionId;

        final GameSessionStorage gameSession = user.get(GameSessionStorage.class);
        final CompoundTag registries = gameSession.getJavaRegistries();
        final String dimensionKey = DimensionIdRewriter.dimensionIdToDimensionKey(this.dimensionId);
        final CompoundTag dimensionRegistry = registries.get("minecraft:dimension_type");
        final ListTag dimensions = dimensionRegistry.get("value");
        final CompoundTag biomeRegistry = registries.get("minecraft:worldgen/biome");
        final ListTag biomes = biomeRegistry.get("value");

        final IntIntPair pair = dimensions.getValue()
                .stream()
                .map(CompoundTag.class::cast)
                .filter(t -> t.get("name").getValue().toString().equals(dimensionKey))
                .findFirst()
                .map(t -> (CompoundTag) t.get("element"))
                .map(t -> new IntIntImmutablePair(((NumberTag) t.get("min_y")).asInt(), ((NumberTag) t.get("height")).asInt()))
                .orElse(null);
        if (pair == null) {
            throw new IllegalStateException("Could not find dimension min_y/height for dimension " + dimensionKey);
        }
        this.minY = pair.keyInt();
        this.worldHeight = pair.valueInt();

        this.chunkType = new Chunk1_18Type(this.worldHeight >> 4, MathUtil.ceilLog2(Protocol1_19_4To1_19_3.MAPPINGS.getBlockStateMappings().mappedSize()), MathUtil.ceilLog2(biomes.size()));
    }

    public void setCenter(final int x, final int z) throws Exception {
        this.centerX = x;
        this.centerZ = z;
        this.removeOutOfViewDistanceChunks();
    }

    public void setRadius(final int radius) throws Exception {
        this.radius = radius;
        this.removeOutOfViewDistanceChunks();
    }

    public BedrockChunk createChunk(final int chunkX, final int chunkZ, final int nonNullSectionCount) {
        if (!this.isInViewDistance(chunkX, chunkZ)) return null;

        final BedrockChunk chunk = new BedrockChunk(chunkX, chunkZ, new BedrockChunkSection[this.worldHeight >> 4]);
        for (int i = 0; i < nonNullSectionCount && i < chunk.getSections().length; i++) {
            chunk.getSections()[i] = new BedrockChunkSectionImpl();
        }
        for (int i = 0; i < chunk.getSections().length; i++) {
            if (chunk.getSections()[i] == null) {
                chunk.getSections()[i] = new BedrockChunkSectionImpl(true);
            }
        }
        synchronized (this.chunkLock) {
            this.chunks.put(this.chunkKey(chunk.getX(), chunk.getZ()), chunk);
        }
        return chunk;
    }

    public void unloadChunk(final int chunkX, final int chunkZ) throws Exception {
        synchronized (this.chunkLock) {
            this.chunks.remove(this.chunkKey(chunkX, chunkZ));
        }

        final PacketWrapper unloadChunk = PacketWrapper.create(ClientboundPackets1_19_4.UNLOAD_CHUNK, this.getUser());
        unloadChunk.write(Type.INT, chunkX); // chunk x
        unloadChunk.write(Type.INT, chunkZ); // chunk z
        unloadChunk.send(BedrockProtocol.class);
    }

    public BedrockChunk getChunk(final int chunkX, final int chunkZ) {
        if (!this.isInViewDistance(chunkX, chunkZ)) return null;

        synchronized (this.chunkLock) {
            return this.chunks.get(this.chunkKey(chunkX, chunkZ));
        }
    }

    public BedrockChunkSection getChunkSection(final int chunkX, final int subChunkY, final int chunkZ) {
        final BedrockChunk chunk = this.getChunk(chunkX, chunkZ);
        if (chunk == null) return null;

        final int sectionIndex = subChunkY + Math.abs(this.minY >> 4);
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) return null;

        return chunk.getSections()[sectionIndex];
    }

    public BedrockChunkSection getChunkSection(final Position blockPosition) {
        return this.getChunkSection(blockPosition.x() >> 4, blockPosition.y() >> 4, blockPosition.z() >> 4);
    }

    public int getBlockState(final Position blockPosition) {
        return this.getBlockState(0, blockPosition);
    }

    public int getBlockState(final int layer, final Position blockPosition) {
        final BedrockChunkSection chunkSection = this.getChunkSection(blockPosition);
        if (chunkSection == null) return this.airId();
        if (chunkSection.palettesCount(PaletteType.BLOCKS) <= layer) return this.airId();

        return chunkSection.palettes(PaletteType.BLOCKS).get(layer).idAt(blockPosition.x() & 15, blockPosition.y() & 15, blockPosition.z() & 15);
    }

    public int getJavaBlockState(final Position blockPosition) {
        final BedrockChunkSection chunkSection = this.getChunkSection(blockPosition);
        if (chunkSection == null) return 0;

        final int sectionX = blockPosition.x() & 15;
        final int sectionY = blockPosition.y() & 15;
        final int sectionZ = blockPosition.z() & 15;

        return this.getJavaBlockState(chunkSection, sectionX, sectionY, sectionZ);
    }

    public int getJavaBlockState(final BedrockChunkSection section, final int sectionX, final int sectionY, final int sectionZ) {
        final BlockStateRewriter blockStateRewriter = this.getUser().get(BlockStateRewriter.class);
        final List<DataPalette> blockPalettes = section.palettes(PaletteType.BLOCKS);

        final int layer0BlockState = blockPalettes.get(0).idAt(sectionX, sectionY, sectionZ);
        int remappedBlockState = blockStateRewriter.javaId(layer0BlockState);
        if (remappedBlockState == -1) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + layer0BlockState);
            remappedBlockState = 0;
        }

        if (blockPalettes.size() > 1) {
            final int layer1BlockState = blockPalettes.get(1).idAt(sectionX, sectionY, sectionZ);
            if (BlockStateRewriter.TAG_WATER.equals(blockStateRewriter.tag(layer1BlockState))) { // Waterlogging
                final int prevBlockState = remappedBlockState;
                remappedBlockState = blockStateRewriter.waterlog(remappedBlockState);
                if (remappedBlockState == -1) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing waterlogged block state: " + prevBlockState);
                    remappedBlockState = prevBlockState;
                }
            }
        }

        return remappedBlockState;
    }

    public BedrockBlockEntity getBlockEntity(final Position blockPosition) {
        final BedrockChunk chunk = this.getChunk(blockPosition.x() >> 4, blockPosition.z() >> 4);
        if (chunk == null) return null;

        for (BlockEntity blockEntity : chunk.blockEntities()) {
            final BedrockBlockEntity bedrockBlockEntity = (BedrockBlockEntity) blockEntity;
            if (bedrockBlockEntity.position().equals(blockPosition)) {
                return bedrockBlockEntity;
            }
        }

        return null;
    }

    public void addBlockEntity(final BedrockBlockEntity bedrockBlockEntity) {
        final BedrockChunk chunk = this.getChunk(bedrockBlockEntity.position().x() >> 4, bedrockBlockEntity.position().z() >> 4);
        if (chunk == null) return;

        chunk.removeBlockEntityAt(bedrockBlockEntity.position());
        chunk.blockEntities().add(bedrockBlockEntity);
    }

    public boolean isChunkLoaded(final int chunkX, final int chunkZ) {
        if (!this.isInViewDistance(chunkX, chunkZ)) return false;

        synchronized (this.chunkLock) {
            return this.chunks.containsKey(this.chunkKey(chunkX, chunkZ));
        }
    }

    public boolean isInUnloadedChunkSection(final Position3f playerPosition) {
        final Position chunkSectionPosition = new Position((int) Math.floor(playerPosition.x() / 16), (int) Math.floor((playerPosition.y() - 1.62F) / 16), (int) Math.floor(playerPosition.z() / 16));
        if (!this.isChunkLoaded(chunkSectionPosition.x(), chunkSectionPosition.z())) {
            return true;
        }
        final BedrockChunkSection chunkSection = this.getChunkSection(chunkSectionPosition.x(), chunkSectionPosition.y(), chunkSectionPosition.z());
        if (chunkSection == null) {
            return false;
        }
        if (chunkSection.hasPendingBlockUpdates()) {
            return true;
        }
        synchronized (this.dirtyChunks) {
            return this.dirtyChunks.contains(this.chunkKey(chunkSectionPosition.x(), chunkSectionPosition.z()));
        }
    }

    public boolean isInViewDistance(final int chunkX, final int chunkZ) {
        return Math.abs(chunkX - this.centerX) <= this.radius && Math.abs(chunkZ - this.centerZ) <= this.radius;
    }

    public void removeOutOfViewDistanceChunks() throws Exception {
        final Set<Long> chunksToRemove = new HashSet<>();
        synchronized (this.chunkLock) {
            for (long chunkKey : this.chunks.keySet()) {
                final int chunkX = (int) (chunkKey >> 32);
                final int chunkZ = (int) chunkKey;
                if (this.isInViewDistance(chunkX, chunkZ)) continue;

                chunksToRemove.add(chunkKey);
            }
        }
        for (long chunkKey : chunksToRemove) {
            final int chunkX = (int) (chunkKey >> 32);
            final int chunkZ = (int) chunkKey;
            this.unloadChunk(chunkX, chunkZ);
        }
    }

    public void requestSubChunks(final int chunkX, final int chunkZ, final int from, final int to) {
        for (int i = from; i < to; i++) {
            this.requestSubChunk(chunkX, i, chunkZ);
        }
    }

    public void requestSubChunk(final int chunkX, final int subChunkY, final int chunkZ) {
        if (!this.isInViewDistance(chunkX, chunkZ)) return;

        synchronized (this.subChunkLock) {
            this.subChunkRequests.add(new SubChunkPosition(chunkX, subChunkY, chunkZ));
        }
    }

    public boolean mergeSubChunk(final int chunkX, final int subChunkY, final int chunkZ, final BedrockChunkSection other, final List<BedrockBlockEntity> blockEntities) {
        if (!this.isInViewDistance(chunkX, chunkZ)) return false;

        final SubChunkPosition position = new SubChunkPosition(chunkX, subChunkY, chunkZ);
        synchronized (this.subChunkLock) {
            if (!this.pendingSubChunks.contains(position)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received sub chunk that was not requested: " + position);
                return false;
            }
            this.pendingSubChunks.remove(position);
        }

        final BedrockChunk chunk = this.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received sub chunk for unloaded chunk: " + position);
            return false;
        }

        final BedrockChunkSection section = chunk.getSections()[subChunkY + Math.abs(this.minY >> 4)];
        section.mergeWith(this.handleBlockPalette(other));
        section.applyPendingBlockUpdates(this.airId());
        blockEntities.forEach(blockEntity -> chunk.removeBlockEntityAt(blockEntity.position()));
        chunk.blockEntities().addAll(blockEntities);

        return true;
    }

    public int handleBlockChange(final Position blockPosition, final int layer, final int blockState) {
        final BedrockChunkSection section = this.getChunkSection(blockPosition);
        if (section == null) {
            return -1;
        }

        final int sectionX = blockPosition.x() & 15;
        final int sectionY = blockPosition.y() & 15;
        final int sectionZ = blockPosition.z() & 15;

        if (section.hasPendingBlockUpdates()) {
            section.addPendingBlockUpdate(sectionX, sectionY, sectionZ, layer, blockState);
            return -1;
        }

        while (section.palettesCount(PaletteType.BLOCKS) <= layer) {
            final BedrockDataPalette palette = new BedrockDataPalette();
            palette.addId(this.airId());
            section.addPalette(PaletteType.BLOCKS, palette);
        }
        section.palettes(PaletteType.BLOCKS).get(layer).setIdAt(sectionX, sectionY, sectionZ, blockState);
        this.getChunk(blockPosition.x() >> 4, blockPosition.z() >> 4).removeBlockEntityAt(blockPosition);

        return this.getJavaBlockState(section, sectionX, sectionY, sectionZ);
    }

    public BedrockChunkSection handleBlockPalette(final BedrockChunkSection section) {
        this.replaceLegacyBlocks(section);
        this.resolveTagPalette(section);
        return section;
    }

    public void sendChunkInNextTick(final int chunkX, final int chunkZ) {
        synchronized (this.dirtyChunks) {
            this.dirtyChunks.add(this.chunkKey(chunkX, chunkZ));
        }
    }

    public void sendChunk(final int chunkX, final int chunkZ) throws Exception {
        final Chunk chunk = this.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            return;
        }
        final Chunk remappedChunk = this.remapChunk(chunk);

        final PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_19_4.CHUNK_DATA, this.getUser());
        final BitSet lightMask = new BitSet();
        lightMask.set(0, remappedChunk.getSections().length + 2);
        wrapper.write(this.chunkType, remappedChunk); // chunk
        wrapper.write(Type.LONG_ARRAY_PRIMITIVE, lightMask.toLongArray()); // sky light mask
        wrapper.write(Type.LONG_ARRAY_PRIMITIVE, new long[0]); // block light mask
        wrapper.write(Type.LONG_ARRAY_PRIMITIVE, new long[0]); // empty sky light mask
        wrapper.write(Type.LONG_ARRAY_PRIMITIVE, lightMask.toLongArray()); // empty block light mask
        wrapper.write(Type.VAR_INT, remappedChunk.getSections().length + 2); // sky light length
        for (int i = 0; i < remappedChunk.getSections().length + 2; i++) {
            wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, FULL_LIGHT); // sky light
        }
        wrapper.write(Type.VAR_INT, 0); // block light length
        wrapper.send(BedrockProtocol.class);
    }

    public int getDimensionId() {
        return this.dimensionId;
    }

    public int getMinY() {
        return this.minY;
    }

    public int getMaxY() {
        return this.worldHeight - Math.abs(this.minY);
    }

    public int getWorldHeight() {
        return this.worldHeight;
    }

    public int airId() {
        return this.getUser().get(BlockStateRewriter.class).bedrockId(BedrockBlockState.AIR);
    }

    public void tick() throws Exception {
        synchronized (this.dirtyChunks) {
            if (!this.dirtyChunks.isEmpty()) {
                this.getUser().getChannel().eventLoop().submit(() -> {
                    synchronized (this.dirtyChunks) {
                        for (Long dirtyChunk : this.dirtyChunks) {
                            final int chunkX = (int) (dirtyChunk >> 32);
                            final int chunkZ = dirtyChunk.intValue();

                            try {
                                this.sendChunk(chunkX, chunkZ);
                            } catch (Throwable e) {
                                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to send chunk " + chunkX + ", " + chunkZ, e);
                            }
                        }
                        this.dirtyChunks.clear();
                    }
                });
            }
        }

        if (this.getUser().get(EntityTracker.class) == null || !this.getUser().get(EntityTracker.class).getClientPlayer().isInitiallySpawned()) return;

        synchronized (this.subChunkLock) {
            this.subChunkRequests.removeIf(s -> !this.isInViewDistance(s.chunkX, s.chunkZ));

            final Position basePosition = new Position(this.centerX, 0, this.centerZ);
            while (!this.subChunkRequests.isEmpty()) {
                final Set<SubChunkPosition> group = this.subChunkRequests.stream().limit(256).collect(Collectors.toSet());
                this.subChunkRequests.removeAll(group);

                final PacketWrapper subChunkRequest = PacketWrapper.create(ServerboundBedrockPackets.SUB_CHUNK_REQUEST, this.getUser());
                subChunkRequest.write(BedrockTypes.VAR_INT, this.dimensionId); // dimension id
                subChunkRequest.write(BedrockTypes.POSITION_3I, basePosition); // base position
                subChunkRequest.write(BedrockTypes.INT_LE, group.size()); // sub chunk offset count
                for (SubChunkPosition subChunkPosition : group) {
                    this.pendingSubChunks.add(subChunkPosition);
                    final Position offset = new Position(subChunkPosition.chunkX - basePosition.x(), subChunkPosition.subChunkY, subChunkPosition.chunkZ - basePosition.z());
                    subChunkRequest.write(BedrockTypes.SUB_CHUNK_OFFSET, offset); // offset
                }
                subChunkRequest.sendToServer(BedrockProtocol.class);
            }
        }
    }

    private long chunkKey(final int chunkX, final int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private Chunk remapChunk(final Chunk chunk) {
        final BlockStateRewriter blockStateRewriter = this.getUser().get(BlockStateRewriter.class);
        final int airId = this.airId();

        final Chunk remappedChunk = new Chunk1_18(chunk.getX(), chunk.getZ(), new ChunkSection[chunk.getSections().length], new CompoundTag(), new ArrayList<>());
        final ChunkSection[] sections = chunk.getSections();
        final ChunkSection[] remappedSections = remappedChunk.getSections();
        for (int idx = 0; idx < sections.length; idx++) {
            final ChunkSection section = sections[idx];

            final ChunkSection remappedSection = remappedSections[idx] = new ChunkSectionImpl(false);
            remappedSection.addPalette(PaletteType.BIOMES, new DataPaletteImpl(ChunkSection.BIOME_SIZE));
            remappedSection.palette(PaletteType.BLOCKS).addId(0);
            remappedSection.palette(PaletteType.BIOMES).addId(0);

            if (section instanceof BedrockChunkSection) {
                final BedrockChunkSection bedrockSection = (BedrockChunkSection) section;
                final List<DataPalette> blockPalettes = bedrockSection.palettes(PaletteType.BLOCKS);
                final DataPalette remappedBlockPalette = remappedSection.palette(PaletteType.BLOCKS);

                if (blockPalettes.size() > 0) {
                    final DataPalette layer0 = blockPalettes.get(0);
                    if (layer0.size() == 1) {
                        final int blockState = layer0.idByIndex(0);
                        int remappedBlockState = blockStateRewriter.javaId(blockState);
                        if (remappedBlockState == -1) {
                            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + blockState);
                            remappedBlockState = 0;
                        }
                        remappedBlockPalette.setIdByIndex(0, remappedBlockState);
                        if (remappedBlockState != 0) {
                            remappedSection.setNonAirBlocksCount(16 * 16 * 16);
                        }
                    } else {
                        int nonAirBlocks = 0;
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 16; y++) {
                                for (int z = 0; z < 16; z++) {
                                    final int blockState = layer0.idAt(x, y, z);
                                    if (blockState == airId) continue;

                                    int remappedBlockState = blockStateRewriter.javaId(blockState);
                                    if (remappedBlockState == -1) {
                                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + blockState);
                                        remappedBlockState = 0;
                                    }
                                    remappedBlockPalette.setIdAt(x, y, z, remappedBlockState);
                                    if (remappedBlockState != 0) {
                                        nonAirBlocks++;
                                    }
                                }
                            }
                        }
                        remappedSection.setNonAirBlocksCount(nonAirBlocks);
                    }
                }
                if (blockPalettes.size() > 1) {
                    final DataPalette layer0 = blockPalettes.get(0);
                    final DataPalette layer1 = blockPalettes.get(1);
                    if (layer1.size() == 1 && layer1.idByIndex(0) == airId) continue;

                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                final int prevBlockState = layer0.idAt(x, y, z);
                                if (prevBlockState == airId) continue;
                                final int blockState = layer1.idAt(x, y, z);
                                if (blockState == airId) continue;
                                final int javaBlockState = remappedBlockPalette.idAt(x, y, z);

                                if (BlockStateRewriter.TAG_WATER.equals(blockStateRewriter.tag(blockState))) { // Waterlogging
                                    final int remappedBlockState = blockStateRewriter.waterlog(javaBlockState);
                                    if (remappedBlockState == -1) {
                                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing waterlogged block state: " + prevBlockState);
                                    } else {
                                        remappedBlockPalette.setIdAt(x, y, z, remappedBlockState);
                                    }
                                }
                            }
                        }
                    }
                }

                final DataPalette biomePalette = bedrockSection.palette(PaletteType.BIOMES);
                final DataPalette remappedBiomePalette = remappedSection.palette(PaletteType.BIOMES);
                if (biomePalette != null) {
                    if (biomePalette.size() == 1) {
                        final int biomeId = biomePalette.idByIndex(0);
                        int remappedBiomeId = biomeId + 1;
                        if (!BedrockProtocol.MAPPINGS.getBedrockBiomes().inverse().containsKey(biomeId)) {
                            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing biome: " + biomeId);
                            remappedBiomeId = 0;
                        }
                        remappedBiomePalette.setIdByIndex(0, remappedBiomeId);
                    } else {
                        for (int x = 0; x < 4; x++) {
                            for (int y = 0; y < 4; y++) {
                                for (int z = 0; z < 4; z++) {
                                    final Int2IntMap subBiomes = new Int2IntOpenHashMap();
                                    int maxBiomeId = -1;
                                    int maxValue = -1;
                                    for (int subX = 0; subX < 4; subX++) {
                                        for (int subY = 0; subY < 4; subY++) {
                                            for (int subZ = 0; subZ < 4; subZ++) {
                                                final int biomeId = biomePalette.idAt(x * 4 + subX, y * 4 + subY, z * 4 + subZ);
                                                final int value = subBiomes.getOrDefault(biomeId, 0) + 1;
                                                subBiomes.put(biomeId, value);
                                                if (value > maxValue) {
                                                    maxBiomeId = biomeId;
                                                    maxValue = value;
                                                }
                                            }
                                        }
                                    }
                                    final int biomeId = maxBiomeId;
                                    int remappedBiomeId = biomeId + 1;
                                    if (!BedrockProtocol.MAPPINGS.getBedrockBiomes().inverse().containsKey(biomeId)) {
                                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing biome: " + biomeId);
                                        remappedBiomeId = 0;
                                    }
                                    remappedBiomePalette.setIdAt(x, y, z, remappedBiomeId);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (BlockEntity blockEntity : chunk.blockEntities()) {
            final BedrockBlockEntity bedrockBlockEntity = (BedrockBlockEntity) blockEntity;
            final BlockEntity javaBlockEntity = BlockEntityRewriter.toJava(this.getUser(), this.getBlockState(bedrockBlockEntity.position()), bedrockBlockEntity);
            if (javaBlockEntity instanceof BlockEntityWithBlockState) {
                final BlockEntityWithBlockState blockEntityWithBlockState = (BlockEntityWithBlockState) javaBlockEntity;
                if (blockEntityWithBlockState.hasBlockState()) {
                    final int sectionIndex = (blockEntityWithBlockState.y() >> 4) + Math.abs(this.minY >> 4);
                    if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) continue;

                    remappedChunk.getSections()[sectionIndex].palette(PaletteType.BLOCKS).setIdAt(blockEntityWithBlockState.sectionX(), blockEntityWithBlockState.y() & 15, blockEntityWithBlockState.sectionZ(), blockEntityWithBlockState.blockState());
                }
            }

            if (javaBlockEntity != null && javaBlockEntity.tag() != null) {
                remappedChunk.blockEntities().add(javaBlockEntity);
            }
        }

        // TODO: Enhancement: Heightmap
        // TODO: Enhancement: Lighting

        return remappedChunk;
    }

    private void resolveTagPalette(final ChunkSection section) {
        final BlockStateRewriter blockStateRewriter = this.getUser().get(BlockStateRewriter.class);

        if (section instanceof BedrockChunkSection) {
            final BedrockChunkSection bedrockSection = (BedrockChunkSection) section;
            final List<DataPalette> palettes = bedrockSection.palettes(PaletteType.BLOCKS);
            for (DataPalette palette : palettes) {
                if (palette instanceof BedrockDataPalette) {
                    final BedrockDataPalette bedrockPalette = (BedrockDataPalette) palette;
                    if (bedrockPalette.hasTagPalette()) {
                        bedrockPalette.addId(this.airId());
                        bedrockPalette.resolveTagPalette(tag -> {
                            int remappedBlockState = blockStateRewriter.bedrockId((CompoundTag) tag);
                            if (remappedBlockState == -1) {
                                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + tag);
                                remappedBlockState = blockStateRewriter.bedrockId(BedrockBlockState.INFO_UPDATE);
                            }
                            return remappedBlockState;
                        });
                    }
                }
            }
        }
    }

    private void replaceLegacyBlocks(final ChunkSection section) {
        final BlockStateRewriter blockStateRewriter = this.getUser().get(BlockStateRewriter.class);

        if (section instanceof BedrockChunkSection) {
            final BedrockChunkSection bedrockSection = (BedrockChunkSection) section;
            final List<DataPalette> palettes = bedrockSection.palettes(PaletteType.BLOCKS);
            for (DataPalette palette : palettes) {
                if (palette instanceof BedrockBlockArray) {
                    final BedrockBlockArray blockArray = (BedrockBlockArray) palette;
                    final BedrockDataPalette dataPalette = new BedrockDataPalette();
                    dataPalette.addId(this.airId());
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                final int blockState = blockArray.idAt(x, y, z);
                                if (blockState == 0) continue;
                                int remappedBlockState = blockStateRewriter.bedrockId(blockState);
                                if (remappedBlockState == -1) {
                                    Via.getPlatform().getLogger().log(Level.WARNING, "Missing legacy block state: " + blockState);
                                    remappedBlockState = this.airId();
                                }
                                dataPalette.setIdAt(x, y, z, remappedBlockState);
                            }
                        }
                    }
                    palettes.set(palettes.indexOf(palette), dataPalette);
                }
            }
        }
    }

    private static class SubChunkPosition {
        private final int chunkX;
        private final int subChunkY;
        private final int chunkZ;

        private SubChunkPosition(final int chunkX, final int subChunkY, final int chunkZ) {
            this.chunkX = chunkX;
            this.subChunkY = subChunkY;
            this.chunkZ = chunkZ;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubChunkPosition that = (SubChunkPosition) o;
            return chunkX == that.chunkX && subChunkY == that.subChunkY && chunkZ == that.chunkZ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunkX, subChunkY, chunkZ);
        }

        @Override
        public String toString() {
            return "SubChunkPosition{" +
                    "chunkX=" + chunkX +
                    ", subChunkY=" + subChunkY +
                    ", chunkZ=" + chunkZ +
                    '}';
        }
    }

}
