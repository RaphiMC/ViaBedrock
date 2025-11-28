// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerListPacketType {

    Add(0),
    Remove(1);

    private static final Int2ObjectMap<PlayerListPacketType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerListPacketType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PlayerListPacketType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerListPacketType getByValue(final int value, final PlayerListPacketType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PlayerListPacketType(final PlayerListPacketType value) {
        this(value.value);
    }

    PlayerListPacketType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
