// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_persona_ArmSizeType {

    Slim(0),
    Wide(1),
    ;

    private static final Int2ObjectMap<SharedTypes_persona_ArmSizeType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_persona_ArmSizeType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SharedTypes_persona_ArmSizeType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_persona_ArmSizeType getByValue(final int value, final SharedTypes_persona_ArmSizeType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SharedTypes_persona_ArmSizeType getByName(final String name) {
        for (SharedTypes_persona_ArmSizeType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SharedTypes_persona_ArmSizeType getByName(final String name, final SharedTypes_persona_ArmSizeType fallback) {
        for (SharedTypes_persona_ArmSizeType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SharedTypes_persona_ArmSizeType(final SharedTypes_persona_ArmSizeType value) {
        this(value.value);
    }

    SharedTypes_persona_ArmSizeType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
