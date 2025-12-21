// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CodeBuilderStorageQueryOptions_Operation {

    None(0),
    Get(1),
    Set(2),
    Reset(3),
    ;

    private static final Int2ObjectMap<CodeBuilderStorageQueryOptions_Operation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CodeBuilderStorageQueryOptions_Operation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CodeBuilderStorageQueryOptions_Operation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CodeBuilderStorageQueryOptions_Operation getByValue(final int value, final CodeBuilderStorageQueryOptions_Operation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CodeBuilderStorageQueryOptions_Operation getByName(final String name) {
        for (CodeBuilderStorageQueryOptions_Operation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CodeBuilderStorageQueryOptions_Operation getByName(final String name, final CodeBuilderStorageQueryOptions_Operation fallback) {
        for (CodeBuilderStorageQueryOptions_Operation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    CodeBuilderStorageQueryOptions_Operation(final CodeBuilderStorageQueryOptions_Operation value) {
        this(value.value);
    }

    CodeBuilderStorageQueryOptions_Operation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
