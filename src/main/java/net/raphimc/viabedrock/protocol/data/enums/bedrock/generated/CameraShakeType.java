// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CameraShakeType {

    Positional(0),
    Rotational(1);

    private static final Int2ObjectMap<CameraShakeType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CameraShakeType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CameraShakeType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CameraShakeType getByValue(final int value, final CameraShakeType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CameraShakeType(final CameraShakeType value) {
        this(value.value);
    }

    CameraShakeType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
