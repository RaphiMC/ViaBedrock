// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerLocationPacketType {

    PLAYER_LOCATION_COORDINATES(0),
    PLAYER_LOCATION_HIDE(1),
    ;

    private static final Int2ObjectMap<PlayerLocationPacketType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerLocationPacketType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PlayerLocationPacketType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerLocationPacketType getByValue(final int value, final PlayerLocationPacketType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PlayerLocationPacketType getByName(final String name) {
        for (PlayerLocationPacketType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PlayerLocationPacketType getByName(final String name, final PlayerLocationPacketType fallback) {
        for (PlayerLocationPacketType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PlayerLocationPacketType(final PlayerLocationPacketType value) {
        this(value.value);
    }

    PlayerLocationPacketType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
