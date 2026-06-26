// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CraftingType {

    Inventory(0),
    Crafting(1),
    ;

    private static final Int2ObjectMap<CraftingType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CraftingType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CraftingType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CraftingType getByValue(final int value, final CraftingType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CraftingType getByName(final String name) {
        for (CraftingType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CraftingType getByName(final String name, final CraftingType fallback) {
        for (CraftingType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    CraftingType(final CraftingType value) {
        this(value.value);
    }

    CraftingType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
