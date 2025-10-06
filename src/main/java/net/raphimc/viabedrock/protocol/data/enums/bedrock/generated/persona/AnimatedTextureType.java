// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.persona;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AnimatedTextureType {

    None(0),
    Face(1),
    Body32x32(2),
    Body128x128(3);

    private static final Int2ObjectMap<AnimatedTextureType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AnimatedTextureType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static AnimatedTextureType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AnimatedTextureType getByValue(final int value, final AnimatedTextureType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    AnimatedTextureType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
