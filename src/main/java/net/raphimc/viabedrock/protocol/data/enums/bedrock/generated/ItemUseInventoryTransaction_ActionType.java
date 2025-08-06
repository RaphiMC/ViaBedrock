// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseInventoryTransaction_ActionType {

    /**
     * Right click item use on a surface like placing a block
     */
    Place(0),
    /**
     * Start right click and hold style item use or potentially interact with nothing.<br>
     * If it is a usable item like food the server is expected to send a SetActorDataPacket with ActorFlags::USINGITEM along with the transaction response.<br>
     * While using an item, movement speed is slowed which will be reflected in the move vector in Player Auth Input.
     */
    Use(1),
    /**
     * Block breaking like left click<br>
     * When using server auth block breaking as specified in StartGamePacket this is never sent.<br>
     * Instead, block actions are supplied in Player Auth Input.
     */
    Destroy(2);

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
