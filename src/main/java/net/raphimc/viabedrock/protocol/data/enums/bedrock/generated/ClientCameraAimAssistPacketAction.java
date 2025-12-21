// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ClientCameraAimAssistPacketAction {

    /**
     * Sets aim-assist to use the settings from a CameraPresets aim_assist field
     */
    SetFromCameraPreset(0),
    /**
     * Clears aim-assist settings
     */
    Clear(1),
    ;

    private static final Int2ObjectMap<ClientCameraAimAssistPacketAction> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ClientCameraAimAssistPacketAction value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ClientCameraAimAssistPacketAction getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ClientCameraAimAssistPacketAction getByValue(final int value, final ClientCameraAimAssistPacketAction fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ClientCameraAimAssistPacketAction getByName(final String name) {
        for (ClientCameraAimAssistPacketAction value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ClientCameraAimAssistPacketAction getByName(final String name, final ClientCameraAimAssistPacketAction fallback) {
        for (ClientCameraAimAssistPacketAction value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ClientCameraAimAssistPacketAction(final ClientCameraAimAssistPacketAction value) {
        this(value.value);
    }

    ClientCameraAimAssistPacketAction(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
