// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum UIProfile {

    Classic(0),
    Pocket(1),
    None(2);

    private static final Int2ObjectMap<UIProfile> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (UIProfile value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static UIProfile getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static UIProfile getByValue(final int value, final UIProfile fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    UIProfile(final UIProfile value) {
        this(value.value);
    }

    UIProfile(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
