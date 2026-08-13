// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerLocationPacketPayload_Type {

    PLAYER_LOCATION_COORDINATES(0),
    PLAYER_LOCATION_HIDE(1),
    ;

    private static final Int2ObjectMap<PlayerLocationPacketPayload_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerLocationPacketPayload_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PlayerLocationPacketPayload_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerLocationPacketPayload_Type getByValue(final int value, final PlayerLocationPacketPayload_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PlayerLocationPacketPayload_Type getByName(final String name) {
        for (PlayerLocationPacketPayload_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PlayerLocationPacketPayload_Type getByName(final String name, final PlayerLocationPacketPayload_Type fallback) {
        for (PlayerLocationPacketPayload_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PlayerLocationPacketPayload_Type(final PlayerLocationPacketPayload_Type value) {
        this(value.value);
    }

    PlayerLocationPacketPayload_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
