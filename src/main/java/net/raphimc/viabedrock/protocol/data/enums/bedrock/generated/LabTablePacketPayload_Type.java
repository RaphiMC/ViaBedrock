// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum LabTablePacketPayload_Type {

    StartCombine(0),
    StartReaction(1),
    Reset(2),
    ;

    private static final Int2ObjectMap<LabTablePacketPayload_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (LabTablePacketPayload_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static LabTablePacketPayload_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static LabTablePacketPayload_Type getByValue(final int value, final LabTablePacketPayload_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static LabTablePacketPayload_Type getByName(final String name) {
        for (LabTablePacketPayload_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static LabTablePacketPayload_Type getByName(final String name, final LabTablePacketPayload_Type fallback) {
        for (LabTablePacketPayload_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    LabTablePacketPayload_Type(final LabTablePacketPayload_Type value) {
        this(value.value);
    }

    LabTablePacketPayload_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
