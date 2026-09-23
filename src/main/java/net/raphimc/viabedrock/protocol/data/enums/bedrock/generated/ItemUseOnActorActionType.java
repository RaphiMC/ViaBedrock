// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseOnActorActionType {

    Interact(0),
    Attack(1),
    Item_Interact(2),
    ;

    private static final Int2ObjectMap<ItemUseOnActorActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseOnActorActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemUseOnActorActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseOnActorActionType getByValue(final int value, final ItemUseOnActorActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemUseOnActorActionType getByName(final String name) {
        for (ItemUseOnActorActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemUseOnActorActionType getByName(final String name, final ItemUseOnActorActionType fallback) {
        for (ItemUseOnActorActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemUseOnActorActionType(final ItemUseOnActorActionType value) {
        this(value.value);
    }

    ItemUseOnActorActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
