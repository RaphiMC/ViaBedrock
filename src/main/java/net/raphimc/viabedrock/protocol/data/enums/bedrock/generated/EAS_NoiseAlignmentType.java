// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EAS_NoiseAlignmentType {

    minlocaltransitionend(0),
    ;

    private static final Int2ObjectMap<EAS_NoiseAlignmentType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (EAS_NoiseAlignmentType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static EAS_NoiseAlignmentType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static EAS_NoiseAlignmentType getByValue(final int value, final EAS_NoiseAlignmentType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static EAS_NoiseAlignmentType getByName(final String name) {
        for (EAS_NoiseAlignmentType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static EAS_NoiseAlignmentType getByName(final String name, final EAS_NoiseAlignmentType fallback) {
        for (EAS_NoiseAlignmentType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    EAS_NoiseAlignmentType(final EAS_NoiseAlignmentType value) {
        this(value.value);
    }

    EAS_NoiseAlignmentType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
