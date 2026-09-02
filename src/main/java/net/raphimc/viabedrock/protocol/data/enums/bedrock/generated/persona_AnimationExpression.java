// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum persona_AnimationExpression {

    Linear(0),
    Blinking(1),
    ;

    private static final Int2ObjectMap<persona_AnimationExpression> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (persona_AnimationExpression value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static persona_AnimationExpression getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static persona_AnimationExpression getByValue(final int value, final persona_AnimationExpression fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static persona_AnimationExpression getByName(final String name) {
        for (persona_AnimationExpression value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static persona_AnimationExpression getByName(final String name, final persona_AnimationExpression fallback) {
        for (persona_AnimationExpression value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    persona_AnimationExpression(final persona_AnimationExpression value) {
        this(value.value);
    }

    persona_AnimationExpression(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
