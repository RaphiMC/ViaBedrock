// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum StructureBlockType {

    Data(0),
    Save(1),
    Load(2),
    Corner(3),
    Invalid(4),
    Export(5);

    private static final Int2ObjectMap<StructureBlockType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (StructureBlockType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static StructureBlockType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static StructureBlockType getByValue(final int value, final StructureBlockType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    StructureBlockType(final StructureBlockType value) {
        this(value.value);
    }

    StructureBlockType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
