// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum UpdateType {

    None(0),
    Neighbors(1),
    Connections(2);

    private static final Int2ObjectMap<UpdateType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (UpdateType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static UpdateType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static UpdateType getByValue(final int value, final UpdateType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    UpdateType(final UpdateType value) {
        this(value.value);
    }

    UpdateType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
