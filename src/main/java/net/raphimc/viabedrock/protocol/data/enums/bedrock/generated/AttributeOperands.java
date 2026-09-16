// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AttributeOperands {

    operand_min(0),
    operand_max(1),
    operand_current(2),
    total_operands(3),
    operand_invalid(3),
    ;

    private static final Int2ObjectMap<AttributeOperands> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AttributeOperands value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static AttributeOperands getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AttributeOperands getByValue(final int value, final AttributeOperands fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static AttributeOperands getByName(final String name) {
        for (AttributeOperands value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static AttributeOperands getByName(final String name, final AttributeOperands fallback) {
        for (AttributeOperands value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    AttributeOperands(final AttributeOperands value) {
        this(value.value);
    }

    AttributeOperands(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
