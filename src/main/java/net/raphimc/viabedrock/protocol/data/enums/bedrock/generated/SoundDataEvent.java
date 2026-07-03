// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SoundDataEvent {

    Stop(0),
    ;

    private static final Int2ObjectMap<SoundDataEvent> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SoundDataEvent value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SoundDataEvent getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SoundDataEvent getByValue(final int value, final SoundDataEvent fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SoundDataEvent getByName(final String name) {
        for (SoundDataEvent value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SoundDataEvent getByName(final String name, final SoundDataEvent fallback) {
        for (SoundDataEvent value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SoundDataEvent(final SoundDataEvent value) {
        this(value.value);
    }

    SoundDataEvent(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
