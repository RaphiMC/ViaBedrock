/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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

import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.NibbleArray;

public class BedrockBlockArray implements DataPalette, Cloneable {

    private byte[] blocks;
    private NibbleArray data;

    public BedrockBlockArray() {
        this.blocks = new byte[ChunkSection.SIZE];
        this.data = new NibbleArray(this.blocks.length);
    }

    public BedrockBlockArray(final byte[] blocks, final NibbleArray data) {
        this.blocks = blocks;
        this.data = data;
    }

    @Override
    public int index(final int x, final int y, final int z) {
        return (x << 8) + (z << 4) + y;
    }

    @Override
    public int idAt(final int sectionCoordinate) {
        return (this.blocks[sectionCoordinate] & 255) << 4 | this.data.get(sectionCoordinate);
    }

    @Override
    public void setIdAt(final int sectionCoordinate, final int id) {
        if (id >> 4 > 255) {
            throw new IllegalArgumentException("Too large block id: " + id);
        }

        this.blocks[sectionCoordinate] = (byte) (id >> 4);
        this.data.set(sectionCoordinate, id & 15);
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
        return this.blocks.length;
    }

    @Override
    public void clear() {
        this.blocks = new byte[this.blocks.length];
        this.data = new NibbleArray(this.blocks.length);
    }

    @Override
    public BedrockBlockArray clone() {
        return new BedrockBlockArray(this.blocks.clone(), new NibbleArray(this.data.getHandle().clone()));
    }

    public byte[] getBlocks() {
        return this.blocks;
    }

    public NibbleArray getData() {
        return this.data;
    }

}
