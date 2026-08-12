// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_CreativeItemCategory {

    All(0),
    Construction(1),
    Nature(2),
    Equipment(3),
    Items(4),
    ItemCommandOnly(5),
    Undefined(6),
    ;

    private static final Int2ObjectMap<SharedTypes_CreativeItemCategory> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_CreativeItemCategory value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SharedTypes_CreativeItemCategory getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_CreativeItemCategory getByValue(final int value, final SharedTypes_CreativeItemCategory fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SharedTypes_CreativeItemCategory getByName(final String name) {
        for (SharedTypes_CreativeItemCategory value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SharedTypes_CreativeItemCategory getByName(final String name, final SharedTypes_CreativeItemCategory fallback) {
        for (SharedTypes_CreativeItemCategory value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SharedTypes_CreativeItemCategory(final SharedTypes_CreativeItemCategory value) {
        this(value.value);
    }

    SharedTypes_CreativeItemCategory(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
