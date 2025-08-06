// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MultiplayerSettingsPacketType {

    EnableMultiplayer(0),
    DisableMultiplayer(1),
    RefreshJoincode(2);

    private static final Int2ObjectMap<MultiplayerSettingsPacketType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MultiplayerSettingsPacketType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static MultiplayerSettingsPacketType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MultiplayerSettingsPacketType getByValue(final int value, final MultiplayerSettingsPacketType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    MultiplayerSettingsPacketType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
