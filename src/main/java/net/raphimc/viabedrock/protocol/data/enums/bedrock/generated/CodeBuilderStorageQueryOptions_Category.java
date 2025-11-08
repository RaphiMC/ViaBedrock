// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CodeBuilderStorageQueryOptions_Category {

    None(0),
    CodeStatus(1),
    Instantiation(2);

    private static final Int2ObjectMap<CodeBuilderStorageQueryOptions_Category> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CodeBuilderStorageQueryOptions_Category value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CodeBuilderStorageQueryOptions_Category getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CodeBuilderStorageQueryOptions_Category getByValue(final int value, final CodeBuilderStorageQueryOptions_Category fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CodeBuilderStorageQueryOptions_Category(final CodeBuilderStorageQueryOptions_Category value) {
        this(value.value);
    }

    CodeBuilderStorageQueryOptions_Category(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
