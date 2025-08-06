// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum GameType {

    Undefined(-1),
    Survival(0),
    Creative(1),
    Adventure(2),
    Default(5),
    Spectator(6),
    WorldDefault(Survival.getValue());

    private static final Int2ObjectMap<GameType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (GameType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static GameType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static GameType getByValue(final int value, final GameType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    GameType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
