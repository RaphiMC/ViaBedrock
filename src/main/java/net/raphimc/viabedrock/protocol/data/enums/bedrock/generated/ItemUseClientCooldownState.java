// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseClientCooldownState {

    Off(0),
    On(1),
    ;

    private static final Int2ObjectMap<ItemUseClientCooldownState> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseClientCooldownState value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemUseClientCooldownState getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseClientCooldownState getByValue(final int value, final ItemUseClientCooldownState fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemUseClientCooldownState getByName(final String name) {
        for (ItemUseClientCooldownState value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemUseClientCooldownState getByName(final String name, final ItemUseClientCooldownState fallback) {
        for (ItemUseClientCooldownState value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemUseClientCooldownState(final ItemUseClientCooldownState value) {
        this(value.value);
    }

    ItemUseClientCooldownState(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
