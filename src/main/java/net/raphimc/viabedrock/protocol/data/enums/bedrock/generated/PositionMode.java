// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PositionMode {

    Normal(0),
    Respawn(1),
    Teleport(2),
    OnlyHeadRot(3),
    ;

    private static final Int2ObjectMap<PositionMode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PositionMode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PositionMode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PositionMode getByValue(final int value, final PositionMode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PositionMode getByName(final String name) {
        for (PositionMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PositionMode getByName(final String name, final PositionMode fallback) {
        for (PositionMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PositionMode(final PositionMode value) {
        this(value.value);
    }

    PositionMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
