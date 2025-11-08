// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseInventoryTransaction_ActionType {

    Place(0),
    Use(1),
    Destroy(2),
    UseAsAttack(3);

    private static final Int2ObjectMap<ItemUseInventoryTransaction_ActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseInventoryTransaction_ActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ItemUseInventoryTransaction_ActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseInventoryTransaction_ActionType getByValue(final int value, final ItemUseInventoryTransaction_ActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ItemUseInventoryTransaction_ActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
