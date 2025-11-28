// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.persona;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AnimationExpression {

    Linear(0),
    Blinking(1);

    private static final Int2ObjectMap<AnimationExpression> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AnimationExpression value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static AnimationExpression getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AnimationExpression getByValue(final int value, final AnimationExpression fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    AnimationExpression(final AnimationExpression value) {
        this(value.value);
    }

    AnimationExpression(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
