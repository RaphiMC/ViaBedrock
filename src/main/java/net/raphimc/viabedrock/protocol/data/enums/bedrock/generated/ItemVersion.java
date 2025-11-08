// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemVersion {

    Legacy(0),
    DataDriven(1),
    None(2);

    private static final Int2ObjectMap<ItemVersion> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemVersion value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ItemVersion getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemVersion getByValue(final int value, final ItemVersion fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ItemVersion(final ItemVersion value) {
        this(value.value);
    }

    ItemVersion(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
