// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CameraAimAssistPresetPacketOperation {

    AAA(1);

    private static final Int2ObjectMap<CameraAimAssistPresetPacketOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CameraAimAssistPresetPacketOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CameraAimAssistPresetPacketOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CameraAimAssistPresetPacketOperation getByValue(final int value, final CameraAimAssistPresetPacketOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CameraAimAssistPresetPacketOperation(final CameraAimAssistPresetPacketOperation value) {
        this(value.value);
    }

    CameraAimAssistPresetPacketOperation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
