// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum TextPacketType {

    Raw(0),
    Chat(1),
    Translate(2),
    Popup(3),
    JukeboxPopup(4),
    Tip(5),
    SystemMessage(6),
    Whisper(7),
    Announcement(8),
    TextObjectWhisper(9),
    TextObject(10),
    TextObjectAnnouncement(11);

    private static final Int2ObjectMap<TextPacketType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (TextPacketType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static TextPacketType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static TextPacketType getByValue(final int value, final TextPacketType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
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
