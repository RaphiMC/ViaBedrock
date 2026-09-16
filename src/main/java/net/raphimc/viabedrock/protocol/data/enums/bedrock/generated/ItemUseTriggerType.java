// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseTriggerType {

    Unknown(0),
    Player_Input(1),
    Simulation_Tick(2),
    ;

    private static final Int2ObjectMap<ItemUseTriggerType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseTriggerType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemUseTriggerType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseTriggerType getByValue(final int value, final ItemUseTriggerType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemUseTriggerType getByName(final String name) {
        for (ItemUseTriggerType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemUseTriggerType getByName(final String name, final ItemUseTriggerType fallback) {
        for (ItemUseTriggerType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemUseTriggerType(final ItemUseTriggerType value) {
        this(value.value);
    }

    ItemUseTriggerType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
