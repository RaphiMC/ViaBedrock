// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum persona_ArmSize_Type {

    Slim(0),
    Wide(1),
    ;

    private static final Int2ObjectMap<persona_ArmSize_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (persona_ArmSize_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static persona_ArmSize_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static persona_ArmSize_Type getByValue(final int value, final persona_ArmSize_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static persona_ArmSize_Type getByName(final String name) {
        for (persona_ArmSize_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static persona_ArmSize_Type getByName(final String name, final persona_ArmSize_Type fallback) {
        for (persona_ArmSize_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    persona_ArmSize_Type(final persona_ArmSize_Type value) {
        this(value.value);
    }

    persona_ArmSize_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
