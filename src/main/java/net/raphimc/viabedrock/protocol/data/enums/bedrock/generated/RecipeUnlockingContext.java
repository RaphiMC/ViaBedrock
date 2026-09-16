// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum RecipeUnlockingContext {

    None(0),
    AlwaysUnlocked(1),
    PlayerInWater(2),
    PlayerHasManyItems(3),
    ;

    private static final Int2ObjectMap<RecipeUnlockingContext> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (RecipeUnlockingContext value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static RecipeUnlockingContext getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static RecipeUnlockingContext getByValue(final int value, final RecipeUnlockingContext fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static RecipeUnlockingContext getByName(final String name) {
        for (RecipeUnlockingContext value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static RecipeUnlockingContext getByName(final String name, final RecipeUnlockingContext fallback) {
        for (RecipeUnlockingContext value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    RecipeUnlockingContext(final RecipeUnlockingContext value) {
        this(value.value);
    }

    RecipeUnlockingContext(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
