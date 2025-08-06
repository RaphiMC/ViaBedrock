// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemReleaseInventoryTransaction_ActionType {

    /**
     * Release right click and hold style item use, like firing a bow
     */
    Release(0),
    /**
     * Finish right click and hold style item use, like charging a crossbow
     */
    Use(1);

    private static final Int2ObjectMap<ItemReleaseInventoryTransaction_ActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemReleaseInventoryTransaction_ActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ItemReleaseInventoryTransaction_ActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemReleaseInventoryTransaction_ActionType getByValue(final int value, final ItemReleaseInventoryTransaction_ActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ItemReleaseInventoryTransaction_ActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
