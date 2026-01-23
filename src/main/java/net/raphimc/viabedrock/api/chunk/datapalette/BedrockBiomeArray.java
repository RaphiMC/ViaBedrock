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
package net.raphimc.viabedrock.api.chunk.datapalette;

import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;

public class BedrockBiomeArray implements DataPalette, Cloneable {

    private byte[] biomes;

    public BedrockBiomeArray() {
        this.biomes = new byte[256];
    }

    public BedrockBiomeArray(final byte[] biomes) {
        this.biomes = biomes;
    }

    @Override
    public int index(final int x, final int y, final int z) {
        return (z << 4) | x;
    }

    @Override
    public int idAt(final int sectionCoordinate) {
        return this.biomes[sectionCoordinate] & 255;
    }

    @Override
    public void setIdAt(final int sectionCoordinate, final int id) {
        if (id > 255) {
            throw new IllegalArgumentException("Too large biome id: " + id);
        }

        this.biomes[sectionCoordinate] = (byte) id;
    }

    @Override
    public int idByIndex(final int index) {
        return this.idAt(index);
    }

    @Override
    public void setIdByIndex(final int index, final int id) {
        this.setIdAt(index, id);
    }

    @Override
    public int paletteIndexAt(final int packedCoordinate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPaletteIndexAt(final int sectionCoordinate, final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addId(final int id) {
    }

    @Override
    public void replaceId(final int oldId, final int newId) {
        for (int i = 0; i < this.size(); i++) {
            if (this.idAt(i) == oldId) {
                this.setIdAt(i, newId);
            }
        }
    }

    @Override
    public int size() {
        return this.biomes.length;
    }

    @Override
    public void clear() {
        this.biomes = new byte[this.biomes.length];
    }

    @Override
    public BedrockBiomeArray clone() {
        return new BedrockBiomeArray(this.biomes.clone());
    }

}
