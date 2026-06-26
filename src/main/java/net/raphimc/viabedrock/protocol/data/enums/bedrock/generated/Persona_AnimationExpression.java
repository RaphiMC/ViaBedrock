// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Persona_AnimationExpression {

    Linear(0),
    Blinking(1),
    ;

    private static final Int2ObjectMap<Persona_AnimationExpression> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Persona_AnimationExpression value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Persona_AnimationExpression getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Persona_AnimationExpression getByValue(final int value, final Persona_AnimationExpression fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Persona_AnimationExpression getByName(final String name) {
        for (Persona_AnimationExpression value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Persona_AnimationExpression getByName(final String name, final Persona_AnimationExpression fallback) {
        for (Persona_AnimationExpression value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Persona_AnimationExpression(final Persona_AnimationExpression value) {
        this(value.value);
    }

    Persona_AnimationExpression(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
