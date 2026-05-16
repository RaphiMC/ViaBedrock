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

// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_VillageType {

    Desert(0),
    Ice(1),
    Savanna(2),
    Taiga(3),
    Default(4),
    ;

    private static final Int2ObjectMap<SharedTypes_VillageType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_VillageType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SharedTypes_VillageType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_VillageType getByValue(final int value, final SharedTypes_VillageType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SharedTypes_VillageType getByName(final String name) {
        for (SharedTypes_VillageType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SharedTypes_VillageType getByName(final String name, final SharedTypes_VillageType fallback) {
        for (SharedTypes_VillageType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SharedTypes_VillageType(final SharedTypes_VillageType value) {
        this(value.value);
    }

    SharedTypes_VillageType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
