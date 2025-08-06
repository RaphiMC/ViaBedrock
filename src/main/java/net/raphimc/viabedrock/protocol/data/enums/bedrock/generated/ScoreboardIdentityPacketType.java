// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ScoreboardIdentityPacketType {

    Update(0),
    Remove(1);

    private static final Int2ObjectMap<ScoreboardIdentityPacketType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ScoreboardIdentityPacketType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ScoreboardIdentityPacketType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ScoreboardIdentityPacketType getByValue(final int value, final ScoreboardIdentityPacketType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ScoreboardIdentityPacketType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
