// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum GamePublishSetting {

    NoMultiPlay(0),
    InviteOnly(1),
    FriendsOnly(2),
    FriendsOfFriends(3),
    Public(4),
    ;

    private static final Int2ObjectMap<GamePublishSetting> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (GamePublishSetting value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static GamePublishSetting getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static GamePublishSetting getByValue(final int value, final GamePublishSetting fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static GamePublishSetting getByName(final String name) {
        for (GamePublishSetting value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static GamePublishSetting getByName(final String name, final GamePublishSetting fallback) {
        for (GamePublishSetting value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    GamePublishSetting(final GamePublishSetting value) {
        this(value.value);
    }

    GamePublishSetting(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
