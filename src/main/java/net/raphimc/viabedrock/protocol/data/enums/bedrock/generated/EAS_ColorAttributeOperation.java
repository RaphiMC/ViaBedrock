// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EAS_ColorAttributeOperation {

    OVERRIDE(0),
    ALPHA_BLEND(1),
    ADD(2),
    SUBTRACT(3),
    MULTIPLY(4),
    ;

    private static final Int2ObjectMap<EAS_ColorAttributeOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (EAS_ColorAttributeOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static EAS_ColorAttributeOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static EAS_ColorAttributeOperation getByValue(final int value, final EAS_ColorAttributeOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static EAS_ColorAttributeOperation getByName(final String name) {
        for (EAS_ColorAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static EAS_ColorAttributeOperation getByName(final String name, final EAS_ColorAttributeOperation fallback) {
        for (EAS_ColorAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    EAS_ColorAttributeOperation(final EAS_ColorAttributeOperation value) {
        this(value.value);
    }

    EAS_ColorAttributeOperation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
