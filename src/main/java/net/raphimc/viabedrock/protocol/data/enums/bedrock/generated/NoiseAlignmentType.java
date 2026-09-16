// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum NoiseAlignmentType {

    MinLocalTransitionEnd(0),
    ;

    private static final Int2ObjectMap<NoiseAlignmentType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (NoiseAlignmentType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static NoiseAlignmentType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static NoiseAlignmentType getByValue(final int value, final NoiseAlignmentType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static NoiseAlignmentType getByName(final String name) {
        for (NoiseAlignmentType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static NoiseAlignmentType getByName(final String name, final NoiseAlignmentType fallback) {
        for (NoiseAlignmentType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    NoiseAlignmentType(final NoiseAlignmentType value) {
        this(value.value);
    }

    NoiseAlignmentType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
