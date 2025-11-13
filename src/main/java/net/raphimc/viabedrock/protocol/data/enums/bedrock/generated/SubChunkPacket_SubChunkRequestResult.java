// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SubChunkPacket_SubChunkRequestResult {

    Undefined(0),
    Success(1),
    LevelChunkDoesntExist(2),
    WrongDimension(3),
    PlayerDoesntExist(4),
    IndexOutOfBounds(5),
    SuccessAllAir(6);

    private static final Int2ObjectMap<SubChunkPacket_SubChunkRequestResult> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SubChunkPacket_SubChunkRequestResult value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static SubChunkPacket_SubChunkRequestResult getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SubChunkPacket_SubChunkRequestResult getByValue(final int value, final SubChunkPacket_SubChunkRequestResult fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    SubChunkPacket_SubChunkRequestResult(final SubChunkPacket_SubChunkRequestResult value) {
        this(value.value);
    }

    SubChunkPacket_SubChunkRequestResult(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
