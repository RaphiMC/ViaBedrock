// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ColorAttributeOperation {

    OVERRIDE(0),
    ALPHA_BLEND(1),
    ADD(2),
    SUBTRACT(3),
    MULTIPLY(4),
    ;

    private static final Int2ObjectMap<ColorAttributeOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ColorAttributeOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ColorAttributeOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ColorAttributeOperation getByValue(final int value, final ColorAttributeOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ColorAttributeOperation getByName(final String name) {
        for (ColorAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ColorAttributeOperation getByName(final String name, final ColorAttributeOperation fallback) {
        for (ColorAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ColorAttributeOperation(final ColorAttributeOperation value) {
        this(value.value);
    }

    ColorAttributeOperation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
