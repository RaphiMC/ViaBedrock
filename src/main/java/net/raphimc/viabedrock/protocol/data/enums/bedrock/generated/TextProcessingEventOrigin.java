// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum TextProcessingEventOrigin {

    unknown(-1),
    serverchatpublic(0),
    serverchatwhisper(1),
    signtext(2),
    anviltext(3),
    bookandquilltext(4),
    commandblocktext(5),
    blockactordatatext(6),
    joineventtext(7),
    leaveeventtext(8),
    slashcommandchat(9),
    cartographytext(10),
    kickcommand(11),
    titlecommand(12),
    summoncommand(13),
    serverform(14),
    datadrivenui(15),
    ;

    private static final Int2ObjectMap<TextProcessingEventOrigin> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (TextProcessingEventOrigin value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static TextProcessingEventOrigin getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static TextProcessingEventOrigin getByValue(final int value, final TextProcessingEventOrigin fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static TextProcessingEventOrigin getByName(final String name) {
        for (TextProcessingEventOrigin value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static TextProcessingEventOrigin getByName(final String name, final TextProcessingEventOrigin fallback) {
        for (TextProcessingEventOrigin value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
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
