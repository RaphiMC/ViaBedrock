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
package net.raphimc.viabedrock.api;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;

public class HashedPaletteComparator implements Comparator<String> {

    public static final HashedPaletteComparator INSTANCE = new HashedPaletteComparator();

    private static final long FNV1_64_INIT = 0xcbf29ce484222325L;
    private static final long FNV1_PRIME_64 = 1099511628211L;

    @Override
    public int compare(String o1, String o2) {
        final byte[] bytes1 = o1.getBytes(StandardCharsets.UTF_8);
        final byte[] bytes2 = o2.getBytes(StandardCharsets.UTF_8);
        final long hash1 = fnv164(bytes1);
        final long hash2 = fnv164(bytes2);
        return Long.compareUnsigned(hash1, hash2);
    }

    public static long fnv164(final byte[] data) {
        long hash = FNV1_64_INIT;
        for (byte datum : data) {
            hash *= FNV1_PRIME_64;
            hash ^= (datum & 0xff);
        }

        return hash;
    }

}
