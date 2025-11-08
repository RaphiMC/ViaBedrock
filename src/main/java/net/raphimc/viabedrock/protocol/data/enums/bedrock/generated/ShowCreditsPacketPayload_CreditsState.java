// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ShowCreditsPacketPayload_CreditsState {

    Start(0),
    Finished(1);

    private static final Int2ObjectMap<ShowCreditsPacketPayload_CreditsState> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ShowCreditsPacketPayload_CreditsState value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ShowCreditsPacketPayload_CreditsState getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ShowCreditsPacketPayload_CreditsState getByValue(final int value, final ShowCreditsPacketPayload_CreditsState fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ShowCreditsPacketPayload_CreditsState(final ShowCreditsPacketPayload_CreditsState value) {
        this(value.value);
    }

    ShowCreditsPacketPayload_CreditsState(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
