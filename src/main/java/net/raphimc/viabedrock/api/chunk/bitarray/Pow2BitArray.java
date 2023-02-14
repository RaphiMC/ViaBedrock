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

package net.raphimc.viabedrock.api.chunk.bitarray;

import com.google.common.base.Preconditions;
import net.raphimc.viabedrock.api.MathUtil;

import java.util.Arrays;

public class Pow2BitArray implements BitArray {

    /**
     * Array used to store data
     */
    private final int[] words;

    /**
     * Palette version information
     */
    private final BitArrayVersion version;

    /**
     * Number of entries in this palette (<b>not</b> the length of the words array that internally backs this palette)
     */
    private final int size;

    Pow2BitArray(final BitArrayVersion version, final int size, final int[] words) {
        this.size = size;
        this.version = version;
        this.words = words;

        final int expectedWordsLength = MathUtil.ceil((float) size / version.getEntriesPerWord());
        if (words.length != expectedWordsLength) {
            throw new IllegalArgumentException("Invalid length given for storage, got: " + words.length + " but expected: " + expectedWordsLength);
        }
    }

    /**
     * Sets the entry at the given location to the given value
     */
    public void set(final int index, final int value) {
        Preconditions.checkElementIndex(index, this.size);
        Preconditions.checkArgument(value >= 0 && value <= this.version.getMaxEntryValue(), "Invalid value %s", value);

        final int bitIndex = index * this.version.getBits();
        final int arrayIndex = bitIndex >> 5;
        final int offset = bitIndex & 31;
        this.words[arrayIndex] = this.words[arrayIndex] & ~(this.version.getMaxEntryValue() << offset) | (value & this.version.getMaxEntryValue()) << offset;
    }

    /**
     * Gets the entry at the given index
     */
    public int get(final int index) {
        Preconditions.checkElementIndex(index, this.size);

        final int bitIndex = index * this.version.getBits();
        final int arrayIndex = bitIndex >> 5;
        final int wordOffset = bitIndex & 31;
        return this.words[arrayIndex] >>> wordOffset & this.version.getMaxEntryValue();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public int[] getWords() {
        return this.words;
    }

    @Override
    public BitArrayVersion getVersion() {
        return this.version;
    }

    @Override
    public Pow2BitArray clone() {
        return new Pow2BitArray(this.version, this.size, Arrays.copyOf(this.words, this.words.length));
    }

}
