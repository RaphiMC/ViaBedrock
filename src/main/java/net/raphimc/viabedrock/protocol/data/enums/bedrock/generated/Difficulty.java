// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Difficulty {

    Peaceful(0),
    Easy(1),
    Normal(2),
    Hard(3),
    Unknown(5);

    private static final Int2ObjectMap<Difficulty> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Difficulty value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static Difficulty getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Difficulty getByValue(final int value, final Difficulty fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    Difficulty(final Difficulty value) {
        this(value.value);
    }

    Difficulty(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
