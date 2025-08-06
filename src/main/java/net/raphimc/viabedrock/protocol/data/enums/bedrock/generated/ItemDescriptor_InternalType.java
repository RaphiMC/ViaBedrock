// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemDescriptor_InternalType {

    Invalid(0),
    Default(1),
    Molang(2),
    ItemTag(3),
    Deferred(4),
    ComplexAlias(5);

    private static final Int2ObjectMap<ItemDescriptor_InternalType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemDescriptor_InternalType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ItemDescriptor_InternalType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemDescriptor_InternalType getByValue(final int value, final ItemDescriptor_InternalType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ItemDescriptor_InternalType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
