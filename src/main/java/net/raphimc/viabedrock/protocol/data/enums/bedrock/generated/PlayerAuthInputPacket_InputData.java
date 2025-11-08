// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerAuthInputPacket_InputData {

    /**
     * Touch input for flying up, similar to WantUp
     */
    Ascend(0),
    /**
     * Touch input for flying down, similar to WantDown
     */
    Descend(1),
    /**
     * If jump input is down. Doesn't necessarily mean the player is jumping.
     */
    JumpDown(3),
    /**
     * If sprint input is down. Doesn't necessarily mean they want to start sprinting
     */
    SprintDown(4),
    /**
     * Touch input for flying down
     */
    ChangeHeight(5),
    /**
     * If the jump input is down or auto jump, even in non-jump cases like flying or swimming
     */
    Jumping(6),
    /**
     * If an auto jump is currently triggering while touching water. Can be ignored if handling Jumping properly
     */
    AutoJumpingInWater(7),
    /**
     * If the player is sneaking, which may be from input or because there's not enough space to stand
     */
    Sneaking(8),
    /**
     * If the sneak input is down, which may not mean that they are sneaking depending on input permission and if they're crawling instead
     */
    SneakDown(9),
    /**
     * Local space up input. Equivalent to the move input Y being positive.
     */
    Up(10),
    /**
     * Local space down input. Equivalent to the move input Y being negative.
     */
    Down(11),
    /**
     * Local space left input. Equivalent to the move input X being negative.
     */
    Left(12),
    /**
     * Local space right input. Equivalent to the move input X being positive.
     */
    Right(13),
    /**
     * Local space diagonal up and left. Equivalent to move input (-1, 1) normalized.
     */
    UpLeft(14),
    /**
     * Local space diagonal up and right. Equivalent to move input (1, 1) normalized.
     */
    UpRight(15),
    /**
     * Flying upwards like holding spacebar, all input modes
     */
    WantUp(16),
    /**
     * Flying downwards like holding shift, all input modes
     */
    WantDown(17),
    /**
     * Alternate flying downwards for gamepad
     */
    WantDownSlow(18),
    /**
     * Alternate flying upwards for gamepad
     */
    WantUpSlow(19),
    /**
     * If the client thinks they're sprinting. Changes to this come in as start and stop sprinting actions
     */
    Sprinting(20),
    /**
     * Touch-specific input for ascending scaffolding
     */
    AscendBlock(21),
    /**
     * Touch-specific input for descending scaffolding
     */
    DescendBlock(22),
    /**
     * Set while sneak toggle is being pressed for touch and gamepad. See Sneaking for the toggle state.
     */
    SneakToggleDown(23),
    /**
     * Always true when using touch input
     */
    PersistSneak(24),
    /**
     * Set when the player wants to start sprinting, like double tapping forward.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SPRINTING and an UpdateAttributesPacket to apply the sprint boost.
     */
    StartSprinting(25),
    /**
     * Sent when the player wants to stop sprinting, like releasing the forward input while sprinting.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SPRINTING and an UpdateAttributesPacket clearing the sprint speed boost.
     */
    StopSprinting(26),
    /**
     * Sent when the player wants to start sneaking like pressing shift.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SNEAKING true if accepted, false if rejected, and a bounding box update.
     */
    StartSneaking(27),
    /**
     * Sent when the player wants to stop sneaking like releasing shift.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SNEAKING false if accepted, true if rejected, and a bounding box update.
     */
    StopSneaking(28),
    /**
     * Sent when the player wants to enter swimming mode like pressing control while moving forward in water.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SWIMMING set to true if accepted or false if rejected, and a bounding box update.
     */
    StartSwimming(29),
    /**
     * Sent when the player wants to exit swimming mode like when releasing the forward input while swimming.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SWIMMMING set to false if accepted or true if rejected, and a bounding box update.
     */
    StopSwimming(30),
    /**
     * Set on the tick that the client triggers a non-vehicle jump
     */
    StartJumping(31),
    /**
     * Sent when the player wants to start elytra gliding like pressing spacebar in air.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::GLIDING true if accepted or false if rejected, and a bounding box update.
     */
    StartGliding(32),
    /**
     * Sent when the player is elytra gliding but expects it to stop, like when touching the ground.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::GLIDING false if accepted or true if rejected, and a bounding box update.
     */
    StopGliding(33),
    /**
     * Indicates that mItemUseTransaction will be written to the packet
     */
    PerformItemInteraction(34),
    /**
     * Indicates that mPlayerBlockActions will be written to the packet
     */
    PerformBlockActions(35),
    /**
     * Indicates mItemStackRequest will be written to the packet
     */
    PerformItemStackRequest(36),
    /**
     * Used to inform the server that we have received a MovePlayerPacket causing a teleport, and re-enable client auth movement.<br>
     * The server should ignore any client predicted positions from the moment a MovePlayerPacket was sent until receipt of this action.
     */
    HandledTeleport(37),
    /**
     * If the player is currently performing an emote, see EmotePacket
     */
    Emoting(38),
    /**
     * Sent when client wants to play the arm swing animation like for left click.<br>
     * Server is expected to broadcast a LevelSoundEventPacket with LevelSoundEvent::AttackNoDamage.
     */
    MissedSwing(39),
    /**
     * Sent when the player is standing and thinks there is not enough space to stand.<br>
     * Server is expected to respond with a SetActorDataPacket containing a bounding box update.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::CRAWLING set to true if accepted, or false if rejected.
     */
    StartCrawling(40),
    /**
     * Sent when the player was crawling and thinks there is space to stand.<br>
     * Server is expected to respond with a SetActorDataPacket containing a bounding box update.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::CRAWLING set to false if accepted, or true if rejected.
     */
    StopCrawling(41),
    /**
     * Sent when the player expects flight to be toggled on like double tap spacebar.<br>
     * Server is expected to respond with an UpdateAbilitiesPacket to accept or reject this.
     */
    StartFlying(42),
    /**
     * Sent when the player expects flight to be toggled off like double tap spacebar.<br>
     * Server is expected to respond with an UpdateAbilitiesPacket to accept or reject this.
     */
    StopFlying(43),
    /**
     * Not sent when using server authoritative movement as specified in StartGamePacket
     */
    ClientAckServerData(44),
    /**
     * Used when the client sends input while in control of a client predicted vehicle. Aka, Horse and Boat.<br>
     * If set, Vehicle Rotation and Client Predicted Vehicle will be written
     */
    IsInClientPredictedVehicle(45),
    /**
     * Player is in a boat and holding the paddle input.<br>
     * Server is expected to respond with SetActorDataPacket updates of the boat's ROW_TIME_LEFT<br>
     * See Player Auth Input for further details
     */
    PaddlingLeft(46),
    /**
     * Player is in a boat and holding the paddle input.<br>
     * Server is expected to respond with SetActorDataPacket updates of the boat's ROW_TIME_RIGHT<br>
     * See Player Auth Input for further details
     */
    PaddlingRight(47),
    /**
     * For touch input modes in creative, true if block destruction in the current mode should happen with a delay, and false if it should happen instantly.
     */
    BlockBreakingDelayEnabled(48),
    /**
     * Set if the client predicted a horizontal collision. Used to factor in to client acceptance logic.<br>
     * Can be used as a hint to the server or ignored based on desired strictness
     */
    HorizontalCollision(49),
    /**
     * Set if the client predicted a vertical collision. Used to factor in to client acceptance logic.<br>
     * Can be used as a hint to the server or ignored based on desired strictness.<br>
     * Strongly correlates with the 'on ground' state of the player
     */
    VerticalCollision(50),
    /**
     * Local space diagonal down and left. Equivalent to move input (-1, -1) normalized.
     */
    DownLeft(51),
    /**
     * Local space diagonal down and right. Equivalent to move input (1, -1) normalized.
     */
    DownRight(52),
    /**
     * Set on ticks when the client predicted the beginning of an item use animation like raising arm for trident or drinking potion.<br>
     * On this same tick will be an InventoryTransactionPacket of type ComplexInventoryTransaction::Type::ItemUseTransaction.<br>
     * Server is expected to respond with SetActorDataPacket containing ActorFlags::USINGITEM true if they agree, otherwise false.
     */
    StartUsingItem(53),
    /**
     * Set on the tick that the client predicts a riptide spin attack starting, when PlayerActionType::StartSpinAttack is set in PlayerActionComponent.<br>
     * and ActorFlags::DAMAGENEARBYMOBS set true in SetActorDataPacket
     */
    StartSpinAttack(56),
    /**
     * Set on the tick that client thinks a riptide spin attack has ended, when PlayerActionType::StopSpinAttack is set in PlayerActionComponent<br>
     * and ActorFlags::DAMAGENEARBYMOBS set false in SetActorDataPacket
     */
    StopSpinAttack(57),
    /**
     * Indicates if touch is only allowed in the touch bar and not in gameplay.
     */
    IsHotbarOnlyTouch(58),
    /**
     * This is whether or not the jump button was released since the last packet.<br>
     * This will be sent even if input permissions are disabled.
     */
    JumpReleasedRaw(59),
    /**
     * This is whether or not the jump button was pressed since the last packet.<br>
     * This will be sent even if input permissions are disabled.
     */
    JumpPressedRaw(60),
    /**
     * This is whether or not the jump button currently down.<br>
     * This will be sent even if input permissions are disabled.
     */
    JumpCurrentRaw(61),
    /**
     * This is whether or not the sneak button was released since the last packet.<br>
     * This will be sent even if input permissions are disabled.
     */
    SneakReleasedRaw(62),
    /**
     * This is whether or not the sneak button was pressed since the last packet.<br>
     * This will be sent even if input permissions are disabled.
     */
    SneakPressedRaw(63),
    /**
     * This is whether or not the sneak button currently down.<br>
     * This will be sent even if input permissions are disabled.
     */
    SneakCurrentRaw(64);

    private static final Int2ObjectMap<PlayerAuthInputPacket_InputData> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerAuthInputPacket_InputData value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PlayerAuthInputPacket_InputData getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerAuthInputPacket_InputData getByValue(final int value, final PlayerAuthInputPacket_InputData fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PlayerAuthInputPacket_InputData(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
