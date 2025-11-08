// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SpawnBiomeType {

    Default(0),
    UserDefined(1);

    private static final Int2ObjectMap<SpawnBiomeType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SpawnBiomeType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static SpawnBiomeType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SpawnBiomeType getByValue(final int value, final SpawnBiomeType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    SpawnBiomeType(final SpawnBiomeType value) {
        this(value.value);
    }

    SpawnBiomeType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
