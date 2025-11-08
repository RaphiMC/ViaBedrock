// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PositionTrackingDBServerBroadcastPacketPayload_Action {

    Update(0),
    Destroy(1),
    NotFound(2);

    private static final Int2ObjectMap<PositionTrackingDBServerBroadcastPacketPayload_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PositionTrackingDBServerBroadcastPacketPayload_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PositionTrackingDBServerBroadcastPacketPayload_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PositionTrackingDBServerBroadcastPacketPayload_Action getByValue(final int value, final PositionTrackingDBServerBroadcastPacketPayload_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PositionTrackingDBServerBroadcastPacketPayload_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
