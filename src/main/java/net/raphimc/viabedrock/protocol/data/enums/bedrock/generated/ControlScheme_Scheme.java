// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ControlScheme_Scheme {

    locked_player_relative_strafe(0),
    camera_relative(1),
    camera_relative_strafe(2),
    player_relative(3),
    player_relative_strafe(4);

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

    ControlScheme_Scheme(final ControlScheme_Scheme value) {
        this(value.value);
    }

    ControlScheme_Scheme(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
