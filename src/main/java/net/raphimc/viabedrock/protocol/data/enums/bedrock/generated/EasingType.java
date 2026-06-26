// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EasingType {

    Linear(0),
    Spring(1),
    In_quad(2),
    Out_quad(3),
    In_out_quad(4),
    In_cubic(5),
    Out_cubic(6),
    In_out_cubic(7),
    In_quart(8),
    Out_quart(9),
    In_out_quart(10),
    In_quint(11),
    Out_quint(12),
    In_out_quint(13),
    In_sine(14),
    Out_sine(15),
    In_out_sine(16),
    In_expo(17),
    Out_expo(18),
    In_out_expo(19),
    In_circ(20),
    Out_circ(21),
    In_out_circ(22),
    In_bounce(23),
    Out_bounce(24),
    In_out_bounce(25),
    In_back(26),
    Out_back(27),
    In_out_back(28),
    In_elastic(29),
    Out_elastic(30),
    In_out_elastic(31),
    ;

    private static final Int2ObjectMap<EasingType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (EasingType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static EasingType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static EasingType getByValue(final int value, final EasingType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static EasingType getByName(final String name) {
        for (EasingType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static EasingType getByName(final String name, final EasingType fallback) {
        for (EasingType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    EasingType(final EasingType value) {
        this(value.value);
    }

    EasingType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
