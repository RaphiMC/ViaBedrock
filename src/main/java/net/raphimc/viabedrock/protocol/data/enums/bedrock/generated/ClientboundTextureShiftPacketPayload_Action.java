// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ClientboundTextureShiftPacketPayload_Action {

    invalid(0),
    initialize(1),
    start(2),
    setenabled(3),
    sync(4),
    ;

    private static final Int2ObjectMap<ClientboundTextureShiftPacketPayload_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ClientboundTextureShiftPacketPayload_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ClientboundTextureShiftPacketPayload_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ClientboundTextureShiftPacketPayload_Action getByValue(final int value, final ClientboundTextureShiftPacketPayload_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ClientboundTextureShiftPacketPayload_Action getByName(final String name) {
        for (ClientboundTextureShiftPacketPayload_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ClientboundTextureShiftPacketPayload_Action getByName(final String name, final ClientboundTextureShiftPacketPayload_Action fallback) {
        for (ClientboundTextureShiftPacketPayload_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ClientboundTextureShiftPacketPayload_Action(final ClientboundTextureShiftPacketPayload_Action value) {
        this(value.value);
    }

    ClientboundTextureShiftPacketPayload_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
