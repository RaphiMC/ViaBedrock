// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_Legacy_Difficulty {

    peaceful(0),
    easy(1),
    normal(2),
    hard(3),
    count(4),
    unknown(5),
    ;

    private static final Int2ObjectMap<SharedTypes_Legacy_Difficulty> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_Legacy_Difficulty value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SharedTypes_Legacy_Difficulty getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_Legacy_Difficulty getByValue(final int value, final SharedTypes_Legacy_Difficulty fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SharedTypes_Legacy_Difficulty getByName(final String name) {
        for (SharedTypes_Legacy_Difficulty value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SharedTypes_Legacy_Difficulty getByName(final String name, final SharedTypes_Legacy_Difficulty fallback) {
        for (SharedTypes_Legacy_Difficulty value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SharedTypes_Legacy_Difficulty(final SharedTypes_Legacy_Difficulty value) {
        this(value.value);
    }

    SharedTypes_Legacy_Difficulty(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
