// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum InventorySourceFlags {

    No_Flag(0),
    World_Interaction_Random(1),
    ;

    private static final Int2ObjectMap<InventorySourceFlags> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InventorySourceFlags value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static InventorySourceFlags getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InventorySourceFlags getByValue(final int value, final InventorySourceFlags fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static InventorySourceFlags getByName(final String name) {
        for (InventorySourceFlags value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static InventorySourceFlags getByName(final String name, final InventorySourceFlags fallback) {
        for (InventorySourceFlags value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    InventorySourceFlags(final InventorySourceFlags value) {
        this(value.value);
    }

    InventorySourceFlags(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
