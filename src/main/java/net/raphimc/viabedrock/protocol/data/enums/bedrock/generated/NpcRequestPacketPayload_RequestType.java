// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum NpcRequestPacketPayload_RequestType {

    SetActions(0),
    ExecuteAction(1),
    ExecuteClosingCommands(2),
    SetName(3),
    SetSkin(4),
    SetInteractText(5),
    ExecuteOpeningCommands(6),
    ;

    private static final Int2ObjectMap<NpcRequestPacketPayload_RequestType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (NpcRequestPacketPayload_RequestType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static NpcRequestPacketPayload_RequestType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static NpcRequestPacketPayload_RequestType getByValue(final int value, final NpcRequestPacketPayload_RequestType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static NpcRequestPacketPayload_RequestType getByName(final String name) {
        for (NpcRequestPacketPayload_RequestType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static NpcRequestPacketPayload_RequestType getByName(final String name, final NpcRequestPacketPayload_RequestType fallback) {
        for (NpcRequestPacketPayload_RequestType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    NpcRequestPacketPayload_RequestType(final NpcRequestPacketPayload_RequestType value) {
        this(value.value);
    }

    NpcRequestPacketPayload_RequestType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
