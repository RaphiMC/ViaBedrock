// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseOnActorInventoryTransaction_ActionType {

    /**
     * Right click interact with actor.
     */
    Interact(0),
    /**
     * Left click style attack of actor or elytra spin attack.<br>
     * Server is expected to deal damage to the entity with visuals.
     */
    Attack(1),
    /**
     * Unused
     */
    ItemInteract(2);

    private static final Int2ObjectMap<ItemUseOnActorInventoryTransaction_ActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseOnActorInventoryTransaction_ActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ItemUseOnActorInventoryTransaction_ActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseOnActorInventoryTransaction_ActionType getByValue(final int value, final ItemUseOnActorInventoryTransaction_ActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ItemUseOnActorInventoryTransaction_ActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
