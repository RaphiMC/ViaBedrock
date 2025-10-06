// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Mirror {

    None(0),
    X(1),
    Z(2),
    XZ(3);

    private static final Int2ObjectMap<Mirror> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Mirror value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static Mirror getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Mirror getByValue(final int value, final Mirror fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    Mirror(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
