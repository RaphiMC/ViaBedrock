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
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.Protocol1_19_3To1_19_1;
import com.viaversion.viaversion.util.MathUtil;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockChunk;
import net.raphimc.viabedrock.api.chunk.BedrockDataPalette;
import net.raphimc.viabedrock.api.chunk.section.AnvilChunkSection;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSection;
import net.raphimc.viabedrock.api.chunk.section.MCRegionChunkSection;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.DimensionIdRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

// TODO: Chunk unloading based on world switch / respawn
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

    private final Object subChunkLock = new Object();
    private final Set<SubChunkPosition> subChunkRequests = new HashSet<>();
    private final Set<SubChunkPosition> pendingSubChunks = new HashSet<>();

    private int centerX = 0;
    private int centerZ = 0;
    private int radius = 1;

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
                .map(t -> (CompoundTag) t)
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

        this.chunkType = new Chunk1_18Type(this.worldHeight >> 4, MathUtil.ceilLog2(Protocol1_19_3To1_19_1.MAPPINGS.getBlockStateMappings().mappedSize()), MathUtil.ceilLog2(biomes.size()));
    }

    public void setCenter(final int x, final int z) {
        this.centerX = x;
        this.centerZ = z;
        this.removeOutOfViewDistanceChunks();
    }

    public void setRadius(final int radius) {
        this.radius = radius;
        this.removeOutOfViewDistanceChunks();
    }

    public void storeChunk(final BedrockChunk chunk) {
        if (!this.isInViewDistance(chunk.getX(), chunk.getZ())) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received chunk outside of view distance: " + chunk.getX() + ", " + chunk.getZ());
            return;
        }

        synchronized (this.chunkLock) {
            this.chunks.put(this.chunkKey(chunk.getX(), chunk.getZ()), chunk);
        }
    }

    public boolean mergeChunkSection(final int chunkX, final int subChunkY, final int chunkZ, final ChunkSection section, final List<BlockEntity> blockEntities) {
        if (!this.isInViewDistance(chunkX, chunkZ)) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received sub chunk outside of view distance: " + chunkX + ", " + chunkZ);
            return false;
        }

        final SubChunkPosition position = new SubChunkPosition(chunkX, subChunkY, chunkZ);
        synchronized (this.subChunkLock) {
            if (!this.pendingSubChunks.contains(position)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received sub chunk that was not requested: " + chunkX + ", " + chunkZ);
                return false;
            }
            this.pendingSubChunks.remove(position);
        }

        BedrockChunk chunk = this.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            chunk = new BedrockChunk(chunkX, chunkZ, new ChunkSection[this.worldHeight >> 4], new CompoundTag(), new ArrayList<>());
            this.storeChunk(chunk);
        }

        final int sectionIndex = subChunkY + Math.abs(this.minY >> 4);
        final ChunkSection[] sections = chunk.getSections();
        final ChunkSection previousSection = sections[sectionIndex];
        if (previousSection == null) {
            sections[sectionIndex] = section;
        } else {
            final DataPalette biomePalette = previousSection.palette(PaletteType.BIOMES);
            sections[sectionIndex] = section;
            section.addPalette(PaletteType.BIOMES, biomePalette);
        }

        chunk.blockEntities().addAll(blockEntities);

        return true;
    }

    public void unloadChunk(final int chunkX, final int chunkZ) throws Exception {
        synchronized (this.chunkLock) {
            this.chunks.remove(this.chunkKey(chunkX, chunkZ));
        }

        final PacketWrapper unloadChunk = PacketWrapper.create(ClientboundPackets1_19_3.UNLOAD_CHUNK, this.getUser());
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

    public boolean isChunkLoaded(final int chunkX, final int chunkZ) {
        if (!this.isInViewDistance(chunkX, chunkZ)) return false;

        synchronized (this.chunkLock) {
            return this.chunks.containsKey(this.chunkKey(chunkX, chunkZ));
        }
    }

    public boolean isInViewDistance(final int chunkX, final int chunkZ) {
        return Math.abs(chunkX - this.centerX) <= this.radius && Math.abs(chunkZ - this.centerZ) <= this.radius;
    }

    public void removeOutOfViewDistanceChunks() {
        synchronized (this.chunkLock) {
            if (this.chunks.entrySet().removeIf(entry -> {
                final int chunkX = (int) (entry.getKey() >> 32);
                final int chunkZ = entry.getKey().intValue();
                return !this.isInViewDistance(chunkX, chunkZ);
            })) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Removed out of view distance chunks");
            }
        }
    }

    public void tick() {
        final EntityTracker entityTracker = this.getUser().get(EntityTracker.class);
        if (!entityTracker.getClientPlayer().isInitiallySpawned()) return;

        synchronized (this.subChunkLock) {
            if (this.subChunkRequests.removeIf(s -> !this.isInViewDistance(s.chunkX, s.chunkZ))) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Removed out of view distance sub chunk requests");
            }

            try {
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
            } catch (Throwable e) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to send sub chunk request", e);
            }
        }
    }

    public void requestSubChunks(final int chunkX, final int chunkZ, final int from, final int to) {
        for (int i = from; i < to; i++) {
            this.requestSubChunk(chunkX, i, chunkZ);
        }
    }

    public void requestSubChunk(final int chunkX, final int subChunkY, final int chunkZ) {
        if (!this.isInViewDistance(chunkX, chunkZ)) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Requested sub chunk out of view distance");
            return;
        }

        synchronized (this.subChunkLock) {
            this.subChunkRequests.add(new SubChunkPosition(chunkX, subChunkY, chunkZ));
        }
    }

    public void sendChunk(final int chunkX, final int chunkZ) throws Exception {
        final PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_19_3.CHUNK_DATA, this.getUser());
        this.writeChunk(wrapper, chunkX, chunkZ);
        wrapper.send(BedrockProtocol.class);
    }

    public void writeChunk(final PacketWrapper wrapper, final int chunkX, final int chunkZ) {
        final Chunk chunk;
        synchronized (this.chunkLock) {
            chunk = this.chunks.get(this.chunkKey(chunkX, chunkZ));
        }
        final Chunk remappedChunk = this.remapChunk(chunk);

        final BitSet lightMask = new BitSet();
        lightMask.set(0, remappedChunk.getSections().length + 2);
        wrapper.write(this.chunkType, remappedChunk); // chunk
        wrapper.write(Type.BOOLEAN, true); // trust edges
        wrapper.write(Type.LONG_ARRAY_PRIMITIVE, lightMask.toLongArray()); // sky light mask
        wrapper.write(Type.LONG_ARRAY_PRIMITIVE, new long[0]); // block light mask
        wrapper.write(Type.LONG_ARRAY_PRIMITIVE, new long[0]); // empty sky light mask
        wrapper.write(Type.LONG_ARRAY_PRIMITIVE, lightMask.toLongArray()); // empty block light mask
        wrapper.write(Type.VAR_INT, remappedChunk.getSections().length + 2); // sky light length
        for (int i = 0; i < remappedChunk.getSections().length + 2; i++) {
            wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, FULL_LIGHT); // sky light
        }
        wrapper.write(Type.VAR_INT, 0); // block light length
    }

    private Chunk remapChunk(final Chunk chunk) {
        final BlockStateRewriter blockStateRewriter = this.getUser().get(BlockStateRewriter.class);
        final int airId = blockStateRewriter.air();

        final Chunk remappedChunk = new Chunk1_18(chunk.getX(), chunk.getZ(), new ChunkSection[chunk.getSections().length], new CompoundTag(), new ArrayList<>());
        final ChunkSection[] sections = chunk.getSections();
        final ChunkSection[] remappedSections = remappedChunk.getSections();
        for (int idx = 0; idx < sections.length; idx++) {
            final ChunkSection section = sections[idx];

            final ChunkSection remappedSection = remappedSections[idx] = new ChunkSectionImpl(false);
            remappedSection.addPalette(PaletteType.BIOMES, new DataPaletteImpl(ChunkSection.BIOME_SIZE));
            remappedSection.palette(PaletteType.BLOCKS).addId(0);
            remappedSection.palette(PaletteType.BIOMES).addId(0);

            if (section instanceof MCRegionChunkSection) {
                final MCRegionChunkSection mcRegionSection = (MCRegionChunkSection) section;
                final DataPalette blockPalette = mcRegionSection.palette(PaletteType.BLOCKS);
                final DataPalette remappedBlockPalette = remappedSection.palette(PaletteType.BLOCKS);

                int nonAirBlocks = 0;
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            final int blockState = blockPalette.idAt(x, y, z);
                            final int remappedBlockState = blockState == 0 ? 0 : 1;
                            remappedBlockPalette.setIdAt(x, y, z, remappedBlockState);
                            if (remappedBlockState != 0) {
                                nonAirBlocks++;
                            }
                        }
                    }
                }
                remappedSection.setNonAirBlocksCount(nonAirBlocks);
            } else if (section instanceof AnvilChunkSection) {
                final AnvilChunkSection anvilSection = (AnvilChunkSection) section;
                final List<BedrockDataPalette> blockPalettes = anvilSection.palettes(PaletteType.BLOCKS);
                final DataPalette remappedBlockPalette = remappedSection.palette(PaletteType.BLOCKS);

                if (blockPalettes.size() > 0) {
                    final BedrockDataPalette mainLayer = blockPalettes.get(0);
                    int nonAirBlocks = 0;
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                final int blockState = mainLayer.idAt(x, y, z);

                                if (blockState == airId) { // Fast path for air
                                    remappedBlockPalette.setIdAt(x, y, z, 0);
                                    continue;
                                }

                                int remappedBlockState = blockStateRewriter.javaId(blockState);
                                if (remappedBlockState == -1) {
                                    Via.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + blockState);
                                    remappedBlockState = 1;
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
                for (int i = 1; i < blockPalettes.size(); i++) {
                    final BedrockDataPalette prevLayer = blockPalettes.get(i - 1);
                    final BedrockDataPalette layer = blockPalettes.get(i);
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                final int prevBlockState = prevLayer.idAt(x, y, z);
                                if (prevBlockState == airId) continue;
                                final int blockState = layer.idAt(x, y, z);
                                if (blockState == airId) continue;
                                final int javaBlockState = remappedBlockPalette.idAt(x, y, z);

                                if (blockStateRewriter.isWater(blockState)) { // Waterlogging
                                    final int remappedBlockState = blockStateRewriter.waterlog(javaBlockState);
                                    if (remappedBlockState == -1) {
                                        Via.getPlatform().getLogger().log(Level.WARNING, "Missing waterlogged block state: " + prevBlockState);
                                    } else {
                                        remappedBlockPalette.setIdAt(x, y, z, remappedBlockState);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (section instanceof BedrockChunkSection) {
                final BedrockChunkSection bedrockSection = (BedrockChunkSection) section;
                final DataPalette biomePalette = bedrockSection.palette(PaletteType.BIOMES);
                final DataPalette remappedBiomePalette = remappedSection.palette(PaletteType.BIOMES);
                if (biomePalette != null) {
                    for (int x = 0; x < 4; x++) {
                        for (int y = 0; y < 4; y++) {
                            for (int z = 0; z < 4; z++) {
                                final Int2IntMap subBiomes = new Int2IntOpenHashMap();
                                for (int subX = 0; subX < 4; subX++) {
                                    for (int subY = 0; subY < 4; subY++) {
                                        for (int subZ = 0; subZ < 4; subZ++) {
                                            final int biomeId = biomePalette.idAt(x * 4 + subX, y * 4 + subY, z * 4 + subZ);
                                            subBiomes.put(biomeId, subBiomes.getOrDefault(biomeId, 0) + 1);
                                        }
                                    }
                                }
                                final int biomeId = subBiomes.int2IntEntrySet().stream().max(Comparator.comparingInt(Int2IntMap.Entry::getIntValue)).get().getIntKey();
                                final int remappedBiomeId = 0;
                                remappedBiomePalette.setIdAt(x, y, z, remappedBiomeId);
                            }
                        }
                    }
                }
            }
        }
        // TODO: Block entities
        // TODO: Heightmap
        // TODO: Lighting

        return remappedChunk;
    }

    public int getDimensionId() {
        return this.dimensionId;
    }

    public int getMinY() {
        return this.minY;
    }

    public int getWorldHeight() {
        return this.worldHeight;
    }

    public long chunkKey(final int chunkX, final int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
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

        public double distance(final SubChunkPosition other) {
            return Math.sqrt(Math.pow(this.chunkX - other.chunkX, 2) + Math.pow(this.chunkZ - other.chunkZ, 2));
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
    }

}
