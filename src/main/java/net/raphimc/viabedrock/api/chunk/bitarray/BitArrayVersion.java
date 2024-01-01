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

import net.raphimc.viabedrock.api.util.MathUtil;

public enum BitArrayVersion {

    V16(16, 2, null),
    V8(8, 4, V16),
    V6(6, 5, V8), // 2 bit padding
    V5(5, 6, V6), // 2 bit padding
    V4(4, 8, V5),
    V3(3, 10, V4), // 2 bit padding
    V2(2, 16, V3),
    V1(1, 32, V2),
    V0(0, 0, V1);

    private final byte bits;
    private final byte entriesPerWord;
    private final int maxEntryValue;
    private final BitArrayVersion next;

    BitArrayVersion(final int bits, final int entriesPerWord, final BitArrayVersion next) {
        this.bits = (byte) bits;
        this.entriesPerWord = (byte) entriesPerWord;
        this.maxEntryValue = (1 << this.bits) - 1;
        this.next = next;
    }

    public static BitArrayVersion get(final int version, final boolean read) {
        for (BitArrayVersion ver : BitArrayVersion.values()) {
            if ((!read && ver.entriesPerWord <= version) || (read && ver.bits == version)) {
                return ver;
            }
        }

        throw new IllegalArgumentException("Invalid palette version: " + version);
    }

    public static BitArrayVersion forBitsCeil(final int bits) {
        for (int i = BitArrayVersion.values().length - 1; i >= 0; i--) {
            final BitArrayVersion version = BitArrayVersion.values()[i];
            if (version.bits >= bits) {
                return version;
            }
        }

        throw new IllegalArgumentException("Invalid palette bits: " + bits);
    }


    public BitArray createArray(final int size) {
        return this.createArray(size, new int[this.getWordsForSize(size)]);
    }

    public BitArray createArray(final int size, final int[] words) {
        if (this == V3 || this == V5 || this == V6) {
            // Padded palettes aren't able to use bitwise operations due to their padding.
            return new PaddedBitArray(this, size, words);
        } else if (this == V0) {
            return SingletonBitArray.INSTANCE;
        } else {
            return new Pow2BitArray(this, size, words);
        }
    }

    public int getWordsForSize(final int size) {
        if (this.entriesPerWord == 0) return 0;
        return MathUtil.ceil((float) size / this.entriesPerWord);
    }

    public byte getBits() {
        return this.bits;
    }

    public byte getEntriesPerWord() {
        return this.entriesPerWord;
    }

    public int getMaxEntryValue() {
        return this.maxEntryValue;
    }

    public BitArrayVersion getNext() {
        return this.next;
    }

}
