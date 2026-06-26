// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Persona_ArmSize_Type {

    Slim(0),
    Wide(1),
    ;

    private static final Int2ObjectMap<Persona_ArmSize_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Persona_ArmSize_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Persona_ArmSize_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Persona_ArmSize_Type getByValue(final int value, final Persona_ArmSize_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Persona_ArmSize_Type getByName(final String name) {
        for (Persona_ArmSize_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Persona_ArmSize_Type getByName(final String name, final Persona_ArmSize_Type fallback) {
        for (Persona_ArmSize_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Persona_ArmSize_Type(final Persona_ArmSize_Type value) {
        this(value.value);
    }

    Persona_ArmSize_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
