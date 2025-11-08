// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CameraAimAssistPacketPayload_Action {

    Set(0),
    Clear(1);

    private static final Int2ObjectMap<CameraAimAssistPacketPayload_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CameraAimAssistPacketPayload_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CameraAimAssistPacketPayload_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CameraAimAssistPacketPayload_Action getByValue(final int value, final CameraAimAssistPacketPayload_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CameraAimAssistPacketPayload_Action(final CameraAimAssistPacketPayload_Action value) {
        this(value.value);
    }

    CameraAimAssistPacketPayload_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
