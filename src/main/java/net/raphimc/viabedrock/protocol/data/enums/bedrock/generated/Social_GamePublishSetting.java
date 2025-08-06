// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Social_GamePublishSetting {

    NoMultiPlay(0),
    InviteOnly(1),
    FriendsOnly(2),
    FriendsOfFriends(3),
    Public(4);

    private static final Int2ObjectMap<Social_GamePublishSetting> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Social_GamePublishSetting value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static Social_GamePublishSetting getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Social_GamePublishSetting getByValue(final int value, final Social_GamePublishSetting fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    Social_GamePublishSetting(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
