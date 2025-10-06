// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CommandPermissionLevel {

    Any(0),
    GameDirectors(1),
    Admin(2),
    Host(3),
    Owner(4),
    Internal(5);

    private static final Int2ObjectMap<CommandPermissionLevel> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CommandPermissionLevel value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CommandPermissionLevel getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CommandPermissionLevel getByValue(final int value, final CommandPermissionLevel fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CommandPermissionLevel(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
