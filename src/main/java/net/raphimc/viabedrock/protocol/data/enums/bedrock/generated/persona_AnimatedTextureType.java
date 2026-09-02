// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum persona_AnimatedTextureType {

    Face(1),
    Body32x32(2),
    Body128x128(3),
    ;

    private static final Int2ObjectMap<persona_AnimatedTextureType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (persona_AnimatedTextureType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static persona_AnimatedTextureType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static persona_AnimatedTextureType getByValue(final int value, final persona_AnimatedTextureType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static persona_AnimatedTextureType getByName(final String name) {
        for (persona_AnimatedTextureType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static persona_AnimatedTextureType getByName(final String name, final persona_AnimatedTextureType fallback) {
        for (persona_AnimatedTextureType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    persona_AnimatedTextureType(final persona_AnimatedTextureType value) {
        this(value.value);
    }

    persona_AnimatedTextureType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
