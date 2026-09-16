// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum FurnaceLeftTabIndex {

    none(0),
    recipefood(1),
    recipeitems(2),
    recipeblocks(3),
    recipesearch(4),
    inventory(5),
    ;

    private static final Int2ObjectMap<FurnaceLeftTabIndex> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (FurnaceLeftTabIndex value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static FurnaceLeftTabIndex getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static FurnaceLeftTabIndex getByValue(final int value, final FurnaceLeftTabIndex fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static FurnaceLeftTabIndex getByName(final String name) {
        for (FurnaceLeftTabIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static FurnaceLeftTabIndex getByName(final String name, final FurnaceLeftTabIndex fallback) {
        for (FurnaceLeftTabIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    FurnaceLeftTabIndex(final FurnaceLeftTabIndex value) {
        this(value.value);
    }

    FurnaceLeftTabIndex(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
