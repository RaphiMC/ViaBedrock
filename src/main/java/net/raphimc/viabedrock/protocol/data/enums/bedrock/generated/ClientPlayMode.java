// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ClientPlayMode {

    normal(0),
    teaser(1),
    screen(2),
    viewer(3),
    reality(4),
    placement(5),
    livingroom(6),
    exitlevel(7),
    exitlevellivingroom(8),
    nummodes(9),
    ;

    private static final Int2ObjectMap<ClientPlayMode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ClientPlayMode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ClientPlayMode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ClientPlayMode getByValue(final int value, final ClientPlayMode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ClientPlayMode getByName(final String name) {
        for (ClientPlayMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ClientPlayMode getByName(final String name, final ClientPlayMode fallback) {
        for (ClientPlayMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ClientPlayMode(final ClientPlayMode value) {
        this(value.value);
    }

    ClientPlayMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
