/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.api.util;

import com.google.common.primitives.Ints;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.util.Arrays;

public class LZ4 {

    private static final LZ4Factory LZ4_FACTORY = LZ4Factory.fastestInstance();
    private static final LZ4Compressor LZ4_COMPRESSOR = LZ4_FACTORY.highCompressor();
    private static final LZ4FastDecompressor LZ4_DECOMPRESSOR = LZ4_FACTORY.fastDecompressor();

    public static byte[] compress(final byte[] input) {
        if (input == null) return null;

        final int maxCompressedLength = LZ4_COMPRESSOR.maxCompressedLength(input.length);
        final byte[] compressed = new byte[maxCompressedLength + 4];
        final int compressedLength = LZ4_COMPRESSOR.compress(input, 0, input.length, compressed, 4);
        System.arraycopy(Ints.toByteArray(input.length), 0, compressed, 0, 4);
        return Arrays.copyOf(compressed, compressedLength + 4);
    }

    public static byte[] decompress(final byte[] input) {
        if (input == null) return null;

        return LZ4_DECOMPRESSOR.decompress(input, 4, Ints.fromByteArray(input));
    }

}
