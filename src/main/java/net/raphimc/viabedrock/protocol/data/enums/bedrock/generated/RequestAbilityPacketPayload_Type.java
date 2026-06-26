// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum RequestAbilityPacketPayload_Type {

    Unset(0),
    Bool(1),
    Float(2),
    ;

    private static final Int2ObjectMap<RequestAbilityPacketPayload_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (RequestAbilityPacketPayload_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static RequestAbilityPacketPayload_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static RequestAbilityPacketPayload_Type getByValue(final int value, final RequestAbilityPacketPayload_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static RequestAbilityPacketPayload_Type getByName(final String name) {
        for (RequestAbilityPacketPayload_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static RequestAbilityPacketPayload_Type getByName(final String name, final RequestAbilityPacketPayload_Type fallback) {
        for (RequestAbilityPacketPayload_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    RequestAbilityPacketPayload_Type(final RequestAbilityPacketPayload_Type value) {
        this(value.value);
    }

    RequestAbilityPacketPayload_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
