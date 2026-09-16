// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum InteractPacketPayload_Action {

    Invalid(0),
    StopRiding(3),
    InteractUpdate(4),
    NpcOpen(5),
    OpenInventory(6),
    ;

    private static final Int2ObjectMap<InteractPacketPayload_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InteractPacketPayload_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static InteractPacketPayload_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InteractPacketPayload_Action getByValue(final int value, final InteractPacketPayload_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static InteractPacketPayload_Action getByName(final String name) {
        for (InteractPacketPayload_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static InteractPacketPayload_Action getByName(final String name, final InteractPacketPayload_Action fallback) {
        for (InteractPacketPayload_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    InteractPacketPayload_Action(final InteractPacketPayload_Action value) {
        this(value.value);
    }

    InteractPacketPayload_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
