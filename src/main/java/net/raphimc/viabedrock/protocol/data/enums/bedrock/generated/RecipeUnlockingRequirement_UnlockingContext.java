// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum RecipeUnlockingRequirement_UnlockingContext {

    None(0),
    AlwaysUnlocked(1),
    PlayerInWater(2),
    PlayerHasManyItems(3);

    private static final Int2ObjectMap<RecipeUnlockingRequirement_UnlockingContext> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (RecipeUnlockingRequirement_UnlockingContext value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static RecipeUnlockingRequirement_UnlockingContext getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static RecipeUnlockingRequirement_UnlockingContext getByValue(final int value, final RecipeUnlockingRequirement_UnlockingContext fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    RecipeUnlockingRequirement_UnlockingContext(final RecipeUnlockingRequirement_UnlockingContext value) {
        this(value.value);
    }

    RecipeUnlockingRequirement_UnlockingContext(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
