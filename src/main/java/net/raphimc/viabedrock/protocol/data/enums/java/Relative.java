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
package net.raphimc.viabedrock.protocol.data.enums.java;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum Relative {

    X,
    Y,
    Z,
    Y_ROT,
    X_ROT,
    DELTA_X,
    DELTA_Y,
    DELTA_Z,
    ROTATE_DELTA;

    public static final Set<Relative> NONE = Collections.unmodifiableSet(EnumSet.noneOf(Relative.class));
    public static final Set<Relative> ROTATION = Collections.unmodifiableSet(EnumSet.of(X_ROT, Y_ROT));
    public static final Set<Relative> VELOCITY = Collections.unmodifiableSet(EnumSet.of(DELTA_X, DELTA_Y, DELTA_Z, ROTATE_DELTA));

    @SafeVarargs
    public static Set<Relative> union(final Set<Relative>... sets) {
        final EnumSet<Relative> result = EnumSet.noneOf(Relative.class);
        for (final Set<Relative> set : sets) {
            result.addAll(set);
        }
        return Collections.unmodifiableSet(result);
    }

    public byte getBit() {
        return (byte) (1 << this.ordinal());
    }

}
