// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_v1_21_90_CameraPreset_AudioListener {

    Camera(0),
    Player(1),
    ;

    private static final Int2ObjectMap<SharedTypes_v1_21_90_CameraPreset_AudioListener> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_v1_21_90_CameraPreset_AudioListener value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SharedTypes_v1_21_90_CameraPreset_AudioListener getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_v1_21_90_CameraPreset_AudioListener getByValue(final int value, final SharedTypes_v1_21_90_CameraPreset_AudioListener fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SharedTypes_v1_21_90_CameraPreset_AudioListener getByName(final String name) {
        for (SharedTypes_v1_21_90_CameraPreset_AudioListener value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SharedTypes_v1_21_90_CameraPreset_AudioListener getByName(final String name, final SharedTypes_v1_21_90_CameraPreset_AudioListener fallback) {
        for (SharedTypes_v1_21_90_CameraPreset_AudioListener value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SharedTypes_v1_21_90_CameraPreset_AudioListener(final SharedTypes_v1_21_90_CameraPreset_AudioListener value) {
        this(value.value);
    }

    SharedTypes_v1_21_90_CameraPreset_AudioListener(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
