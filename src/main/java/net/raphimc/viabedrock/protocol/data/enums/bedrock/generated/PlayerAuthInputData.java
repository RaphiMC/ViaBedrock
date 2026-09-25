// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerAuthInputData {

    Ascend(0),
    Descend(1),
    JumpDown(2),
    SprintDown(3),
    ChangeHeight(4),
    Jumping(5),
    AutoJumpingInWater(6),
    Sneaking(7),
    SneakDown(8),
    Up(9),
    Down(10),
    Left(11),
    Right(12),
    UpLeft(13),
    UpRight(14),
    WantUp(15),
    WantDown(16),
    WantDownSlow(17),
    WantUpSlow(18),
    Sprinting(19),
    AscendBlock(20),
    DescendBlock(21),
    SneakToggleDown(22),
    PersistSneak(23),
    StartSprinting(24),
    StopSprinting(25),
    StartSneaking(26),
    StopSneaking(27),
    StartSwimming(28),
    StopSwimming(29),
    StartJumping(30),
    StartGliding(31),
    StopGliding(32),
    PerformItemInteraction(33),
    PerformBlockActions(34),
    PerformItemStackRequest(35),
    HandledTeleport(36),
    Emoting(37),
    MissedSwing(38),
    StartCrawling(39),
    StopCrawling(40),
    StartFlying(41),
    StopFlying(42),
    ClientAckServerData(43),
    IsInClientPredictedVehicle(44),
    PaddlingLeft(45),
    PaddlingRight(46),
    BlockBreakingDelayEnabled(47),
    HorizontalCollision(48),
    VerticalCollision(49),
    DownLeft(50),
    DownRight(51),
    StartUsingItem(52),
    StartSpinAttack(53),
    StopSpinAttack(54),
    IsHotbarOnlyTouch(55),
    JumpReleasedRaw(56),
    JumpPressedRaw(57),
    JumpCurrentRaw(58),
    SneakReleasedRaw(59),
    SneakPressedRaw(60),
    SneakCurrentRaw(61),
    InternalUpdate(62),
    ;

    private static final Int2ObjectMap<PlayerAuthInputData> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerAuthInputData value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PlayerAuthInputData getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerAuthInputData getByValue(final int value, final PlayerAuthInputData fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PlayerAuthInputData getByName(final String name) {
        for (PlayerAuthInputData value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PlayerAuthInputData getByName(final String name, final PlayerAuthInputData fallback) {
        for (PlayerAuthInputData value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PlayerAuthInputData(final PlayerAuthInputData value) {
        this(value.value);
    }

    PlayerAuthInputData(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
