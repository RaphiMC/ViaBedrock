// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Rotation {

    None(0),
    Rotate90(1),
    Rotate180(2),
    Rotate270(3),
    Clockwise90(Rotate90),
    Clockwise180(Rotate180),
    CounterClockwise90(Rotate270);

    private static final Int2ObjectMap<Rotation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Rotation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static Rotation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Rotation getByValue(final int value, final Rotation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    Rotation(final Rotation value) {
        this(value.value);
    }

    Rotation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
