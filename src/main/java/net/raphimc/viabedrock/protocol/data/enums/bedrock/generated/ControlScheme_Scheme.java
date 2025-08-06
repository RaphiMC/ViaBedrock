// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ControlScheme_Scheme {

    LockedPlayerRelativeStrafe(0),
    CameraRelative(1),
    CameraRelativeStrafe(2),
    PlayerRelative(3),
    PlayerRelativeStrafe(4);

    private static final Int2ObjectMap<ControlScheme_Scheme> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ControlScheme_Scheme value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ControlScheme_Scheme getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ControlScheme_Scheme getByValue(final int value, final ControlScheme_Scheme fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ControlScheme_Scheme(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
