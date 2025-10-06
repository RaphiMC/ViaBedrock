// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerPermissionLevel {

    Visitor(0),
    Member(1),
    Operator(2),
    Custom(3);

    private static final Int2ObjectMap<PlayerPermissionLevel> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerPermissionLevel value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PlayerPermissionLevel getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerPermissionLevel getByValue(final int value, final PlayerPermissionLevel fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PlayerPermissionLevel(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
