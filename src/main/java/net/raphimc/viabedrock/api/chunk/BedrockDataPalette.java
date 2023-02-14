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
package net.raphimc.viabedrock.api.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import net.raphimc.viabedrock.api.chunk.bitarray.BitArray;
import net.raphimc.viabedrock.api.chunk.bitarray.BitArrayVersion;

public class BedrockDataPalette implements DataPalette, Cloneable {

    private final IntList palette;
    private BitArray bitArray;

    public BedrockDataPalette() {
        this(BitArrayVersion.V2);
    }

    public BedrockDataPalette(final BitArrayVersion version) {
        this.bitArray = version.createArray(ChunkSection.SIZE);
        this.palette = new IntArrayList(version.getEntriesPerWord());
    }

    public BedrockDataPalette(final IntList palette, final BitArray bitArray) {
        this.palette = palette;
        this.bitArray = bitArray;
    }

    @Override
    public int index(final int x, final int y, final int z) {
        return (x << 8) + (z << 4) + y;
    }

    @Override
    public int idAt(final int sectionCoordinate) {
        return this.palette.getInt(this.bitArray.get(sectionCoordinate));
    }

    @Override
    public void setIdAt(final int sectionCoordinate, final int id) {
        int index = this.palette.indexOf(id);
        if (index == -1) {
            index = this.palette.size();
            this.addId(id);
        }

        this.bitArray.set(sectionCoordinate, index);
    }

    @Override
    public int idByIndex(final int index) {
        return this.palette.getInt(index);
    }

    @Override
    public void setIdByIndex(final int index, final int id) {
        this.palette.set(index, id);
    }

    @Override
    public int paletteIndexAt(final int packedCoordinate) {
        return this.bitArray.get(packedCoordinate);
    }

    @Override
    public void setPaletteIndexAt(final int sectionCoordinate, final int index) {
        this.bitArray.set(sectionCoordinate, index);
    }

    @Override
    public void addId(final int id) {
        this.palette.add(id);

        final BitArrayVersion currentVersion = this.bitArray.getVersion();
        if (this.palette.size() >= currentVersion.getMaxEntryValue()) {
            final BitArrayVersion nextVersion = currentVersion.getNext();
            if (nextVersion != null) {
                final BitArray newBitArray = nextVersion.createArray(this.bitArray.size());
                for (int i = 0; i < this.bitArray.size(); i++) {
                    newBitArray.set(i, this.bitArray.get(i));
                }
                this.bitArray = newBitArray;
            }
        }
    }

    @Override
    public void replaceId(final int oldId, final int newId) {
        final int index = this.palette.indexOf(oldId);
        if (index == -1) return;

        for (int i = 0; i < this.palette.size(); i++) {
            if (this.palette.getInt(i) == oldId) {
                this.palette.set(i, newId);
            }
        }
    }

    @Override
    public int size() {
        return this.palette.size();
    }

    @Override
    public void clear() {
        this.palette.clear();
    }

    @Override
    public BedrockDataPalette clone() {
        return new BedrockDataPalette(new IntArrayList(this.palette), this.bitArray.clone());
    }

    public BitArray getBitArray() {
        return this.bitArray;
    }

}
