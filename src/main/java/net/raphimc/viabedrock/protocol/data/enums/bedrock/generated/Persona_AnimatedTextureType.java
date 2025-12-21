// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Persona_AnimatedTextureType {

    None(0),
    Face(1),
    Body32x32(2),
    Body128x128(3),
    ;

    private static final Int2ObjectMap<Persona_AnimatedTextureType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Persona_AnimatedTextureType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Persona_AnimatedTextureType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Persona_AnimatedTextureType getByValue(final int value, final Persona_AnimatedTextureType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Persona_AnimatedTextureType getByName(final String name) {
        for (Persona_AnimatedTextureType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Persona_AnimatedTextureType getByName(final String name, final Persona_AnimatedTextureType fallback) {
        for (Persona_AnimatedTextureType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Persona_AnimatedTextureType(final Persona_AnimatedTextureType value) {
        this(value.value);
    }

    Persona_AnimatedTextureType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
