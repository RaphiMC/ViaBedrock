// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AttributeModifierOperation {

    OPERATION_ADDITION(0),
    OPERATION_MULTIPLY_BASE(1),
    OPERATION_MULTIPLY_TOTAL(2),
    OPERATION_CAP(3),
    OPERATION_INVALID(4);

    private static final Int2ObjectMap<AttributeModifierOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AttributeModifierOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static AttributeModifierOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AttributeModifierOperation getByValue(final int value, final AttributeModifierOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
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
