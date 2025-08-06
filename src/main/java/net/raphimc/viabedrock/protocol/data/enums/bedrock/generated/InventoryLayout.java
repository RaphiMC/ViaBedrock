// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum InventoryLayout {

    None(0),
    Survival(1),
    RecipeBook(2),
    Creative(3);

    private static final Int2ObjectMap<InventoryLayout> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InventoryLayout value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static InventoryLayout getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InventoryLayout getByValue(final int value, final InventoryLayout fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    InventoryLayout(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
