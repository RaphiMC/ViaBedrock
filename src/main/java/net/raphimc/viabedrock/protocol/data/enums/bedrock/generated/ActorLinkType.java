// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ActorLinkType {

    None(0),
    Riding(1),
    Passenger(2),
    ;

    private static final Int2ObjectMap<ActorLinkType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ActorLinkType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ActorLinkType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ActorLinkType getByValue(final int value, final ActorLinkType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ActorLinkType getByName(final String name) {
        for (ActorLinkType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ActorLinkType getByName(final String name, final ActorLinkType fallback) {
        for (ActorLinkType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ActorLinkType(final ActorLinkType value) {
        this(value.value);
    }

    ActorLinkType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
