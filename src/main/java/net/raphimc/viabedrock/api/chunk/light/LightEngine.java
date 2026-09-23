/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.api.chunk.light;

import java.util.Arrays;

/**
 * Computes Minecraft style sky and block light for a chunk using the block data of the chunk and
 * its eight neighbors. The propagation algorithm is a breadth first flood fill, based on the light
 * engine of Minestom (https://github.com/Minestom/Minestom, Apache License 2.0).
 * <p>
 * Light behaves like on the Java Edition client: light entering a block loses a level of
 * {@code max(1, opacity)} of that block. Sky light of level 15 travels downwards without losing
 * level as long as it does not enter a light filtering block. Blocks with an opacity of 15 or
 * above fully block light.
 */
public final class LightEngine {

    public static final int LIGHT_LENGTH = 2048;

    private static final int REGION_SIZE = 48; // 3x3 chunks
    private static final int REGION_SIZE_SQUARED = REGION_SIZE * REGION_SIZE;
    private static final int MAX_HEIGHT = 1 << 12;
    private static final int[][] DIRECTIONS = {{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
    private static final int DOWN = 2; // Index of the {0, -1, 0} direction in DIRECTIONS

    private LightEngine() {
    }

    /**
     * Computes the light of all chunks of a 3x3 chunk region. The lighting needs of every chunk in
     * the region are fully covered by the region, as light travels at most 15 blocks.
     *
     * @param regionStates  The java block states of the region, indexed as
     *                      {@code [chunkIndex][sectionIndex][blockIndex]} where
     *                      {@code chunkIndex = (chunkZOffset + 1) * 3 + (chunkXOffset + 1)} and
     *                      {@code blockIndex = y * 256 | z * 16 | x}. Chunks and sections that have
     *                      not been loaded are {@code null} and are treated as fully opaque.
     * @param sectionCount  The amount of chunk sections of a chunk column
     * @param skyLight      Whether the dimension has sky light
     * @param emissionTable Light emission (0-15) per java block state id
     * @param opacityTable  Light opacity (0-15) per java block state id
     * @return The light data of all loaded chunks of the region; {@code null} for unloaded chunks
     */
    public static ChunkLight[] computeRegionLight(final int[][][] regionStates, final int sectionCount, final boolean skyLight, final byte[] emissionTable, final byte[] opacityTable) {
        if (regionStates.length != 9) throw new IllegalArgumentException("Region must contain exactly 9 chunks");
        if (sectionCount << 4 > MAX_HEIGHT) throw new IllegalArgumentException("World height exceeds light engine limit");

        final int height = sectionCount << 4;
        // Block index scheme: (y * REGION_SIZE + z) * REGION_SIZE + x
        final byte[] opacity = new byte[REGION_SIZE * REGION_SIZE * height];
        final byte[] emission = new byte[opacity.length];
        Arrays.fill(opacity, (byte) 15); // Unloaded chunks are treated as fully opaque
        fillRegion(regionStates, opacity, emission, emissionTable, opacityTable, height);

        final byte[] skyLightData = skyLight ? new byte[opacity.length] : null;
        final byte[] blockLightData = new byte[opacity.length];

        final IntQueue queue = new IntQueue();
        if (skyLightData != null) {
            final short[] skyCutoff = new short[REGION_SIZE * REGION_SIZE]; // Topmost block of each column that is not fully sky lit
            seedSkyLight(skyLightData, opacity, skyCutoff, height);
            enqueueSkyLightFrontier(skyLightData, opacity, skyCutoff, queue, height);
            propagate(skyLightData, opacity, queue, height, true);
            queue.clear();
        }
        seedBlockLight(blockLightData, emission, queue, height);
        propagate(blockLightData, opacity, queue, height, false);

        final ChunkLight[] results = new ChunkLight[9];
        for (int chunkIndex = 0; chunkIndex < 9; chunkIndex++) {
            if (regionStates[chunkIndex] == null) continue;

            final ChunkLight light = results[chunkIndex] = new ChunkLight(sectionCount, skyLight);
            final int baseX = (chunkIndex % 3) * 16;
            final int baseZ = (chunkIndex / 3) * 16;
            for (int sectionIndex = 0; sectionIndex < sectionCount; sectionIndex++) {
                if (skyLightData != null) {
                    light.setSkyLight(sectionIndex + 1, extractSection(skyLightData, baseX, baseZ, sectionIndex, height));
                }
                light.setBlockLight(sectionIndex + 1, extractSection(blockLightData, baseX, baseZ, sectionIndex, height));
            }
        }
        return results;
    }

    /**
     * Fills the opacity and emission arrays of the 3x3 chunk region from the java block states.
     */
    private static void fillRegion(final int[][][] regionStates, final byte[] opacity, final byte[] emission, final byte[] emissionTable, final byte[] opacityTable, final int height) {
        for (int chunkIndex = 0; chunkIndex < 9; chunkIndex++) {
            final int[][] chunkStates = regionStates[chunkIndex];
            if (chunkStates == null) continue;

            final int baseX = (chunkIndex % 3) << 4;
            final int baseZ = (chunkIndex / 3) << 4;
            for (int sectionIndex = 0; sectionIndex < chunkStates.length; sectionIndex++) {
                final int[] states = chunkStates[sectionIndex];
                if (states == null) continue;

                final int javaId0 = states[0];
                boolean singleState = true;
                for (int i = 1; i < states.length; i++) {
                    if (states[i] != javaId0) {
                        singleState = false;
                        break;
                    }
                }
                if (singleState) { // Fast path for uniform sections, e.g. all air
                    final byte opacityValue = opacityTable[javaId0];
                    final byte emissionValue = emissionTable[javaId0];
                    for (int y = 0; y < 16; y++) {
                        final int layerBase = ((sectionIndex << 4) + y) * REGION_SIZE_SQUARED;
                        for (int z = 0; z < 16; z++) {
                            final int rowBase = layerBase + (baseZ + z) * REGION_SIZE + baseX;
                            Arrays.fill(opacity, rowBase, rowBase + 16, opacityValue);
                            Arrays.fill(emission, rowBase, rowBase + 16, emissionValue);
                        }
                    }
                    continue;
                }

                for (int y = 0; y < 16; y++) {
                    final int layerBase = ((sectionIndex << 4) + y) * REGION_SIZE_SQUARED;
                    final int statesBase = y << 8;
                    for (int z = 0; z < 16; z++) {
                        final int regionIndex = layerBase + (baseZ + z) * REGION_SIZE + baseX;
                        final int statesIndex = statesBase | (z << 4);
                        for (int x = 0; x < 16; x++) {
                            final int stateId = states[statesIndex + x];
                            opacity[regionIndex + x] = opacityTable[stateId];
                            emission[regionIndex + x] = emissionTable[stateId];
                        }
                    }
                }
            }
        }
    }

    /**
     * Scans every column of the region from the top and assigns sky light to all blocks that are
     * directly lit by the sky.
     */
    private static void seedSkyLight(final byte[] skyLight, final byte[] opacity, final short[] skyCutoff, final int height) {
        for (int z = 0; z < REGION_SIZE; z++) {
            for (int x = 0; x < REGION_SIZE; x++) {
                int level = 15;
                short cutoff = -1; // Topmost block of the column that is not fully sky lit
                for (int y = height - 1; y >= 0; y--) {
                    final int index = (y * REGION_SIZE + z) * REGION_SIZE + x;
                    if (level == 15 && opacity[index] == 0) { // Sky light travels downwards without losing level
                        skyLight[index] = 15;
                        continue;
                    }
                    level = Math.max(0, level - Math.max(1, opacity[index] & 0xFF));
                    if (cutoff == -1) skyCutoff[z * REGION_SIZE + x] = cutoff = (short) y;
                    if (level == 0) break;
                    skyLight[index] = (byte) level;
                }
            }
        }
    }

    /**
     * Enqueues all directly sky lit blocks that are able to brighten one of their neighbors. Cells
     * that cannot brighten any neighbor are skipped, as light values never decrease again during
     * propagation. Fully sky lit cells above the terrain of all neighboring columns are skipped
     * upfront, as all their neighbors are fully sky lit as well.
     */
    private static void enqueueSkyLightFrontier(final byte[] skyLight, final byte[] opacity, final short[] skyCutoff, final IntQueue queue, final int height) {
        for (int y = height - 1; y >= 0; y--) {
            for (int z = 0; z < REGION_SIZE; z++) {
                for (int x = 0; x < REGION_SIZE; x++) {
                    final int index = (y * REGION_SIZE + z) * REGION_SIZE + x;
                    final int level = skyLight[index] & 0xFF;
                    if (level <= 1) continue;

                    if (level == 15) { // Cheap check whether any horizontal neighbor column is taller than this cell
                        boolean neighborColumnTaller = false;
                        if (x > 0 && skyCutoff[z * REGION_SIZE + x - 1] >= y) neighborColumnTaller = true;
                        else if (x < REGION_SIZE - 1 && skyCutoff[z * REGION_SIZE + x + 1] >= y) neighborColumnTaller = true;
                        else if (z > 0 && skyCutoff[(z - 1) * REGION_SIZE + x] >= y) neighborColumnTaller = true;
                        else if (z < REGION_SIZE - 1 && skyCutoff[(z + 1) * REGION_SIZE + x] >= y) neighborColumnTaller = true;
                        if (!neighborColumnTaller) continue;
                    }

                    if (canBrightenNeighbor(skyLight, opacity, x, y, z, level, height, true)) {
                        queue.enqueue(pack(x, y, z));
                    }
                }
            }
        }
    }

    private static void seedBlockLight(final byte[] blockLight, final byte[] emission, final IntQueue queue, final int height) {
        for (int index = 0; index < emission.length; index++) {
            final int level = emission[index] & 0xFF;
            if (level == 0) continue;

            blockLight[index] = (byte) level;
            if (level > 1) {
                final int y = index / REGION_SIZE_SQUARED;
                final int remainder = index % REGION_SIZE_SQUARED;
                queue.enqueue(pack(remainder % REGION_SIZE, y, remainder / REGION_SIZE));
            }
        }
    }

    private static void propagate(final byte[] light, final byte[] opacity, final IntQueue queue, final int height, final boolean skyLight) {
        while (!queue.isEmpty()) {
            final int packed = queue.dequeueInt();
            final int x = packed & 0x3F;
            final int z = (packed >>> 6) & 0x3F;
            final int y = packed >>> 12;
            final int index = (y * REGION_SIZE + z) * REGION_SIZE + x;
            final int level = light[index] & 0xFF;
            if (level <= 1) continue;

            for (int[] direction : DIRECTIONS) {
                spread(light, opacity, queue, x + direction[0], y + direction[1], z + direction[2], direction[1] == -1, level, height, skyLight);
            }
        }
    }

    private static void spread(final byte[] light, final byte[] opacity, final IntQueue queue, final int x, final int y, final int z, final boolean downwards, final int level, final int height, final boolean skyLight) {
        if (x < 0 || x >= REGION_SIZE || z < 0 || z >= REGION_SIZE || y < 0 || y >= height) return;

        final int neighborIndex = (y * REGION_SIZE + z) * REGION_SIZE + x;
        final int neighborOpacity = opacity[neighborIndex] & 0xFF;
        if (neighborOpacity >= 15) return; // Fully opaque blocks receive no light

        final int targetLevel;
        if (skyLight && downwards && level == 15 && neighborOpacity == 0) {
            targetLevel = 15; // Sky light of level 15 travels downwards without losing level
        } else {
            targetLevel = level - Math.max(1, neighborOpacity);
        }
        if (targetLevel > (light[neighborIndex] & 0xFF)) {
            light[neighborIndex] = (byte) targetLevel;
            if (targetLevel > 1) queue.enqueue(pack(x, y, z));
        }
    }

    private static boolean canBrightenNeighbor(final byte[] light, final byte[] opacity, final int x, final int y, final int z, final int level, final int height, final boolean skyLight) {
        for (int direction = 0; direction < DIRECTIONS.length; direction++) {
            final int nx = x + DIRECTIONS[direction][0];
            final int ny = y + DIRECTIONS[direction][1];
            final int nz = z + DIRECTIONS[direction][2];
            if (nx < 0 || nx >= REGION_SIZE || nz < 0 || nz >= REGION_SIZE || ny < 0 || ny >= height) continue;

            final int neighborIndex = (ny * REGION_SIZE + nz) * REGION_SIZE + nx;
            final int neighborOpacity = opacity[neighborIndex] & 0xFF;
            if (neighborOpacity >= 15) continue;

            final int targetLevel;
            if (skyLight && direction == DOWN && level == 15 && neighborOpacity == 0) {
                targetLevel = 15;
            } else {
                targetLevel = level - Math.max(1, neighborOpacity);
            }
            if (targetLevel > (light[neighborIndex] & 0xFF)) return true;
        }
        return false;
    }

    /**
     * Packs the light of a section of a chunk of the region into a protocol nibble array. A
     * {@code null} return value means the whole section is unlit; the shared full array is
     * returned for fully lit sections.
     */
    private static byte[] extractSection(final byte[] light, final int baseX, final int baseZ, final int sectionIndex, final int height) {
        final byte[] data = new byte[LIGHT_LENGTH];
        boolean allZero = true;
        boolean allFifteen = true;

        for (int y = 0; y < 16; y++) {
            final int layerBase = ((sectionIndex << 4) + y) * REGION_SIZE_SQUARED;
            for (int z = 0; z < 16; z++) {
                int index = layerBase + (baseZ + z) * REGION_SIZE + baseX;
                for (int x = 0; x < 16; x++, index++) {
                    final int level = light[index] & 0xFF;
                    final int byteIndex = (y << 7) | (z << 3) | (x >> 1);
                    if ((x & 1) == 0) {
                        data[byteIndex] |= (byte) level;
                    } else {
                        data[byteIndex] |= (byte) (level << 4);
                    }
                    if (level != 0) allZero = false;
                    if (level != 15) allFifteen = false;
                }
            }
        }

        if (allZero) return null;
        if (allFifteen) return ChunkLight.FULL;
        return data;
    }

    private static int pack(final int x, final int y, final int z) {
        return (y << 12) | (z << 6) | x;
    }

    /**
     * Simple growable int first in first out queue.
     */
    private static final class IntQueue {

        private int[] array = new int[4096];
        private int head;
        private int tail;

        public boolean isEmpty() {
            return this.head == this.tail;
        }

        public void enqueue(final int value) {
            if (this.tail == this.array.length) {
                final int size = this.tail - this.head;
                if (this.head >= this.array.length / 2) {
                    System.arraycopy(this.array, this.head, this.array, 0, size);
                    this.head = 0;
                    this.tail = size;
                } else {
                    this.array = Arrays.copyOf(this.array, this.array.length * 2);
                }
            }
            this.array[this.tail++] = value;
        }

        public int dequeueInt() {
            return this.array[this.head++];
        }

        public void clear() {
            this.head = 0;
            this.tail = 0;
        }

    }

}
