// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SetTitlePacketPayload_TitleType {

    clear(0),
    reset(1),
    title(2),
    subtitle(3),
    actionbar(4),
    times(5),
    titletextobject(6),
    subtitletextobject(7),
    actionbartextobject(8),
    ;

    private static final Int2ObjectMap<SetTitlePacketPayload_TitleType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SetTitlePacketPayload_TitleType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SetTitlePacketPayload_TitleType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SetTitlePacketPayload_TitleType getByValue(final int value, final SetTitlePacketPayload_TitleType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SetTitlePacketPayload_TitleType getByName(final String name) {
        for (SetTitlePacketPayload_TitleType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SetTitlePacketPayload_TitleType getByName(final String name, final SetTitlePacketPayload_TitleType fallback) {
        for (SetTitlePacketPayload_TitleType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SetTitlePacketPayload_TitleType(final SetTitlePacketPayload_TitleType value) {
        this(value.value);
    }

    SetTitlePacketPayload_TitleType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
