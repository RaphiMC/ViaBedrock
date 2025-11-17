// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseInventoryTransaction_PredictedResult {

    Failure(0),
    Success(1);

    private static final Int2ObjectMap<ItemUseInventoryTransaction_PredictedResult> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseInventoryTransaction_PredictedResult value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ItemUseInventoryTransaction_PredictedResult getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseInventoryTransaction_PredictedResult getByValue(final int value, final ItemUseInventoryTransaction_PredictedResult fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ItemUseInventoryTransaction_PredictedResult(final ItemUseInventoryTransaction_PredictedResult value) {
        this(value.value);
    }

    ItemUseInventoryTransaction_PredictedResult(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
