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

public class FNV1 {

    private static final long FNV1_64_INIT = 0xcbf29ce484222325L;
    private static final long FNV1_PRIME_64 = 1099511628211L;

    private static final int FNV1_32_INIT = 0x811c9dc5;
    private static final int FNV1_PRIME_32 = 0x01000193;

    public static long fnv1_64(final byte[] data) {
        long hash = FNV1_64_INIT;
        for (byte b : data) {
            hash *= FNV1_PRIME_64;
            hash ^= (b & 0xff);
        }

        return hash;
    }

    public static int fnv1a_32(final byte[] data) {
        int hash = FNV1_32_INIT;
        for (byte b : data) {
            hash ^= (b & 0xff);
            hash *= FNV1_PRIME_32;
        }

        return hash;
    }

}
