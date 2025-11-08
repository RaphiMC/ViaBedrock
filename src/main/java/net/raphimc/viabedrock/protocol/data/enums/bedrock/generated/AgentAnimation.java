// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AgentAnimation {

    ArmSwing(0),
    Shrug(1);

    private static final Int2ObjectMap<AgentAnimation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AgentAnimation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static AgentAnimation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AgentAnimation getByValue(final int value, final AgentAnimation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    AgentAnimation(final AgentAnimation value) {
        this(value.value);
    }

    AgentAnimation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
