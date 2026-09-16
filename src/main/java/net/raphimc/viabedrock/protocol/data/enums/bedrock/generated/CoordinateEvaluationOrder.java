// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CoordinateEvaluationOrder {

    XYZ(0),
    XZY(1),
    YXZ(2),
    YZX(3),
    ZXY(4),
    ZYX(5),
    ;

    private static final Int2ObjectMap<CoordinateEvaluationOrder> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CoordinateEvaluationOrder value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CoordinateEvaluationOrder getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CoordinateEvaluationOrder getByValue(final int value, final CoordinateEvaluationOrder fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CoordinateEvaluationOrder getByName(final String name) {
        for (CoordinateEvaluationOrder value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CoordinateEvaluationOrder getByName(final String name, final CoordinateEvaluationOrder fallback) {
        for (CoordinateEvaluationOrder value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    CoordinateEvaluationOrder(final CoordinateEvaluationOrder value) {
        this(value.value);
    }

    CoordinateEvaluationOrder(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
