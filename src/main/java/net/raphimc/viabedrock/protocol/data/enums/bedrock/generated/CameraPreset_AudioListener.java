// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CameraPreset_AudioListener {

    Camera(0),
    Player(1),
    ;

    private static final Int2ObjectMap<CameraPreset_AudioListener> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CameraPreset_AudioListener value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CameraPreset_AudioListener getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CameraPreset_AudioListener getByValue(final int value, final CameraPreset_AudioListener fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CameraPreset_AudioListener getByName(final String name) {
        for (CameraPreset_AudioListener value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CameraPreset_AudioListener getByName(final String name, final CameraPreset_AudioListener fallback) {
        for (CameraPreset_AudioListener value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    CameraPreset_AudioListener(final CameraPreset_AudioListener value) {
        this(value.value);
    }

    CameraPreset_AudioListener(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
