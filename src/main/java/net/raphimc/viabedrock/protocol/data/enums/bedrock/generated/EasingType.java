// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EasingType {

    linear(0),
    spring(1),
    in_quad(2),
    out_quad(3),
    in_out_quad(4),
    in_cubic(5),
    out_cubic(6),
    in_out_cubic(7),
    in_quart(8),
    out_quart(9),
    in_out_quart(10),
    in_quint(11),
    out_quint(12),
    in_out_quint(13),
    in_sine(14),
    out_sine(15),
    in_out_sine(16),
    in_expo(17),
    out_expo(18),
    in_out_expo(19),
    in_circ(20),
    out_circ(21),
    in_out_circ(22),
    in_bounce(23),
    out_bounce(24),
    in_out_bounce(25),
    in_back(26),
    out_back(27),
    in_out_back(28),
    in_elastic(29),
    out_elastic(30),
    in_out_elastic(31);

    private static final Int2ObjectMap<EasingType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (EasingType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static EasingType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static EasingType getByValue(final int value, final EasingType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
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
