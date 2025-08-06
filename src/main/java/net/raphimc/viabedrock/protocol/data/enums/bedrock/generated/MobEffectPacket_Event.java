// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MobEffectPacket_Event {

    Invalid(0),
    Add(1),
    Update(2),
    Remove(3);

    private static final Int2ObjectMap<MobEffectPacket_Event> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MobEffectPacket_Event value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static MobEffectPacket_Event getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MobEffectPacket_Event getByValue(final int value, final MobEffectPacket_Event fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    MobEffectPacket_Event(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
