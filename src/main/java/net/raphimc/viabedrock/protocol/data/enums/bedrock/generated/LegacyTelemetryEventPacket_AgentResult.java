// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum LegacyTelemetryEventPacket_AgentResult {

    ActionFail(0),
    ActionSuccess(1),
    QueryResultFalse(2),
    QueryResultTrue(3);

    private static final Int2ObjectMap<LegacyTelemetryEventPacket_AgentResult> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (LegacyTelemetryEventPacket_AgentResult value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static LegacyTelemetryEventPacket_AgentResult getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static LegacyTelemetryEventPacket_AgentResult getByValue(final int value, final LegacyTelemetryEventPacket_AgentResult fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    LegacyTelemetryEventPacket_AgentResult(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
