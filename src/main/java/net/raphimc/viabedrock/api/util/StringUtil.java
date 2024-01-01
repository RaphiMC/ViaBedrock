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

import java.util.UUID;

public class StringUtil {

    /**
     * Encodes a UUID into a 16 character long minecraft invisible string
     *
     * @param uuid The uuid to encode
     * @return The encoded string
     */
    public static String encodeUUID(final UUID uuid) {
        return encodeLong(uuid.getMostSignificantBits()) + encodeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Encodes a long into an 8 character long minecraft invisible string
     *
     * @param bits The long to encode
     * @return The encoded string
     */
    public static String encodeLong(long bits) {
        final StringBuilder builder = new StringBuilder();
        final char[] chars = new char[4];
        for (int i = 0; i < 4; i++) {
            chars[i] = (char) (bits & 0xFF);
            bits >>= 8;
        }
        for (char c : chars) {
            builder.append('§').append(c);
        }
        return builder.toString();
    }

}
