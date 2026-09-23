// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum FurnaceLayout {

    None(0),
    InventoryOnly(1),
    Default(2),
    ;

    private static final Int2ObjectMap<FurnaceLayout> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (FurnaceLayout value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static FurnaceLayout getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static FurnaceLayout getByValue(final int value, final FurnaceLayout fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static FurnaceLayout getByName(final String name) {
        for (FurnaceLayout value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static FurnaceLayout getByName(final String name, final FurnaceLayout fallback) {
        for (FurnaceLayout value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    FurnaceLayout(final FurnaceLayout value) {
        this(value.value);
    }

    FurnaceLayout(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
