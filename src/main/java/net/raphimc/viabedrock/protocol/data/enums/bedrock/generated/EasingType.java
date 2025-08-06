// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EasingType {

    Linear(0),
    Spring(1),
    InQuad(2),
    OutQuad(3),
    InOutQuad(4),
    InCubic(5),
    OutCubic(6),
    InOutCubic(7),
    InQuart(8),
    OutQuart(9),
    InOutQuart(10),
    InQuint(11),
    OutQuint(12),
    InOutQuint(13),
    InSine(14),
    OutSine(15),
    InOutSine(16),
    InExpo(17),
    OutExpo(18),
    InOutExpo(19),
    InCirc(20),
    OutCirc(21),
    InOutCirc(22),
    InBounce(23),
    OutBounce(24),
    InOutBounce(25),
    InBack(26),
    OutBack(27),
    InOutBack(28),
    InElastic(29),
    OutElastic(30),
    InOutElastic(31),
    _Invalid(33);

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

    EasingType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
