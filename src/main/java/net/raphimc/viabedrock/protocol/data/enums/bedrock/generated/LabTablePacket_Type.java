// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum LabTablePacket_Type {

    StartCombine(0),
    StartReaction(1),
    Reset(2);

    private static final Int2ObjectMap<LabTablePacket_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (LabTablePacket_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static LabTablePacket_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static LabTablePacket_Type getByValue(final int value, final LabTablePacket_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    LabTablePacket_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
