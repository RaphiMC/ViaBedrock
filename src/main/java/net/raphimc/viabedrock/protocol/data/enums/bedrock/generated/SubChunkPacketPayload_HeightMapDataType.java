// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SubChunkPacketPayload_HeightMapDataType {

    NoData(0),
    HasData(1),
    AllTooHigh(2),
    AllTooLow(3),
    AllCopied(4),
    ;

    private static final Int2ObjectMap<SubChunkPacketPayload_HeightMapDataType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SubChunkPacketPayload_HeightMapDataType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SubChunkPacketPayload_HeightMapDataType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SubChunkPacketPayload_HeightMapDataType getByValue(final int value, final SubChunkPacketPayload_HeightMapDataType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SubChunkPacketPayload_HeightMapDataType getByName(final String name) {
        for (SubChunkPacketPayload_HeightMapDataType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SubChunkPacketPayload_HeightMapDataType getByName(final String name, final SubChunkPacketPayload_HeightMapDataType fallback) {
        for (SubChunkPacketPayload_HeightMapDataType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SubChunkPacketPayload_HeightMapDataType(final SubChunkPacketPayload_HeightMapDataType value) {
        this(value.value);
    }

    SubChunkPacketPayload_HeightMapDataType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
