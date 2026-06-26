// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MinecraftEventing_TeleportationCause {

    Unknown(0),
    Projectile(1),
    ChorusFruit(2),
    Command(3),
    Behavior(4),
    TeleportationCause_Count(5),
    ;

    private static final Int2ObjectMap<MinecraftEventing_TeleportationCause> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MinecraftEventing_TeleportationCause value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static MinecraftEventing_TeleportationCause getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MinecraftEventing_TeleportationCause getByValue(final int value, final MinecraftEventing_TeleportationCause fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static MinecraftEventing_TeleportationCause getByName(final String name) {
        for (MinecraftEventing_TeleportationCause value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static MinecraftEventing_TeleportationCause getByName(final String name, final MinecraftEventing_TeleportationCause fallback) {
        for (MinecraftEventing_TeleportationCause value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    MinecraftEventing_TeleportationCause(final MinecraftEventing_TeleportationCause value) {
        this(value.value);
    }

    MinecraftEventing_TeleportationCause(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
