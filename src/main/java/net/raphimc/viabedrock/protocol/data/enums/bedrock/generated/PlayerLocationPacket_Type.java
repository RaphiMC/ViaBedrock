// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerLocationPacket_Type {

    PLAYER_LOCATION_COORDINATES(0),
    PLAYER_LOCATION_HIDE(1);

    private static final Int2ObjectMap<PlayerLocationPacket_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerLocationPacket_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PlayerLocationPacket_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerLocationPacket_Type getByValue(final int value, final PlayerLocationPacket_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PlayerLocationPacket_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
