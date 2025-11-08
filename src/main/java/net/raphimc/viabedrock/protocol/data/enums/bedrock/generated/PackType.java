// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PackType {

    Invalid(0),
    Addon(1),
    Cached(2),
    CopyProtected(3),
    Behavior(4),
    PersonaPiece(5),
    Resources(6),
    Skins(7),
    WorldTemplate(8);

    private static final Int2ObjectMap<PackType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PackType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PackType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PackType getByValue(final int value, final PackType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PackType(final PackType value) {
        this(value.value);
    }

    PackType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
