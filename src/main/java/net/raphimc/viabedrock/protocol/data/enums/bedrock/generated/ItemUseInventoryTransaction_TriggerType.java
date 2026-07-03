// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseInventoryTransaction_TriggerType {

    Unknown(0),
    PlayerInput(1),
    SimulationTick(2),
    ;

    private static final Int2ObjectMap<ItemUseInventoryTransaction_TriggerType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseInventoryTransaction_TriggerType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemUseInventoryTransaction_TriggerType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseInventoryTransaction_TriggerType getByValue(final int value, final ItemUseInventoryTransaction_TriggerType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemUseInventoryTransaction_TriggerType getByName(final String name) {
        for (ItemUseInventoryTransaction_TriggerType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemUseInventoryTransaction_TriggerType getByName(final String name, final ItemUseInventoryTransaction_TriggerType fallback) {
        for (ItemUseInventoryTransaction_TriggerType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemUseInventoryTransaction_TriggerType(final ItemUseInventoryTransaction_TriggerType value) {
        this(value.value);
    }

    ItemUseInventoryTransaction_TriggerType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
