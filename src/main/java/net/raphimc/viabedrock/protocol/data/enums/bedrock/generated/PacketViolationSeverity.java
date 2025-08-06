// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PacketViolationSeverity {

    Unknown(-1),
    Warning(0),
    FinalWarning(1),
    TerminatingConnection(2);

    private static final Int2ObjectMap<PacketViolationSeverity> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PacketViolationSeverity value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PacketViolationSeverity getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PacketViolationSeverity getByValue(final int value, final PacketViolationSeverity fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PacketViolationSeverity(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
