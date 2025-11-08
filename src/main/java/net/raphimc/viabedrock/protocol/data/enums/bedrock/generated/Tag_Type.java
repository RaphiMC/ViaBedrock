// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Tag_Type {

    End(0),
    Byte(1),
    Short(2),
    Int(3),
    Int64(4),
    Float(5),
    Double(6),
    ByteArray(7),
    String(8),
    List(9),
    Compound(10),
    IntArray(11);

    private static final Int2ObjectMap<Tag_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Tag_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static Tag_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Tag_Type getByValue(final int value, final Tag_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    Tag_Type(final Tag_Type value) {
        this(value.value);
    }

    Tag_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
