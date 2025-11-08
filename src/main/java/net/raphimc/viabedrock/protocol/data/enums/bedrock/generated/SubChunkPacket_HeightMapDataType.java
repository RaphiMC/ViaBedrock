// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SubChunkPacket_HeightMapDataType {

    NoData(0),
    HasData(1),
    AllTooHigh(2),
    AllTooLow(3),
    AllCopied(4);

    private static final Int2ObjectMap<SubChunkPacket_HeightMapDataType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SubChunkPacket_HeightMapDataType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static SubChunkPacket_HeightMapDataType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SubChunkPacket_HeightMapDataType getByValue(final int value, final SubChunkPacket_HeightMapDataType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    SubChunkPacket_HeightMapDataType(final SubChunkPacket_HeightMapDataType value) {
        this(value.value);
    }

    SubChunkPacket_HeightMapDataType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
