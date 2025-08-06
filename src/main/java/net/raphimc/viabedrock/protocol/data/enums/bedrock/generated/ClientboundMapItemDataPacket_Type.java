// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ClientboundMapItemDataPacket_Type {

    Invalid(0),
    TextureUpdate(1 << 1),
    DecorationUpdate(1 << 2),
    Creation(1 << 3);

    private static final Int2ObjectMap<ClientboundMapItemDataPacket_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ClientboundMapItemDataPacket_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ClientboundMapItemDataPacket_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ClientboundMapItemDataPacket_Type getByValue(final int value, final ClientboundMapItemDataPacket_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ClientboundMapItemDataPacket_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
