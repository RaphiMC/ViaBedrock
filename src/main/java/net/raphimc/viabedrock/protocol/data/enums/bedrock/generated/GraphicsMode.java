// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum GraphicsMode {

    Simple(0),
    Fancy(1),
    Advanced(2),
    RayTraced(3),
    ;

    private static final Int2ObjectMap<GraphicsMode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (GraphicsMode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static GraphicsMode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static GraphicsMode getByValue(final int value, final GraphicsMode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static GraphicsMode getByName(final String name) {
        for (GraphicsMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static GraphicsMode getByName(final String name, final GraphicsMode fallback) {
        for (GraphicsMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    GraphicsMode(final GraphicsMode value) {
        this(value.value);
    }

    GraphicsMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
