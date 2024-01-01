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

package net.raphimc.viabedrock.api.chunk.bitarray;

import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;

public class SingletonBitArray implements BitArray {

    public static final SingletonBitArray INSTANCE = new SingletonBitArray();

    private SingletonBitArray() {
    }

    @Override
    public void set(final int index, final int value) {
    }

    @Override
    public int get(final int index) {
        return 0;
    }

    @Override
    public int size() {
        return ChunkSection.SIZE;
    }

    @Override
    public int[] getWords() {
        return new int[0];
    }

    @Override
    public BitArrayVersion getVersion() {
        return BitArrayVersion.V0;
    }

    @Override
    public SingletonBitArray clone() {
        return SingletonBitArray.INSTANCE;
    }

}
