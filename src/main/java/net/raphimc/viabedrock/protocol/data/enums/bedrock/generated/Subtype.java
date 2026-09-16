// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Subtype {

    UninitializedSubtype(0),
    EnableCommands(1),
    DisableCommands(2),
    UnlockWorldTemplateSettings(3),
    ;

    private static final Int2ObjectMap<Subtype> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Subtype value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Subtype getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Subtype getByValue(final int value, final Subtype fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Subtype getByName(final String name) {
        for (Subtype value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Subtype getByName(final String name, final Subtype fallback) {
        for (Subtype value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Subtype(final Subtype value) {
        this(value.value);
    }

    Subtype(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
