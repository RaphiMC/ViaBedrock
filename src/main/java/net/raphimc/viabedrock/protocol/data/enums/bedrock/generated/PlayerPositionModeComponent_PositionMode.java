// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerPositionModeComponent_PositionMode {

    Normal(0),
    Respawn(1),
    Teleport(2),
    OnlyHeadRot(3);

    private static final Int2ObjectMap<PlayerPositionModeComponent_PositionMode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerPositionModeComponent_PositionMode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PlayerPositionModeComponent_PositionMode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerPositionModeComponent_PositionMode getByValue(final int value, final PlayerPositionModeComponent_PositionMode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PlayerPositionModeComponent_PositionMode(final PlayerPositionModeComponent_PositionMode value) {
        this(value.value);
    }

    PlayerPositionModeComponent_PositionMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
