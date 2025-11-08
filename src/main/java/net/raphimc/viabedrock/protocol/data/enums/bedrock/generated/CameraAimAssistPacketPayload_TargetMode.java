// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CameraAimAssistPacketPayload_TargetMode {

    Angle(0),
    Distance(1);

    private static final Int2ObjectMap<CameraAimAssistPacketPayload_TargetMode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CameraAimAssistPacketPayload_TargetMode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CameraAimAssistPacketPayload_TargetMode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CameraAimAssistPacketPayload_TargetMode getByValue(final int value, final CameraAimAssistPacketPayload_TargetMode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CameraAimAssistPacketPayload_TargetMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
