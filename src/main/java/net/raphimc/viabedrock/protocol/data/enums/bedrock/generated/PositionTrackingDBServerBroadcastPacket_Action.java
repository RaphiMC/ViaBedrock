// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PositionTrackingDBServerBroadcastPacket_Action {

    Update(0),
    Destroy(1),
    NotFound(2);

    private static final Int2ObjectMap<PositionTrackingDBServerBroadcastPacket_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PositionTrackingDBServerBroadcastPacket_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PositionTrackingDBServerBroadcastPacket_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PositionTrackingDBServerBroadcastPacket_Action getByValue(final int value, final PositionTrackingDBServerBroadcastPacket_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PositionTrackingDBServerBroadcastPacket_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
