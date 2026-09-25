// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerActionType {

    Unknown(0),
    StartDestroyBlock(1),
    AbortDestroyBlock(2),
    StopDestroyBlock(3),
    StartSleeping(4),
    StopSleeping(5),
    Respawn(6),
    StartJump(7),
    StartSprinting(8),
    StopSprinting(9),
    StartSneaking(10),
    StopSneaking(11),
    CreativeDestroyBlock(12),
    ChangeDimensionAck(13),
    StartGliding(14),
    StopGliding(15),
    DenyDestroyBlock(16),
    CrackBlock(17),
    StartSwimming(18),
    StopSwimming(19),
    StartSpinAttack(20),
    StopSpinAttack(21),
    PredictDestroyBlock(22),
    ContinueDestroyBlock(23),
    StartItemUseOn(24),
    StopItemUseOn(25),
    HandledTeleport(26),
    MissedSwing(27),
    StartCrawling(28),
    StopCrawling(29),
    StartFlying(30),
    StopFlying(31),
    StartUsingItem(32),
    InternalUpdate(33),
    Count(34),
    ;

    private static final Int2ObjectMap<PlayerActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PlayerActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerActionType getByValue(final int value, final PlayerActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PlayerActionType getByName(final String name) {
        for (PlayerActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PlayerActionType getByName(final String name, final PlayerActionType fallback) {
        for (PlayerActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PlayerActionType(final PlayerActionType value) {
        this(value.value);
    }

    PlayerActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
