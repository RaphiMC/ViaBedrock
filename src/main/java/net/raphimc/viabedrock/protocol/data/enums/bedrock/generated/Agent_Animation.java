// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Agent_Animation {

    ArmSwing(0),
    Shrug(1),
    ;

    private static final Int2ObjectMap<Agent_Animation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Agent_Animation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Agent_Animation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Agent_Animation getByValue(final int value, final Agent_Animation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Agent_Animation getByName(final String name) {
        for (Agent_Animation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Agent_Animation getByName(final String name, final Agent_Animation fallback) {
        for (Agent_Animation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Agent_Animation(final Agent_Animation value) {
        this(value.value);
    }

    Agent_Animation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
