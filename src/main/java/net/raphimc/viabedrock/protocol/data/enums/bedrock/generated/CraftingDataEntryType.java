// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CraftingDataEntryType {

    ShapelessRecipe(0),
    ShapedRecipe(1),
    FurnaceRecipe(2),
    FurnaceAuxRecipe(3),
    MultiRecipe(4),
    UserDataShapelessRecipe(5),
    ShapelessChemistryRecipe(6),
    ShapedChemistryRecipe(7),
    SmithingTransformRecipe(8),
    SmithingTrimRecipe(9),
    ;

    private static final Int2ObjectMap<CraftingDataEntryType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CraftingDataEntryType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CraftingDataEntryType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CraftingDataEntryType getByValue(final int value, final CraftingDataEntryType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CraftingDataEntryType getByName(final String name) {
        for (CraftingDataEntryType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CraftingDataEntryType getByName(final String name, final CraftingDataEntryType fallback) {
        for (CraftingDataEntryType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    CraftingDataEntryType(final CraftingDataEntryType value) {
        this(value.value);
    }

    CraftingDataEntryType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
