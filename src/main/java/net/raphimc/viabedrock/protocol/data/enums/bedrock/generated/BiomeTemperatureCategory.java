// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum BiomeTemperatureCategory {

    Medium(0),
    Warm(1),
    Lukewarm(2),
    Cold(3),
    Frozen(4),
    ;

    private static final Int2ObjectMap<BiomeTemperatureCategory> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (BiomeTemperatureCategory value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static BiomeTemperatureCategory getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static BiomeTemperatureCategory getByValue(final int value, final BiomeTemperatureCategory fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static BiomeTemperatureCategory getByName(final String name) {
        for (BiomeTemperatureCategory value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static BiomeTemperatureCategory getByName(final String name, final BiomeTemperatureCategory fallback) {
        for (BiomeTemperatureCategory value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    BiomeTemperatureCategory(final BiomeTemperatureCategory value) {
        this(value.value);
    }

    BiomeTemperatureCategory(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
