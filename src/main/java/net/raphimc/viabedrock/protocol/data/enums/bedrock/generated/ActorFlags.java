// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ActorFlags {

    ONFIRE(0),
    SNEAKING(1),
    RIDING(2),
    SPRINTING(3),
    USINGITEM(4),
    INVISIBLE(5),
    TEMPTED(6),
    INLOVE(7),
    SADDLED(8),
    POWERED(9),
    IGNITED(10),
    BABY(11),
    CONVERTING(12),
    CRITICAL(13),
    CAN_SHOW_NAME(14),
    ALWAYS_SHOW_NAME(15),
    NOAI(16),
    SILENT(17),
    WALLCLIMBING(18),
    CANCLIMB(19),
    CANSWIM(20),
    CANFLY(21),
    CANWALK(22),
    RESTING(23),
    SITTING(24),
    ANGRY(25),
    INTERESTED(26),
    CHARGED(27),
    TAMED(28),
    ORPHANED(29),
    LEASHED(30),
    SHEARED(31),
    GLIDING(32),
    ELDER(33),
    MOVING(34),
    BREATHING(35),
    CHESTED(36),
    STACKABLE(37),
    SHOW_BOTTOM(38),
    STANDING(39),
    SHAKING(40),
    IDLING(41),
    CASTING(42),
    CHARGING(43),
    WASD_CONTROLLED(44),
    CAN_POWER_JUMP(45),
    CAN_DASH(46),
    LINGERING(47),
    HAS_COLLISION(48),
    HAS_GRAVITY(49),
    FIRE_IMMUNE(50),
    DANCING(51),
    ENCHANTED(52),
    RETURNTRIDENT(53),
    CONTAINER_IS_PRIVATE(54),
    IS_TRANSFORMING(55),
    DAMAGENEARBYMOBS(56),
    SWIMMING(57),
    BRIBED(58),
    IS_PREGNANT(59),
    LAYING_EGG(60),
    PASSENGER_CAN_PICK(61),
    TRANSITION_SITTING(62),
    EATING(63),
    LAYING_DOWN(64),
    SNEEZING(65),
    TRUSTING(66),
    ROLLING(67),
    SCARED(68),
    IN_SCAFFOLDING(69),
    OVER_SCAFFOLDING(70),
    DESCEND_THROUGH_BLOCK(71),
    BLOCKING(72),
    TRANSITION_BLOCKING(73),
    BLOCKED_USING_SHIELD(74),
    BLOCKED_USING_DAMAGED_SHIELD(75),
    SLEEPING(76),
    WANTS_TO_WAKE(77),
    TRADE_INTEREST(78),
    DOOR_BREAKER(79),
    BREAKING_OBSTRUCTION(80),
    DOOR_OPENER(81),
    IS_ILLAGER_CAPTAIN(82),
    STUNNED(83),
    ROARING(84),
    DELAYED_ATTACK(85),
    IS_AVOIDING_MOBS(86),
    IS_AVOIDING_BLOCK(87),
    FACING_TARGET_TO_RANGE_ATTACK(88),
    HIDDEN_WHEN_INVISIBLE(89),
    IS_IN_UI(90),
    STALKING(91),
    EMOTING(92),
    CELEBRATING(93),
    ADMIRING(94),
    CELEBRATING_SPECIAL(95),
    OUT_OF_CONTROL(96),
    RAM_ATTACK(97),
    PLAYING_DEAD(98),
    IN_ASCENDABLE_BLOCK(99),
    OVER_DESCENDABLE_BLOCK(100),
    CROAKING(101),
    EAT_MOB(102),
    JUMP_GOAL_JUMP(103),
    EMERGING(104),
    SNIFFING(105),
    DIGGING(106),
    SONIC_BOOM(107),
    HAS_DASH_COOLDOWN(108),
    PUSH_TOWARDS_CLOSEST_SPACE(109),
    SEARCHING(113),
    CRAWLING(114),
    TIMER_FLAG_1(115),
    TIMER_FLAG_2(116),
    TIMER_FLAG_3(117),
    BODY_ROTATION_BLOCKED(118),
    RENDERS_WHEN_INVISIBLE(119),
    ROTATION_AXIS_ALIGNED(120),
    COLLIDABLE(121),
    WASD_FREE_CAMERA_CONTROLLED(122),
    DOES_SERVER_AUTH_ONLY_DISMOUNT(123),
    BODY_ROTATION_ALWAYS_FOLLOWS_HEAD(124),
    CAN_USE_VERTICAL_MOVEMENT_ACTION(125);

    private static final Int2ObjectMap<ActorFlags> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ActorFlags value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ActorFlags getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ActorFlags getByValue(final int value, final ActorFlags fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ActorFlags(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
