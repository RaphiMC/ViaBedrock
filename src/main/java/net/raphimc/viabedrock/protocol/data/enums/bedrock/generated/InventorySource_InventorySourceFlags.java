// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum InventorySource_InventorySourceFlags {

    NoFlag(0),
    WorldInteraction_Random(1);

    private static final Int2ObjectMap<InventorySource_InventorySourceFlags> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InventorySource_InventorySourceFlags value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static InventorySource_InventorySourceFlags getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InventorySource_InventorySourceFlags getByValue(final int value, final InventorySource_InventorySourceFlags fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    InventorySource_InventorySourceFlags(final InventorySource_InventorySourceFlags value) {
        this(value.value);
    }

    InventorySource_InventorySourceFlags(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
