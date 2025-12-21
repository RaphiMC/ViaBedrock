// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ServerboundLoadingScreenPacketType {

    Unknown(0),
    StartLoadingScreen(1),
    EndLoadingScreen(2),
    ;

    private static final Int2ObjectMap<ServerboundLoadingScreenPacketType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ServerboundLoadingScreenPacketType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ServerboundLoadingScreenPacketType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ServerboundLoadingScreenPacketType getByValue(final int value, final ServerboundLoadingScreenPacketType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ServerboundLoadingScreenPacketType getByName(final String name) {
        for (ServerboundLoadingScreenPacketType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ServerboundLoadingScreenPacketType getByName(final String name, final ServerboundLoadingScreenPacketType fallback) {
        for (ServerboundLoadingScreenPacketType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ServerboundLoadingScreenPacketType(final ServerboundLoadingScreenPacketType value) {
        this(value.value);
    }

    ServerboundLoadingScreenPacketType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
