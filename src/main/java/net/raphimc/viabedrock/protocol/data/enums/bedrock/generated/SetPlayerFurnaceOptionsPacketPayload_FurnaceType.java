// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SetPlayerFurnaceOptionsPacketPayload_FurnaceType {

    None(0),
    Furnace(1),
    BlastFurnace(2),
    Smoker(3),
    ;

    private static final Int2ObjectMap<SetPlayerFurnaceOptionsPacketPayload_FurnaceType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SetPlayerFurnaceOptionsPacketPayload_FurnaceType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SetPlayerFurnaceOptionsPacketPayload_FurnaceType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SetPlayerFurnaceOptionsPacketPayload_FurnaceType getByValue(final int value, final SetPlayerFurnaceOptionsPacketPayload_FurnaceType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SetPlayerFurnaceOptionsPacketPayload_FurnaceType getByName(final String name) {
        for (SetPlayerFurnaceOptionsPacketPayload_FurnaceType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SetPlayerFurnaceOptionsPacketPayload_FurnaceType getByName(final String name, final SetPlayerFurnaceOptionsPacketPayload_FurnaceType fallback) {
        for (SetPlayerFurnaceOptionsPacketPayload_FurnaceType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SetPlayerFurnaceOptionsPacketPayload_FurnaceType(final SetPlayerFurnaceOptionsPacketPayload_FurnaceType value) {
        this(value.value);
    }

    SetPlayerFurnaceOptionsPacketPayload_FurnaceType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
