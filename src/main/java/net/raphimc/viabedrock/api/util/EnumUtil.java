/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.ToIntFunction;

public class EnumUtil {

    public static <T extends Enum<T>> T getEnumConstantOrNull(final Class<T> enumClass, final String name) {
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static <T extends Enum<T>> Set<T> getEnumSetFromBitmask(final Class<T> enumClass, final long bitmask, final ToIntFunction<T> bitGetter) {
        final EnumSet<T> set = EnumSet.noneOf(enumClass);
        for (T constant : enumClass.getEnumConstants()) {
            final int bit = bitGetter.applyAsInt(constant);
            if (bit >= 0 && bit < Long.SIZE && (bitmask & (1L << bit)) != 0) {
                set.add(constant);
            }
        }
        return set;
    }

    public static <T extends Enum<T>> Set<T> getEnumSetFromBitmask(final Class<T> enumClass, final BigInteger bitmask, final ToIntFunction<T> bitGetter) {
        final EnumSet<T> set = EnumSet.noneOf(enumClass);
        for (T constant : enumClass.getEnumConstants()) {
            final int bit = bitGetter.applyAsInt(constant);
            if (bit >= 0 && bitmask.testBit(bit)) {
                set.add(constant);
            }
        }
        return set;
    }

    public static <T extends Enum<T>> int getIntBitmaskFromEnumSet(final Set<T> set, final ToIntFunction<T> bitGetter) {
        int bitmask = 0;
        for (T constant : set) {
            final int bit = bitGetter.applyAsInt(constant);
            if (bit >= 0 && bit < Integer.SIZE) {
                bitmask |= 1 << bit;
            }
        }
        return bitmask;
    }

    public static <T extends Enum<T>> long getLongBitmaskFromEnumSet(final Set<T> set, final ToIntFunction<T> bitGetter) {
        long bitmask = 0;
        for (T constant : set) {
            final int bit = bitGetter.applyAsInt(constant);
            if (bit >= 0 && bit < Long.SIZE) {
                bitmask |= 1L << bit;
            }
        }
        return bitmask;
    }

    public static <T extends Enum<T>> BigInteger getBigBitmaskFromEnumSet(final Set<T> set, final ToIntFunction<T> bitGetter) {
        BigInteger bitmask = BigInteger.ZERO;
        for (T constant : set) {
            final int bit = bitGetter.applyAsInt(constant);
            if (bit >= 0) {
                bitmask = bitmask.setBit(bit);
            }
        }
        return bitmask;
    }

}
