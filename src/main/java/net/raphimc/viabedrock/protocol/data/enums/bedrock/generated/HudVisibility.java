// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum HudVisibility {

    Hide(0),
    Reset(1),
    ;

    private static final Int2ObjectMap<HudVisibility> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (HudVisibility value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static HudVisibility getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static HudVisibility getByValue(final int value, final HudVisibility fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static HudVisibility getByName(final String name) {
        for (HudVisibility value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static HudVisibility getByName(final String name, final HudVisibility fallback) {
        for (HudVisibility value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    HudVisibility(final HudVisibility value) {
        this(value.value);
    }

    HudVisibility(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
