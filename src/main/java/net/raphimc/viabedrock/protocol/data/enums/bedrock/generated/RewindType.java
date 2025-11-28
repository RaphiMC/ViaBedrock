// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum RewindType {

    Player(0),
    Vehicle(1);

    private static final Int2ObjectMap<RewindType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (RewindType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static RewindType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static RewindType getByValue(final int value, final RewindType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    RewindType(final RewindType value) {
        this(value.value);
    }

    RewindType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
