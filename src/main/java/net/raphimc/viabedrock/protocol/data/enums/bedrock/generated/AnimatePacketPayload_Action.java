// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AnimatePacketPayload_Action {

    NoAction(0),
    Swing(1),
    WakeUp(3),
    CriticalHit(4),
    MagicCriticalHit(5);

    private static final Int2ObjectMap<AnimatePacketPayload_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AnimatePacketPayload_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static AnimatePacketPayload_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AnimatePacketPayload_Action getByValue(final int value, final AnimatePacketPayload_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    AnimatePacketPayload_Action(final AnimatePacketPayload_Action value) {
        this(value.value);
    }

    AnimatePacketPayload_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
