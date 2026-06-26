// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_v1_21_10_CoordinateEvaluationOrder {

    XYZ(0),
    XZY(1),
    YXZ(2),
    YZX(3),
    ZXY(4),
    ZYX(5),
    ;

    private static final Int2ObjectMap<SharedTypes_v1_21_10_CoordinateEvaluationOrder> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_v1_21_10_CoordinateEvaluationOrder value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SharedTypes_v1_21_10_CoordinateEvaluationOrder getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_v1_21_10_CoordinateEvaluationOrder getByValue(final int value, final SharedTypes_v1_21_10_CoordinateEvaluationOrder fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SharedTypes_v1_21_10_CoordinateEvaluationOrder getByName(final String name) {
        for (SharedTypes_v1_21_10_CoordinateEvaluationOrder value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SharedTypes_v1_21_10_CoordinateEvaluationOrder getByName(final String name, final SharedTypes_v1_21_10_CoordinateEvaluationOrder fallback) {
        for (SharedTypes_v1_21_10_CoordinateEvaluationOrder value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SharedTypes_v1_21_10_CoordinateEvaluationOrder(final SharedTypes_v1_21_10_CoordinateEvaluationOrder value) {
        this(value.value);
    }

    SharedTypes_v1_21_10_CoordinateEvaluationOrder(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
