// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CameraShakeAction {

    Add(0),
    Stop(1);

    private static final Int2ObjectMap<CameraShakeAction> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CameraShakeAction value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CameraShakeAction getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CameraShakeAction getByValue(final int value, final CameraShakeAction fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CameraShakeAction(final CameraShakeAction value) {
        this(value.value);
    }

    CameraShakeAction(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
