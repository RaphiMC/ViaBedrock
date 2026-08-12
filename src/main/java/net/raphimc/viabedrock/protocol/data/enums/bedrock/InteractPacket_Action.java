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

public enum InteractPacket_Action {

    Invalid(0),
    StopRiding(1),
    InteractUpdate(2),
    NpcOpen(3),
    OpenInventory(4),
    ;

    private static final Int2ObjectMap<InteractPacket_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InteractPacket_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static InteractPacket_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InteractPacket_Action getByValue(final int value, final InteractPacket_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static InteractPacket_Action getByName(final String name) {
        for (InteractPacket_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static InteractPacket_Action getByName(final String name, final InteractPacket_Action fallback) {
        for (InteractPacket_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    InteractPacket_Action(final InteractPacket_Action value) {
        this(value.value);
    }

    InteractPacket_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
