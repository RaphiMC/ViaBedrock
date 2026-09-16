// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum VillageType {

    Desert(0),
    Ice(1),
    Savanna(2),
    Taiga(3),
    Default(4),
    ;

    private static final Int2ObjectMap<VillageType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (VillageType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static VillageType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static VillageType getByValue(final int value, final VillageType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static VillageType getByName(final String name) {
        for (VillageType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static VillageType getByName(final String name, final VillageType fallback) {
        for (VillageType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    VillageType(final VillageType value) {
        this(value.value);
    }

    VillageType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
