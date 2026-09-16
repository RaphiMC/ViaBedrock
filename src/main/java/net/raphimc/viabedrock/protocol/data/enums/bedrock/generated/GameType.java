// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum GameType {

    undefined(-1),
    survival(0),
    creative(1),
    adventure(2),
    default(5),
    spectator(6),
    worlddefault(0),
    ;

    private static final Int2ObjectMap<GameType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (GameType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static GameType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static GameType getByValue(final int value, final GameType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static GameType getByName(final String name) {
        for (GameType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static GameType getByName(final String name, final GameType fallback) {
        for (GameType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    GameType(final GameType value) {
        this(value.value);
    }

    GameType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
