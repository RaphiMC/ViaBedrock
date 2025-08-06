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
package net.raphimc.viabedrock.protocol.data.enums.bedrock;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemDataVersion {

    LEGACY(0),
    DATA_DRIVEN(1),
    NONE(2);

    private static final Int2ObjectMap<ItemDataVersion> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemDataVersion value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ItemDataVersion getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemDataVersion getByValue(final int value, final ItemDataVersion fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ItemDataVersion(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
