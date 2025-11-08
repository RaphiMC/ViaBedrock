// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SetTitlePacketPayload_TitleType {

    Clear(0),
    Reset(1),
    Title(2),
    Subtitle(3),
    Actionbar(4),
    Times(5),
    TitleTextObject(6),
    SubtitleTextObject(7),
    ActionbarTextObject(8);

    private static final Int2ObjectMap<SetTitlePacketPayload_TitleType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SetTitlePacketPayload_TitleType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static SetTitlePacketPayload_TitleType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SetTitlePacketPayload_TitleType getByValue(final int value, final SetTitlePacketPayload_TitleType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    SetTitlePacketPayload_TitleType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
