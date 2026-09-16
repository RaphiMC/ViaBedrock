// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum DataItemType {

    byte(0),
    short(1),
    int(2),
    float(3),
    string(4),
    compoundtag(5),
    pos(6),
    int64(7),
    vec3(8),
    ;

    private static final Int2ObjectMap<DataItemType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (DataItemType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static DataItemType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static DataItemType getByValue(final int value, final DataItemType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static DataItemType getByName(final String name) {
        for (DataItemType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static DataItemType getByName(final String name, final DataItemType fallback) {
        for (DataItemType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    DataItemType(final DataItemType value) {
        this(value.value);
    }

    DataItemType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
