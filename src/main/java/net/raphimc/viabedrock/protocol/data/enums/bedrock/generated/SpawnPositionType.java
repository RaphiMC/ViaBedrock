// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SpawnPositionType {

    PlayerRespawn(0),
    WorldSpawn(1),
    ;

    private static final Int2ObjectMap<SpawnPositionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SpawnPositionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SpawnPositionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SpawnPositionType getByValue(final int value, final SpawnPositionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SpawnPositionType getByName(final String name) {
        for (SpawnPositionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SpawnPositionType getByName(final String name, final SpawnPositionType fallback) {
        for (SpawnPositionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SpawnPositionType(final SpawnPositionType value) {
        this(value.value);
    }

    SpawnPositionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
