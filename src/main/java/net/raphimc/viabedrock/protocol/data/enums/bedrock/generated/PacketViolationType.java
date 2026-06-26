// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PacketViolationType {

    Unknown(-1),
    PacketMalformed(0),
    ;

    private static final Int2ObjectMap<PacketViolationType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PacketViolationType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PacketViolationType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PacketViolationType getByValue(final int value, final PacketViolationType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PacketViolationType getByName(final String name) {
        for (PacketViolationType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PacketViolationType getByName(final String name, final PacketViolationType fallback) {
        for (PacketViolationType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PacketViolationType(final PacketViolationType value) {
        this(value.value);
    }

    PacketViolationType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
