// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum TextPacketType {

    /**
     * Raw unformatted text message
     */
    Raw(0),
    /**
     * Player chat message with author name
     */
    Chat(1),
    /**
     * Localized message with translation key and parameters
     */
    Translate(2),
    /**
     * Popup notification with translation key and parameters
     */
    Popup(3),
    /**
     * Jukebox 'Now Playing' popup notification
     */
    JukeboxPopup(4),
    /**
     * Action bar tip message
     */
    Tip(5),
    /**
     * System-generated message
     */
    SystemMessage(6),
    /**
     * Private message between players
     */
    Whisper(7),
    /**
     * Broadcast announcement message with author
     */
    Announcement(8),
    /**
     * Private message using text object format
     */
    TextObjectWhisper(9),
    /**
     * Rich text message using JSON text object format
     */
    TextObject(10),
    /**
     * Broadcast announcement using text object format
     */
    TextObjectAnnouncement(11),
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
