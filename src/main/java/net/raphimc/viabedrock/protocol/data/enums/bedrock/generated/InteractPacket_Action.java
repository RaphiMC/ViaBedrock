// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum InteractPacket_Action {

    Invalid(0),
    StopRiding(3),
    InteractUpdate(4),
    NpcOpen(5),
    OpenInventory(6);

    private static final Int2ObjectMap<InteractPacket_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InteractPacket_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static InteractPacket_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InteractPacket_Action getByValue(final int value, final InteractPacket_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    InteractPacket_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
