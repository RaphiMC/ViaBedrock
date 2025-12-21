// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum InventorySourceType {

    InvalidInventory(Integer.MAX_VALUE),
    ContainerInventory(0),
    GlobalInventory(1),
    WorldInteraction(2),
    CreativeInventory(3),
    NonImplementedFeatureTODO(99999),
    ;

    private static final Int2ObjectMap<InventorySourceType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InventorySourceType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static InventorySourceType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InventorySourceType getByValue(final int value, final InventorySourceType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static InventorySourceType getByName(final String name) {
        for (InventorySourceType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static InventorySourceType getByName(final String name, final InventorySourceType fallback) {
        for (InventorySourceType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    InventorySourceType(final InventorySourceType value) {
        this(value.value);
    }

    InventorySourceType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
