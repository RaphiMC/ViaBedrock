// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum GeneratorType {

    Legacy(0),
    Overworld(1),
    Flat(2),
    Nether(3),
    TheEnd(4),
    Void(5),
    Undefined(6),
    ;

    private static final Int2ObjectMap<GeneratorType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (GeneratorType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static GeneratorType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static GeneratorType getByValue(final int value, final GeneratorType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static GeneratorType getByName(final String name) {
        for (GeneratorType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static GeneratorType getByName(final String name, final GeneratorType fallback) {
        for (GeneratorType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    GeneratorType(final GeneratorType value) {
        this(value.value);
    }

    GeneratorType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
