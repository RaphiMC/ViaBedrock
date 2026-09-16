// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AttributeModifierOperation {

    operation_addition(0),
    operation_multiply_base(1),
    operation_multiply_total(2),
    operation_cap(3),
    total_operations(4),
    operation_invalid(4),
    ;

    private static final Int2ObjectMap<AttributeModifierOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AttributeModifierOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static AttributeModifierOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AttributeModifierOperation getByValue(final int value, final AttributeModifierOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static AttributeModifierOperation getByName(final String name) {
        for (AttributeModifierOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static AttributeModifierOperation getByName(final String name, final AttributeModifierOperation fallback) {
        for (AttributeModifierOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    AttributeModifierOperation(final AttributeModifierOperation value) {
        this(value.value);
    }

    AttributeModifierOperation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
