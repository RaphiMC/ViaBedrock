// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemStackRequestCereal_ItemDescriptorType {

    Empty(0),
    ItemName(1),
    Molang(2),
    ItemTag(3),
    ;

    private static final Int2ObjectMap<ItemStackRequestCereal_ItemDescriptorType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemStackRequestCereal_ItemDescriptorType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemStackRequestCereal_ItemDescriptorType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemStackRequestCereal_ItemDescriptorType getByValue(final int value, final ItemStackRequestCereal_ItemDescriptorType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemStackRequestCereal_ItemDescriptorType getByName(final String name) {
        for (ItemStackRequestCereal_ItemDescriptorType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemStackRequestCereal_ItemDescriptorType getByName(final String name, final ItemStackRequestCereal_ItemDescriptorType fallback) {
        for (ItemStackRequestCereal_ItemDescriptorType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemStackRequestCereal_ItemDescriptorType(final ItemStackRequestCereal_ItemDescriptorType value) {
        this(value.value);
    }

    ItemStackRequestCereal_ItemDescriptorType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
