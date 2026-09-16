// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemStackRequestActionType {

    take(0),
    place(1),
    swap(2),
    drop(3),
    destroy(4),
    consume(5),
    create(6),
    placeinitemcontainer(7),
    takefromitemcontainer(8),
    screenlabtablecombine(9),
    screenbeaconpayment(10),
    screenhudmineblock(11),
    craftrecipe(12),
    craftrecipeauto(13),
    craftcreative(14),
    craftrecipeoptional(15),
    craftrepairanddisenchant(16),
    craftloom(17),
    craftnonimplemented(18),
    craftresults(19),
    ;

    private static final Int2ObjectMap<ItemStackRequestActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemStackRequestActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemStackRequestActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemStackRequestActionType getByValue(final int value, final ItemStackRequestActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemStackRequestActionType getByName(final String name) {
        for (ItemStackRequestActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemStackRequestActionType getByName(final String name, final ItemStackRequestActionType fallback) {
        for (ItemStackRequestActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemStackRequestActionType(final ItemStackRequestActionType value) {
        this(value.value);
    }

    ItemStackRequestActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
