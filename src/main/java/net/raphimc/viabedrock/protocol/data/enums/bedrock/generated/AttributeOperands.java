// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AttributeOperands {

    OPERAND_MIN(0),
    OPERAND_MAX(1),
    OPERAND_CURRENT(2),
    OPERAND_INVALID(3);

    private static final Int2ObjectMap<AttributeOperands> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AttributeOperands value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static AttributeOperands getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AttributeOperands getByValue(final int value, final AttributeOperands fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    AttributeOperands(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
