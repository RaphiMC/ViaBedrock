// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MinecraftEventing_POIBlockInteractionType {

    none(0),
    extend(1),
    clone(2),
    lock(3),
    create(4),
    createlocator(5),
    rename(6),
    itemplaced(7),
    itemremoved(8),
    cooking(9),
    dousing(10),
    lighting(11),
    haystack(12),
    filled(13),
    emptied(14),
    adddye(15),
    dyeitem(16),
    clearitem(17),
    enchantarrow(18),
    compostitemplaced(19),
    recoveredbonemeal(20),
    bookplaced(21),
    bookopened(22),
    disenchant(23),
    repair(24),
    disenchantandrepair(25),
    ;

    private static final Int2ObjectMap<MinecraftEventing_POIBlockInteractionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MinecraftEventing_POIBlockInteractionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static MinecraftEventing_POIBlockInteractionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MinecraftEventing_POIBlockInteractionType getByValue(final int value, final MinecraftEventing_POIBlockInteractionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static MinecraftEventing_POIBlockInteractionType getByName(final String name) {
        for (MinecraftEventing_POIBlockInteractionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static MinecraftEventing_POIBlockInteractionType getByName(final String name, final MinecraftEventing_POIBlockInteractionType fallback) {
        for (MinecraftEventing_POIBlockInteractionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    MinecraftEventing_POIBlockInteractionType(final MinecraftEventing_POIBlockInteractionType value) {
        this(value.value);
    }

    MinecraftEventing_POIBlockInteractionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
