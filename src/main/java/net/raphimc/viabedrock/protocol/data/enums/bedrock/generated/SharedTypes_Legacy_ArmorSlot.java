// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_Legacy_ArmorSlot {

    Head(0),
    Torso(1),
    Legs(2),
    Feet(3),
    Body(4);

    private static final Int2ObjectMap<SharedTypes_Legacy_ArmorSlot> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_Legacy_ArmorSlot value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static SharedTypes_Legacy_ArmorSlot getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_Legacy_ArmorSlot getByValue(final int value, final SharedTypes_Legacy_ArmorSlot fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    SharedTypes_Legacy_ArmorSlot(final SharedTypes_Legacy_ArmorSlot value) {
        this(value.value);
    }

    SharedTypes_Legacy_ArmorSlot(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
