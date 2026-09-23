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
 * Computed light data of a whole chunk column. Stores one 2048 byte nibble array per chunk section
 * for sky light and block light. The array indices include the two out of world sections, so they
 * can be used directly as light mask indices: index {@code 0} is the section below the world and
 * index {@code sectionCount + 1} is the section above the world.
 * <p>
 * A {@code null} array means that the light value of the whole section is zero. Sections with a
 * uniform light value of 15 use the shared {@link #FULL} array. Returned arrays must never be modified.
 */
public final class ChunkLight {

    /**
     * Shared read only array for sections with a uniform light value of 15.
     */
    public static final byte[] FULL = createFull();

    private final byte[][] skyLight;
    private final byte[][] blockLight;

    public ChunkLight(final int sectionCount, final boolean skyLight) {
        this.skyLight = new byte[sectionCount + 2][];
        this.blockLight = new byte[sectionCount + 2][];

        if (skyLight) { // Sections outside of the world bounds are always fully sky lit
            this.skyLight[0] = FULL;
            this.skyLight[sectionCount + 1] = FULL;
        }
    }

    /**
     * @return The amount of sections covered by the light data (chunk sections + 2 out of world sections)
     */
    public int skyLightLength() {
        return this.skyLight.length;
    }

    /**
     * @return The packed sky light nibble array of the given light mask index, or {@code null} if the whole section has a light value of zero
     */
    public byte[] skyLight(final int lightMaskIndex) {
        return this.skyLight[lightMaskIndex];
    }

    /**
     * @return The packed block light nibble array of the given light mask index, or {@code null} if the whole section has a light value of zero
     */
    public byte[] blockLight(final int lightMaskIndex) {
        return this.blockLight[lightMaskIndex];
    }

    /**
     * Replaces the packed sky light nibble array of the given light mask index.
     * Only intended for use by the light engine.
     */
    public void setSkyLight(final int lightMaskIndex, final byte[] data) {
        this.skyLight[lightMaskIndex] = data;
    }

    /**
     * Replaces the packed block light nibble array of the given light mask index.
     * Only intended for use by the light engine.
     */
    public void setBlockLight(final int lightMaskIndex, final byte[] data) {
        this.blockLight[lightMaskIndex] = data;
    }

    private static byte[] createFull() {
        final byte[] data = new byte[2048];
        Arrays.fill(data, (byte) 0xFF);
        return data;
    }

}
