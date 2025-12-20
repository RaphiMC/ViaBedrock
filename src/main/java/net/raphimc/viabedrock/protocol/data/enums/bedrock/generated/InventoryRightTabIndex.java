// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum InventoryRightTabIndex {

    None(0),
    FullScreen(1),
    Crafting(2),
    Armor(3),
    ;

    private static final Int2ObjectMap<InventoryRightTabIndex> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InventoryRightTabIndex value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static InventoryRightTabIndex getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InventoryRightTabIndex getByValue(final int value, final InventoryRightTabIndex fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static InventoryRightTabIndex getByName(final String name) {
        for (InventoryRightTabIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static InventoryRightTabIndex getByName(final String name, final InventoryRightTabIndex fallback) {
        for (InventoryRightTabIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    InventoryRightTabIndex(final InventoryRightTabIndex value) {
        this(value.value);
    }

    InventoryRightTabIndex(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
