// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemStackRequestActionType {

    Take(0),
    Place(1),
    Swap(2),
    Drop(3),
    Destroy(4),
    Consume(5),
    Create(6),
    ScreenLabTableCombine(9),
    ScreenBeaconPayment(10),
    ScreenHUDMineBlock(11),
    CraftRecipe(12),
    CraftRecipeAuto(13),
    CraftCreative(14),
    CraftRecipeOptional(15),
    CraftRepairAndDisenchant(16),
    CraftLoom(17),
    ifdef(20),
    TEST_INFRASTRUCTURE_ENABLED(21),
    Test(22),
    endif(23);

    private static final Int2ObjectMap<ItemStackRequestActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemStackRequestActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ItemStackRequestActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemStackRequestActionType getByValue(final int value, final ItemStackRequestActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ItemStackRequestActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
