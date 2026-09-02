// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerActionType {

    Unknown(-1),
    StartDestroyBlock(0),
    AbortDestroyBlock(1),
    StopDestroyBlock(2),
    GetUpdatedBlock(3),
    DropItem(4),
    StartSleeping(5),
    StopSleeping(6),
    Respawn(7),
    StartJump(8),
    StartSprinting(9),
    StopSprinting(10),
    StartSneaking(11),
    StopSneaking(12),
    CreativeDestroyBlock(13),
    ChangeDimensionAck(14),
    StartGliding(15),
    StopGliding(16),
    DenyDestroyBlock(17),
    CrackBlock(18),
    ChangeSkin(19),
    UpdatedEnchantingSeed(20),
    StartSwimming(21),
    StopSwimming(22),
    StartSpinAttack(23),
    StopSpinAttack(24),
    InteractWithBlock(25),
    PredictDestroyBlock(26),
    ContinueDestroyBlock(27),
    StartItemUseOn(28),
    StopItemUseOn(29),
    HandledTeleport(30),
    MissedSwing(31),
    StartCrawling(32),
    StopCrawling(33),
    StartFlying(34),
    StopFlying(35),
    ClientAckServerData(36),
    StartUsingItem(37),
    InternalUpdate(38),
    Count(39),
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
