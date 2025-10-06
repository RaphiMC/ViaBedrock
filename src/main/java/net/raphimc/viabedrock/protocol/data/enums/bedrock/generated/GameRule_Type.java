// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum GameRule_Type {

    Invalid(0),
    Bool(1),
    Int(2),
    Float(3);

    private static final Int2ObjectMap<GameRule_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (GameRule_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static GameRule_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static GameRule_Type getByValue(final int value, final GameRule_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    GameRule_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
