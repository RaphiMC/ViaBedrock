// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseInventoryTransaction_ClientCooldownState {

    Off(0),
    On(1),
    ;

    private static final Int2ObjectMap<ItemUseInventoryTransaction_ClientCooldownState> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseInventoryTransaction_ClientCooldownState value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemUseInventoryTransaction_ClientCooldownState getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseInventoryTransaction_ClientCooldownState getByValue(final int value, final ItemUseInventoryTransaction_ClientCooldownState fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemUseInventoryTransaction_ClientCooldownState getByName(final String name) {
        for (ItemUseInventoryTransaction_ClientCooldownState value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemUseInventoryTransaction_ClientCooldownState getByName(final String name, final ItemUseInventoryTransaction_ClientCooldownState fallback) {
        for (ItemUseInventoryTransaction_ClientCooldownState value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemUseInventoryTransaction_ClientCooldownState(final ItemUseInventoryTransaction_ClientCooldownState value) {
        this(value.value);
    }

    ItemUseInventoryTransaction_ClientCooldownState(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
