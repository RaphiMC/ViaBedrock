// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum HandSlot {

    Mainhand(0),
    Offhand(1),
    ;

    private static final Int2ObjectMap<HandSlot> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (HandSlot value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static HandSlot getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static HandSlot getByValue(final int value, final HandSlot fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static HandSlot getByName(final String name) {
        for (HandSlot value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static HandSlot getByName(final String name, final HandSlot fallback) {
        for (HandSlot value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    HandSlot(final HandSlot value) {
        this(value.value);
    }

    HandSlot(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
