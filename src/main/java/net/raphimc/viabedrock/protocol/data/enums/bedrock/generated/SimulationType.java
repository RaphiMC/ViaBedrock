// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SimulationType {

    Game(0),
    Editor(1),
    Test(2),
    INVALID(3);

    private static final Int2ObjectMap<SimulationType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SimulationType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static SimulationType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SimulationType getByValue(final int value, final SimulationType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    SimulationType(final SimulationType value) {
        this(value.value);
    }

    SimulationType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
