// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ScorePacketType {

    Change(0),
    Remove(1);

    private static final Int2ObjectMap<ScorePacketType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ScorePacketType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ScorePacketType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ScorePacketType getByValue(final int value, final ScorePacketType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ScorePacketType(final ScorePacketType value) {
        this(value.value);
    }

    ScorePacketType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
