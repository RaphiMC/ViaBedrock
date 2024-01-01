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
package net.raphimc.viabedrock.api.util;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;

public class HashedPaletteComparator implements Comparator<String> {

    public static final HashedPaletteComparator INSTANCE = new HashedPaletteComparator();

    @Override
    public int compare(String o1, String o2) {
        final long hash1 = FNV1.fnv1_64(o1.getBytes(StandardCharsets.UTF_8));
        final long hash2 = FNV1.fnv1_64(o2.getBytes(StandardCharsets.UTF_8));
        return Long.compareUnsigned(hash1, hash2);
    }

}
