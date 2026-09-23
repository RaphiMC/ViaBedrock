// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PayloadType {

    Invalid(0),
    ClearDebugMarkers(1),
    AddDebugMarkerCube(2),
    ;

    private static final Int2ObjectMap<PayloadType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PayloadType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PayloadType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PayloadType getByValue(final int value, final PayloadType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PayloadType getByName(final String name) {
        for (PayloadType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PayloadType getByName(final String name, final PayloadType fallback) {
        for (PayloadType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PayloadType(final PayloadType value) {
        this(value.value);
    }

    PayloadType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
