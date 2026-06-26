// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum InventoryLeftTabIndex {

    None(0),
    RecipeConstruction(1),
    RecipeEquipment(2),
    RecipeItems(3),
    RecipeNature(4),
    RecipeSearch(5),
    Survival(6),
    ;

    private static final Int2ObjectMap<InventoryLeftTabIndex> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InventoryLeftTabIndex value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static InventoryLeftTabIndex getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InventoryLeftTabIndex getByValue(final int value, final InventoryLeftTabIndex fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static InventoryLeftTabIndex getByName(final String name) {
        for (InventoryLeftTabIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static InventoryLeftTabIndex getByName(final String name, final InventoryLeftTabIndex fallback) {
        for (InventoryLeftTabIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    InventoryLeftTabIndex(final InventoryLeftTabIndex value) {
        this(value.value);
    }

    InventoryLeftTabIndex(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
