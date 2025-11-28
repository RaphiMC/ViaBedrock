// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerActionType {

    Unknown(-1),
    /**
     * Sent in Player Auth Input Block Actions with position and facing
     */
    StartDestroyBlock(0),
    /**
     * Sent in Player Auth Input Block Actions with position and facing
     */
    AbortDestroyBlock(1),
    /**
     * Sent in Player Auth Input Block Actions without additional data
     */
    StopDestroyBlock(2),
    GetUpdatedBlock(3),
    DropItem(4),
    /**
     * Sent in Player Action
     */
    StartSleeping(5),
    /**
     * Sent in Player Action
     */
    StopSleeping(6),
    /**
     * Sent in Player Action
     */
    Respawn(7),
    /**
     * Set on the tick that a player triggers a jump.<br>
     * Corresponds to Player Auth Input InputData::StartJumping bit 31
     */
    StartJump(8),
    /**
     * Set when the player wants to start sprinting, like double tapping forward.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SPRINTING and an UpdateAttributesPacket to apply the sprint boost.<br>
     * Corresponds to Player Auth Input InputData::StartSprinting bit 25
     */
    StartSprinting(9),
    /**
     * Sent when the player wants to stop sprinting, like releasing the forward input while sprinting.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SPRINTING and an UpdateAttributesPacket clearing the sprint speed boost.<br>
     * Corresponds to Player Auth Input InputData::StopSprinting bit 26
     */
    StopSprinting(10),
    /**
     * Sent when the player wants to start sneaking like pressing shift.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SNEAKING true if accepted, false if rejected, and a bounding box update.<br>
     * Corresponds to Player Auth Input InputData::StartSneaking bit 27
     */
    StartSneaking(11),
    /**
     * Sent when the player wants to stop sneaking like releasing shift.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SNEAKING false if accepted, true if rejected, and a bounding box update.<br>
     * Corresponds to Player Auth Input InputData::StopSneaking bit 28
     */
    StopSneaking(12),
    /**
     * Sent when trying to destroy a block in creative like left clicking on it. Expects server to destroy the block and optionally send new block or chunk information.<br>
     * Used to be a ChangeDimension action.<br>
     * Sent in Player Action.
     */
    CreativeDestroyBlock(13),
    /**
     * Sent in Player Action, this is the one case of the server sending an action to the client to start a dimension change.
     */
    ChangeDimensionAck(14),
    /**
     * Sent when the player wants to start elytra gliding like pressing spacebar in air.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::GLIDING true if accepted or false if rejected, and a bounding box update.<br>
     * Corresponds to Player Auth Input InputData::StartGliding bit 32
     */
    StartGliding(15),
    /**
     * Sent when the player is elytra gliding but expects it to stop, like when touching the ground.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::GLIDING false if accepted or true if rejected, and a bounding box update.<br>
     * Corresponds to Player Auth Input InputData::StopGliding bit 33
     */
    StopGliding(16),
    /**
     * Sent when the client thinks they aren't allowed to break a block at the location and want the deny particle effect.<br>
     * Sent in Player Action in EDU
     */
    DenyDestroyBlock(17),
    /**
     * Client expects a LevelEventPacket with the appropriate crack event to be broadcast in response.<br>
     * Only sent if server auth block breaking is disabled in StartGamePacket.<br>
     * Sent in Player Auth Input Block Actions with position and facing.
     */
    CrackBlock(18),
    ChangeSkin(19),
    UpdatedEnchantingSeed(20),
    /**
     * Sent when the player wants to enter swimming mode like pressing control while moving forward in water.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SWIMMING set to true if accepted or false if rejected, and a bounding box update.<br>
     * Corresponds to Player Auth Input InputData::StartSwimming bit 29
     */
    StartSwimming(21),
    /**
     * Sent when the player wants to exit swimming mode like when releasing the forward input while swimming.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::SWIMMMING set to false if accepted or true if rejected, and a bounding box update.<br>
     * Corresponds to Player Auth Input InputData::StopSwimming bit 30
     */
    StopSwimming(22),
    /**
     * Sent on the tick that the client predicts a riptide spin attack starting. It is accompanied by an InventoryTransactionPacket of type ComplexInventoryTransaction::Type::ItemUseTransaction.<br>
     * Server is expected to send a SetActorDataPacket with ActorFlags::DAMAGENEARBYMOBS set to true if accepted or false if rejected along with a bounding box update.<br>
     * Sent in Player Action but will soon turn into a Player Auth Input InputData bit
     */
    StartSpinAttack(23),
    /**
     * Sent when the client thinks a riptide spin attack has ended.<br>
     * Server is expected to send a SetActorDataPacket with ActorFlags::DAMAGENEARBYMOBS set to false if accepted or true if rejected along with a bounding box update.<br>
     * Sent in Player Action but will soon turn into a Player Auth Input InputData bit
     */
    StopSpinAttack(24),
    InteractWithBlock(25),
    /**
     * Sent in Player Auth Input Block Actions with position and facing.<br>
     * Used for the client to inform the server that it predicted the player destroying a block.<br>
     * The server may respond with block, chunk, or item information if it disagrees, or send no response to imply agreement.<br>
     * Only used when server-auth block breaking toggle is on as specified in StartGamePacket
     */
    PredictDestroyBlock(26),
    /**
     * Sent in Player Auth Input Block Actions with position and facing.<br>
     * Used to inform the server that the client's current block changed for block destruction.<br>
     * The server is expected to use this to progress the block destruction and await an upcoming PredictDestroyBlock action.<br>
     * They are also expected to broadcast LevelEventPackets for the block cracking of the block being destroyed.<br>
     * Only sent when server-auth block breaking toggle is on as specified in StartGamePacket
     */
    ContinueDestroyBlock(27),
    /**
     * Sent upon starting right click and hold style item use.<br>
     * Sent in Player Action.<br>
     * Server can expect this to arrive with an InventoryTransactionPacket with ItemUseInventoryTransaction in it.
     */
    StartItemUseOn(28),
    /**
     * Sent upon releasing right click and hold style item use. This is for canceling the action, not the same as firing a bow which would be InventoryTransactionPacket with ItemUseInventoryTransaction.<br>
     * Sent in Player Action
     */
    StopItemUseOn(29),
    /**
     * Used to inform the server that we have received a MovePlayerPacket causing a teleport, and re-enable client auth movement.<br>
     * The server should ignore any client predicted positions from the moment a MovePlayerPacket was sent until receipt of this action.<br>
     * Corresponds to Player Auth Input InputData::HandledTeleport bit 37
     */
    HandledTeleport(30),
    /**
     * Sent when client wants to play the arm swing animation like for left click.<br>
     * Server is expected to broadcast a LevelSoundEventPacket with LevelSoundEvent::AttackNoDamage.<br>
     * Corresponds to Player Auth Input InputData::MissedSwing bit 39
     */
    MissedSwing(31),
    /**
     * Sent when the player is standing and thinks there is not enough space to stand.<br>
     * Server is expected to respond with a SetActorDataPacket containing a bounding box update.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::CRAWLING set to true if accepted, or false if rejected.<br>
     * Corresponds to Player Auth Input InputData::StartCrawling bit 40
     */
    StartCrawling(32),
    /**
     * Sent when the player was crawling and thinks there is space to stand.<br>
     * Server is expected to respond with a SetActorDataPacket containing a bounding box update.<br>
     * Server is expected to respond with SetActorDataPacket with ActorFlags::CRAWLING set to false if accepted, or true if rejected.<br>
     * Corresponds to Player Auth Input InputData::StopCrawling bit 41
     */
    StopCrawling(33),
    /**
     * Sent when the player expects flight to be toggled on like double tap spacebar.<br>
     * Server is expected to respond with an UpdateAbilitiesPacket to accept or reject this.<br>
     * Corresponds to Player Auth Input InputData::StartFlying bit 42
     */
    StartFlying(34),
    /**
     * Sent when the player expects flight to be toggled off like double tap spacebar.<br>
     * Server is expected to respond with an UpdateAbilitiesPacket to accept or reject this.<br>
     * Corresponds to Player Auth Input InputData::StopFlying bit 43
     */
    StopFlying(35),
    ClientAckServerData(36),
    /**
     * Used to inform the server that we are predicting using an item.
     */
    StartUsingItem(37);

    private static final Int2ObjectMap<PlayerActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PlayerActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerActionType getByValue(final int value, final PlayerActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
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
