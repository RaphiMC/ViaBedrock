// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ClientboundDebugRendererPacketPayload_PayloadType {

    Invalid(0),
    ClearDebugMarkers(1),
    AddDebugMarkerCube(2);

    private static final Int2ObjectMap<ClientboundDebugRendererPacketPayload_PayloadType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ClientboundDebugRendererPacketPayload_PayloadType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ClientboundDebugRendererPacketPayload_PayloadType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ClientboundDebugRendererPacketPayload_PayloadType getByValue(final int value, final ClientboundDebugRendererPacketPayload_PayloadType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ClientboundDebugRendererPacketPayload_PayloadType(final ClientboundDebugRendererPacketPayload_PayloadType value) {
        this(value.value);
    }

    ClientboundDebugRendererPacketPayload_PayloadType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
