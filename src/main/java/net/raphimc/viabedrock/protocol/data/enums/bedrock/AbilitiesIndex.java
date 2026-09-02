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
package net.raphimc.viabedrock.protocol.data.enums.bedrock;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AbilitiesIndex {

    Invalid(-1),
    Build(0),
    Mine(1),
    DoorsAndSwitches(2),
    OpenContainers(3),
    AttackPlayers(4),
    AttackMobs(5),
    OperatorCommands(6),
    Teleport(7),
    Invulnerable(8),
    Flying(9),
    MayFly(10),
    Instabuild(11),
    Lightning(12),
    FlySpeed(13),
    WalkSpeed(14),
    Muted(15),
    WorldBuilder(16),
    NoClip(17),
    PrivilegedBuilder(18),
    VerticalFlySpeed(19),
    ;

    private static final Int2ObjectMap<AbilitiesIndex> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AbilitiesIndex value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static AbilitiesIndex getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AbilitiesIndex getByValue(final int value, final AbilitiesIndex fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static AbilitiesIndex getByName(final String name) {
        for (AbilitiesIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static AbilitiesIndex getByName(final String name, final AbilitiesIndex fallback) {
        for (AbilitiesIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    AbilitiesIndex(final AbilitiesIndex value) {
        this(value.value);
    }

    AbilitiesIndex(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
