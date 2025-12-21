// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SoftEnumUpdateType {

    Add(0),
    Remove(1),
    Replace(2),
    ;

    private static final Int2ObjectMap<SoftEnumUpdateType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SoftEnumUpdateType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SoftEnumUpdateType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SoftEnumUpdateType getByValue(final int value, final SoftEnumUpdateType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SoftEnumUpdateType getByName(final String name) {
        for (SoftEnumUpdateType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SoftEnumUpdateType getByName(final String name, final SoftEnumUpdateType fallback) {
        for (SoftEnumUpdateType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SoftEnumUpdateType(final SoftEnumUpdateType value) {
        this(value.value);
    }

    SoftEnumUpdateType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
