// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseActionType {

    Place(0),
    Use(1),
    Destroy(2),
    Use_As_Attack(3),
    ;

    private static final Int2ObjectMap<ItemUseActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemUseActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseActionType getByValue(final int value, final ItemUseActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemUseActionType getByName(final String name) {
        for (ItemUseActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemUseActionType getByName(final String name, final ItemUseActionType fallback) {
        for (ItemUseActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemUseActionType(final ItemUseActionType value) {
        this(value.value);
    }

    ItemUseActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
