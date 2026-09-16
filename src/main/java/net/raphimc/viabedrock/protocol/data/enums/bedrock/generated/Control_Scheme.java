// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Control_Scheme {

    locked_player_relative_strafe(0),
    camera_relative(1),
    camera_relative_strafe(2),
    player_relative(3),
    player_relative_strafe(4),
    ;

    private static final Int2ObjectMap<Control_Scheme> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Control_Scheme value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Control_Scheme getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Control_Scheme getByValue(final int value, final Control_Scheme fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Control_Scheme getByName(final String name) {
        for (Control_Scheme value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Control_Scheme getByName(final String name, final Control_Scheme fallback) {
        for (Control_Scheme value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Control_Scheme(final Control_Scheme value) {
        this(value.value);
    }

    Control_Scheme(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
