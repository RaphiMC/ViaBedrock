// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ObjectiveSortOrder {

    Ascending(0),
    Descending(1);

    private static final Int2ObjectMap<ObjectiveSortOrder> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ObjectiveSortOrder value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ObjectiveSortOrder getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ObjectiveSortOrder getByValue(final int value, final ObjectiveSortOrder fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ObjectiveSortOrder(final ObjectiveSortOrder value) {
        this(value.value);
    }

    ObjectiveSortOrder(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
