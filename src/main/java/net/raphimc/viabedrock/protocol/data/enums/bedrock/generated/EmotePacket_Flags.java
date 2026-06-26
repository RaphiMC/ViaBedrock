// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EmotePacket_Flags {

    SERVER_SIDE(1 << 0),
    MUTE_EMOTE_CHAT(1 << 1),
    ;

    private static final Int2ObjectMap<EmotePacket_Flags> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (EmotePacket_Flags value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static EmotePacket_Flags getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static EmotePacket_Flags getByValue(final int value, final EmotePacket_Flags fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static EmotePacket_Flags getByName(final String name) {
        for (EmotePacket_Flags value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static EmotePacket_Flags getByName(final String name, final EmotePacket_Flags fallback) {
        for (EmotePacket_Flags value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    EmotePacket_Flags(final EmotePacket_Flags value) {
        this(value.value);
    }

    EmotePacket_Flags(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
