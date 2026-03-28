// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum TrustedSkinFlag {

    Unset(0),
    False(1),
    True(2),
    ;

    private static final Int2ObjectMap<TrustedSkinFlag> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (TrustedSkinFlag value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static TrustedSkinFlag getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static TrustedSkinFlag getByValue(final int value, final TrustedSkinFlag fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static TrustedSkinFlag getByName(final String name) {
        for (TrustedSkinFlag value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static TrustedSkinFlag getByName(final String name, final TrustedSkinFlag fallback) {
        for (TrustedSkinFlag value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    TrustedSkinFlag(final TrustedSkinFlag value) {
        this(value.value);
    }

    TrustedSkinFlag(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
