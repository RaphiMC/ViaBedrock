// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ComplexInventoryTransaction_Type {

    /**
     * Sent for container UI operations depending on if ItemStackNetManager is enabled
     */
    NormalTransaction(0),
    /**
     * Sent from server to client to reject a transaction
     */
    InventoryMismatch(1),
    /**
     * Sent for a player performing right click style item use. See the contained ItemUseInventoryTransaction::ActionType for the expected use case.
     */
    ItemUseTransaction(2),
    /**
     * Sent for a player right clicking on an entity or attacking them. See ItemUseInventoryTransaction::ActionType for which it is.
     */
    ItemUseOnEntityTransaction(3),
    /**
     * Sent when releasing right click on a chargeable item like a bow or finishing charging like a crossbow. This is different than canceling item use early which would be in Player Auth Input.<br>
     * See ItemReleaseInventoryTransaction::ActionType for which it is.
     */
    ItemReleaseTransaction(4);

    private static final Int2ObjectMap<ComplexInventoryTransaction_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ComplexInventoryTransaction_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ComplexInventoryTransaction_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ComplexInventoryTransaction_Type getByValue(final int value, final ComplexInventoryTransaction_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ComplexInventoryTransaction_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
