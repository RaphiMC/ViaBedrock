// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum TextProcessingEventOrigin {

    unknown(-1),
    ServerChatPublic(0),
    ServerChatWhisper(1),
    SignText(2),
    AnvilText(3),
    BookAndQuillText(4),
    CommandBlockText(5),
    BlockActorDataText(6),
    JoinEventText(7),
    LeaveEventText(8),
    SlashCommandChat(9),
    CartographyText(10),
    KickCommand(11),
    TitleCommand(12),
    SummonCommand(13),
    ServerForm(14);

    private static final Int2ObjectMap<TextProcessingEventOrigin> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (TextProcessingEventOrigin value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static TextProcessingEventOrigin getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static TextProcessingEventOrigin getByValue(final int value, final TextProcessingEventOrigin fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    TextProcessingEventOrigin(final TextProcessingEventOrigin value) {
        this(value.value);
    }

    TextProcessingEventOrigin(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
