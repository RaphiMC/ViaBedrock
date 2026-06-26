// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PositionTrackingDBClientRequestPacket_Action {

    Query(0),
    ;

    private static final Int2ObjectMap<PositionTrackingDBClientRequestPacket_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PositionTrackingDBClientRequestPacket_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PositionTrackingDBClientRequestPacket_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PositionTrackingDBClientRequestPacket_Action getByValue(final int value, final PositionTrackingDBClientRequestPacket_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PositionTrackingDBClientRequestPacket_Action getByName(final String name) {
        for (PositionTrackingDBClientRequestPacket_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PositionTrackingDBClientRequestPacket_Action getByName(final String name, final PositionTrackingDBClientRequestPacket_Action fallback) {
        for (PositionTrackingDBClientRequestPacket_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PositionTrackingDBClientRequestPacket_Action(final PositionTrackingDBClientRequestPacket_Action value) {
        this(value.value);
    }

    PositionTrackingDBClientRequestPacket_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
