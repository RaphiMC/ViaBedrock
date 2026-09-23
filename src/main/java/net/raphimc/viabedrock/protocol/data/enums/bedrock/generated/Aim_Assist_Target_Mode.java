// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Aim_Assist_Target_Mode {

    angle(0),
    distance(1),
    ;

    private static final Int2ObjectMap<Aim_Assist_Target_Mode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Aim_Assist_Target_Mode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Aim_Assist_Target_Mode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Aim_Assist_Target_Mode getByValue(final int value, final Aim_Assist_Target_Mode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Aim_Assist_Target_Mode getByName(final String name) {
        for (Aim_Assist_Target_Mode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Aim_Assist_Target_Mode getByName(final String name, final Aim_Assist_Target_Mode fallback) {
        for (Aim_Assist_Target_Mode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Aim_Assist_Target_Mode(final Aim_Assist_Target_Mode value) {
        this(value.value);
    }

    Aim_Assist_Target_Mode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
