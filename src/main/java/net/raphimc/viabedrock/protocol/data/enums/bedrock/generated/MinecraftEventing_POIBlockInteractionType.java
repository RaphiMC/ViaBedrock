// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MinecraftEventing_POIBlockInteractionType {

    None(0),
    Extend(1),
    Clone(2),
    Lock(3),
    Create(4),
    CreateLocator(5),
    Rename(6),
    ItemPlaced(7),
    ItemRemoved(8),
    Cooking(9),
    Dousing(10),
    Lighting(11),
    Haystack(12),
    Filled(13),
    Emptied(14),
    AddDye(15),
    DyeItem(16),
    ClearItem(17),
    EnchantArrow(18),
    CompostItemPlaced(19),
    RecoveredBonemeal(20),
    BookPlaced(21),
    BookOpened(22),
    Disenchant(23),
    Repair(24),
    DisenchantAndRepair(25);

    private static final Int2ObjectMap<MinecraftEventing_POIBlockInteractionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MinecraftEventing_POIBlockInteractionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static MinecraftEventing_POIBlockInteractionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MinecraftEventing_POIBlockInteractionType getByValue(final int value, final MinecraftEventing_POIBlockInteractionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    MinecraftEventing_POIBlockInteractionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
