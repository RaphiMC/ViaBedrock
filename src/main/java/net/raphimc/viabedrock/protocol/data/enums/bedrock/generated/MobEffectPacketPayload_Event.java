// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MobEffectPacketPayload_Event {

    Invalid(0),
    Add(1),
    Update(2),
    Remove(3),
    ;

    private static final Int2ObjectMap<MobEffectPacketPayload_Event> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MobEffectPacketPayload_Event value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static MobEffectPacketPayload_Event getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MobEffectPacketPayload_Event getByValue(final int value, final MobEffectPacketPayload_Event fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static MobEffectPacketPayload_Event getByName(final String name) {
        for (MobEffectPacketPayload_Event value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static MobEffectPacketPayload_Event getByName(final String name, final MobEffectPacketPayload_Event fallback) {
        for (MobEffectPacketPayload_Event value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    MobEffectPacketPayload_Event(final MobEffectPacketPayload_Event value) {
        this(value.value);
    }

    MobEffectPacketPayload_Event(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
