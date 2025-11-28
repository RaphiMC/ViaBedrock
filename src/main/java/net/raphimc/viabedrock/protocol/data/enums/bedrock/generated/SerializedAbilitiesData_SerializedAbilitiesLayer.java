// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SerializedAbilitiesData_SerializedAbilitiesLayer {

    CustomCache(0),
    Base(1),
    Spectator(2),
    Commands(3),
    Editor(4),
    LoadingScreen(5);

    private static final Int2ObjectMap<SerializedAbilitiesData_SerializedAbilitiesLayer> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SerializedAbilitiesData_SerializedAbilitiesLayer value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static SerializedAbilitiesData_SerializedAbilitiesLayer getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SerializedAbilitiesData_SerializedAbilitiesLayer getByValue(final int value, final SerializedAbilitiesData_SerializedAbilitiesLayer fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    SerializedAbilitiesData_SerializedAbilitiesLayer(final SerializedAbilitiesData_SerializedAbilitiesLayer value) {
        this(value.value);
    }

    SerializedAbilitiesData_SerializedAbilitiesLayer(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
