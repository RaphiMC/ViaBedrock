// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EAS_BoolAttributeOperation {

    OVERRIDE(0),
    ALPHA_BLEND(1),
    AND(2),
    NAND(3),
    OR(4),
    NOR(5),
    XOR(6),
    XNOR(7),
    ;

    private static final Int2ObjectMap<EAS_BoolAttributeOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (EAS_BoolAttributeOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static EAS_BoolAttributeOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static EAS_BoolAttributeOperation getByValue(final int value, final EAS_BoolAttributeOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static EAS_BoolAttributeOperation getByName(final String name) {
        for (EAS_BoolAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static EAS_BoolAttributeOperation getByName(final String name, final EAS_BoolAttributeOperation fallback) {
        for (EAS_BoolAttributeOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    EAS_BoolAttributeOperation(final EAS_BoolAttributeOperation value) {
        this(value.value);
    }

    EAS_BoolAttributeOperation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
