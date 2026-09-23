// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUsePredictedResult {

    Failure(0),
    Success(1),
    ;

    private static final Int2ObjectMap<ItemUsePredictedResult> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUsePredictedResult value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemUsePredictedResult getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUsePredictedResult getByValue(final int value, final ItemUsePredictedResult fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemUsePredictedResult getByName(final String name) {
        for (ItemUsePredictedResult value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemUsePredictedResult getByName(final String name, final ItemUsePredictedResult fallback) {
        for (ItemUsePredictedResult value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemUsePredictedResult(final ItemUsePredictedResult value) {
        this(value.value);
    }

    ItemUsePredictedResult(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
