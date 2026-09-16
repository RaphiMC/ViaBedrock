// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MinecraftEventing_InteractionType {

    breeding(1),
    taming(2),
    curing(3),
    crafted(4),
    shearing(5),
    milking(6),
    trading(7),
    feeding(8),
    igniting(9),
    coloring(10),
    naming(11),
    leashing(12),
    unleashing(13),
    petsleep(14),
    trusting(15),
    commanding(16),
    equipping(17),
    ;

    private static final Int2ObjectMap<MinecraftEventing_InteractionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MinecraftEventing_InteractionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static MinecraftEventing_InteractionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MinecraftEventing_InteractionType getByValue(final int value, final MinecraftEventing_InteractionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static MinecraftEventing_InteractionType getByName(final String name) {
        for (MinecraftEventing_InteractionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static MinecraftEventing_InteractionType getByName(final String name, final MinecraftEventing_InteractionType fallback) {
        for (MinecraftEventing_InteractionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    MinecraftEventing_InteractionType(final MinecraftEventing_InteractionType value) {
        this(value.value);
    }

    MinecraftEventing_InteractionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
