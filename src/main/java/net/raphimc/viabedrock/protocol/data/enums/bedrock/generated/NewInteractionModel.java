// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum NewInteractionModel {

    Touch(0),
    Crosshair(1),
    Classic(2);

    private static final Int2ObjectMap<NewInteractionModel> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (NewInteractionModel value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static NewInteractionModel getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static NewInteractionModel getByValue(final int value, final NewInteractionModel fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    NewInteractionModel(final NewInteractionModel value) {
        this(value.value);
    }

    NewInteractionModel(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
