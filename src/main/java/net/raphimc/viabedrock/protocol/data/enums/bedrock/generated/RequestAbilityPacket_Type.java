// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum RequestAbilityPacket_Type {

    Unset(0),
    Bool(1),
    Float(2);

    private static final Int2ObjectMap<RequestAbilityPacket_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (RequestAbilityPacket_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static RequestAbilityPacket_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static RequestAbilityPacket_Type getByValue(final int value, final RequestAbilityPacket_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    RequestAbilityPacket_Type(final RequestAbilityPacket_Type value) {
        this(value.value);
    }

    RequestAbilityPacket_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
