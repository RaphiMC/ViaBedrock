// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ActorDataBoundingBoxComponent_Type {

    Scale(0),
    Width(1),
    Height(2);

    private static final Int2ObjectMap<ActorDataBoundingBoxComponent_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ActorDataBoundingBoxComponent_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ActorDataBoundingBoxComponent_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ActorDataBoundingBoxComponent_Type getByValue(final int value, final ActorDataBoundingBoxComponent_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ActorDataBoundingBoxComponent_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
