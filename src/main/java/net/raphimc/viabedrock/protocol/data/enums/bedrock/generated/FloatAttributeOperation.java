// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum FloatAttributeOperation {

    OVERRIDE(0),
    ALPHA_BLEND(1),
    ADD(2),
    SUBTRACT(3),
    MULTIPLY(4),
    MINIMUM(5),
    MAXIMUM(6),
    ;

    private static final Int2ObjectMap<FloatAttributeOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (FloatAttributeOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static FloatAttributeOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static FloatAttributeOperation getByValue(final int value, final FloatAttributeOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static FloatAttributeOperation getByName(final String name) {
        for (FloatAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static FloatAttributeOperation getByName(final String name, final FloatAttributeOperation fallback) {
        for (FloatAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    FloatAttributeOperation(final FloatAttributeOperation value) {
        this(value.value);
    }

    FloatAttributeOperation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
