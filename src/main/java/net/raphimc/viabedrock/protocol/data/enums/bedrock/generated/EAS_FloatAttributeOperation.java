// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EAS_FloatAttributeOperation {

    OVERRIDE(0),
    ALPHA_BLEND(1),
    ADD(2),
    SUBTRACT(3),
    MULTIPLY(4),
    MINIMUM(MULTIPLY),
    MAXIMUM(MULTIPLY),
    ;

    private static final Int2ObjectMap<EAS_FloatAttributeOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (EAS_FloatAttributeOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static EAS_FloatAttributeOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static EAS_FloatAttributeOperation getByValue(final int value, final EAS_FloatAttributeOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static EAS_FloatAttributeOperation getByName(final String name) {
        for (EAS_FloatAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static EAS_FloatAttributeOperation getByName(final String name, final EAS_FloatAttributeOperation fallback) {
        for (EAS_FloatAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    EAS_FloatAttributeOperation(final EAS_FloatAttributeOperation value) {
        this(value.value);
    }

    EAS_FloatAttributeOperation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
