// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SimpleEventPacketPayload_Subtype {

    UninitializedSubtype(0),
    EnableCommands(1),
    DisableCommands(2),
    UnlockWorldTemplateSettings(3);

    private static final Int2ObjectMap<SimpleEventPacketPayload_Subtype> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SimpleEventPacketPayload_Subtype value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static SimpleEventPacketPayload_Subtype getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SimpleEventPacketPayload_Subtype getByValue(final int value, final SimpleEventPacketPayload_Subtype fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    SimpleEventPacketPayload_Subtype(final SimpleEventPacketPayload_Subtype value) {
        this(value.value);
    }

    SimpleEventPacketPayload_Subtype(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
