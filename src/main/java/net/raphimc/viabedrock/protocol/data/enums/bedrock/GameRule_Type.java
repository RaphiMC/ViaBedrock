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

public enum GameRule_Type {

    Invalid(0),
    Bool(1),
    Int(2),
    Float(3),
    ;

    private static final Int2ObjectMap<GameRule_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (GameRule_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static GameRule_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static GameRule_Type getByValue(final int value, final GameRule_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static GameRule_Type getByName(final String name) {
        for (GameRule_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static GameRule_Type getByName(final String name, final GameRule_Type fallback) {
        for (GameRule_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    GameRule_Type(final GameRule_Type value) {
        this(value.value);
    }

    GameRule_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
