// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemReleaseActionType {

    Release(0),
    Use(1),
    ;

    private static final Int2ObjectMap<ItemReleaseActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemReleaseActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemReleaseActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemReleaseActionType getByValue(final int value, final ItemReleaseActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemReleaseActionType getByName(final String name) {
        for (ItemReleaseActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemReleaseActionType getByName(final String name, final ItemReleaseActionType fallback) {
        for (ItemReleaseActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemReleaseActionType(final ItemReleaseActionType value) {
        this(value.value);
    }

    ItemReleaseActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
