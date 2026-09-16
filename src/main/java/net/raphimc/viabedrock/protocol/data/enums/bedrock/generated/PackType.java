// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PackType {

    invalid(0),
    addon(1),
    cached(2),
    copyprotected(3),
    behavior(4),
    personapiece(5),
    resources(6),
    skins(7),
    worldtemplate(8),
    ;

    private static final Int2ObjectMap<PackType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PackType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PackType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PackType getByValue(final int value, final PackType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PackType getByName(final String name) {
        for (PackType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PackType getByName(final String name, final PackType fallback) {
        for (PackType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
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
