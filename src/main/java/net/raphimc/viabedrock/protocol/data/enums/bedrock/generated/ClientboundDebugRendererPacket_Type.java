// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ClientboundDebugRendererPacket_Type {

    Invalid(0),
    ClearDebugMarkers(1),
    AddDebugMarkerCube(2),
    ;

    private static final Int2ObjectMap<ClientboundDebugRendererPacket_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ClientboundDebugRendererPacket_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ClientboundDebugRendererPacket_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ClientboundDebugRendererPacket_Type getByValue(final int value, final ClientboundDebugRendererPacket_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ClientboundDebugRendererPacket_Type getByName(final String name) {
        for (ClientboundDebugRendererPacket_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ClientboundDebugRendererPacket_Type getByName(final String name, final ClientboundDebugRendererPacket_Type fallback) {
        for (ClientboundDebugRendererPacket_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ClientboundDebugRendererPacket_Type(final ClientboundDebugRendererPacket_Type value) {
        this(value.value);
    }

    ClientboundDebugRendererPacket_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
