// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerAuthInputPacketPayload_InputData {

    Ascend(0),
    Descend(1),
    NorthJump(2),
    JumpDown(3),
    SprintDown(4),
    ChangeHeight(5),
    Jumping(6),
    AutoJumpingInWater(7),
    Sneaking(8),
    SneakDown(9),
    Up(10),
    Down(11),
    Left(12),
    Right(13),
    UpLeft(14),
    UpRight(15),
    WantUp(16),
    WantDown(17),
    WantDownSlow(18),
    WantUpSlow(19),
    Sprinting(20),
    AscendBlock(21),
    DescendBlock(22),
    SneakToggleDown(23),
    PersistSneak(24),
    StartSprinting(25),
    StopSprinting(26),
    StartSneaking(27),
    StopSneaking(28),
    StartSwimming(29),
    StopSwimming(30),
    StartJumping(31),
    StartGliding(32),
    StopGliding(33),
    PerformItemInteraction(34),
    PerformBlockActions(35),
    PerformItemStackRequest(36),
    HandledTeleport(37),
    Emoting(38),
    MissedSwing(39),
    StartCrawling(40),
    StopCrawling(41),
    StartFlying(42),
    StopFlying(43),
    ClientAckServerData(44),
    IsInClientPredictedVehicle(45),
    PaddlingLeft(46),
    PaddlingRight(47),
    BlockBreakingDelayEnabled(48),
    HorizontalCollision(49),
    VerticalCollision(50),
    DownLeft(51),
    DownRight(52),
    StartUsingItem(53),
    IsCameraRelativeMovementEnabled(54),
    IsRotControlledByMoveDirection(55),
    StartSpinAttack(56),
    StopSpinAttack(57),
    IsHotbarOnlyTouch(58),
    JumpReleasedRaw(59),
    JumpPressedRaw(60),
    JumpCurrentRaw(61),
    SneakReleasedRaw(62),
    SneakPressedRaw(63),
    SneakCurrentRaw(64),
    InternalUpdate(65),
    ;

    private static final Int2ObjectMap<PlayerAuthInputPacketPayload_InputData> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerAuthInputPacketPayload_InputData value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PlayerAuthInputPacketPayload_InputData getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerAuthInputPacketPayload_InputData getByValue(final int value, final PlayerAuthInputPacketPayload_InputData fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PlayerAuthInputPacketPayload_InputData getByName(final String name) {
        for (PlayerAuthInputPacketPayload_InputData value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PlayerAuthInputPacketPayload_InputData getByName(final String name, final PlayerAuthInputPacketPayload_InputData fallback) {
        for (PlayerAuthInputPacketPayload_InputData value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PlayerAuthInputPacketPayload_InputData(final PlayerAuthInputPacketPayload_InputData value) {
        this(value.value);
    }

    PlayerAuthInputPacketPayload_InputData(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
