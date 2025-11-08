// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MovementEffectType {

    GLIDE_BOOST(0),
    DOLPHIN_BOOST(1);

    private static final Int2ObjectMap<MovementEffectType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MovementEffectType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static MovementEffectType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MovementEffectType getByValue(final int value, final MovementEffectType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    MovementEffectType(final MovementEffectType value) {
        this(value.value);
    }

    MovementEffectType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
