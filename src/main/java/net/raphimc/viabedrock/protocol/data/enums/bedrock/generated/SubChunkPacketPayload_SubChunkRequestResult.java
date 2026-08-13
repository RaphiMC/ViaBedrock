// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SubChunkPacketPayload_SubChunkRequestResult {

    Undefined(0),
    Success(1),
    LevelChunkDoesntExist(2),
    WrongDimension(3),
    PlayerDoesntExist(4),
    IndexOutOfBounds(5),
    SuccessAllAir(6),
    ;

    private static final Int2ObjectMap<SubChunkPacketPayload_SubChunkRequestResult> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SubChunkPacketPayload_SubChunkRequestResult value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SubChunkPacketPayload_SubChunkRequestResult getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SubChunkPacketPayload_SubChunkRequestResult getByValue(final int value, final SubChunkPacketPayload_SubChunkRequestResult fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SubChunkPacketPayload_SubChunkRequestResult getByName(final String name) {
        for (SubChunkPacketPayload_SubChunkRequestResult value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SubChunkPacketPayload_SubChunkRequestResult getByName(final String name, final SubChunkPacketPayload_SubChunkRequestResult fallback) {
        for (SubChunkPacketPayload_SubChunkRequestResult value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SubChunkPacketPayload_SubChunkRequestResult(final SubChunkPacketPayload_SubChunkRequestResult value) {
        this(value.value);
    }

    SubChunkPacketPayload_SubChunkRequestResult(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
