// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum TextPacketType {

    raw(0),
    chat(1),
    translate(2),
    popup(3),
    jukeboxPopup(4),
    tip(5),
    systemMessage(6),
    whisper(7),
    announcement(8),
    textObjectWhisper(9),
    textObject(10),
    textObjectAnnouncement(11),
    ;

    private static final Int2ObjectMap<TextPacketType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (TextPacketType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static TextPacketType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static TextPacketType getByValue(final int value, final TextPacketType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static TextPacketType getByName(final String name) {
        for (TextPacketType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static TextPacketType getByName(final String name, final TextPacketType fallback) {
        for (TextPacketType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    TextPacketType(final TextPacketType value) {
        this(value.value);
    }

    TextPacketType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
