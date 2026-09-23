// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum BoolAttributeOperation {

    OVERRIDE(0),
    ALPHA_BLEND(1),
    AND(2),
    NAND(3),
    OR(4),
    NOR(5),
    XOR(6),
    XNOR(7),
    ;

    private static final Int2ObjectMap<BoolAttributeOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (BoolAttributeOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static BoolAttributeOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static BoolAttributeOperation getByValue(final int value, final BoolAttributeOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static BoolAttributeOperation getByName(final String name) {
        for (BoolAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static BoolAttributeOperation getByName(final String name, final BoolAttributeOperation fallback) {
        for (BoolAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    BoolAttributeOperation(final BoolAttributeOperation value) {
        this(value.value);
    }

    BoolAttributeOperation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
