// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ActorEvent {

    none(0),
    jump(1),
    hurt(2),
    death(3),
    start_attacking(4),
    stop_attacking(5),
    taming_failed(6),
    taming_succeeded(7),
    shake_wetness(8),
    eat_grass(10),
    fishhook_bubble(11),
    fishhook_fishpos(12),
    fishhook_hooktime(13),
    fishhook_tease(14),
    squid_fleeing(15),
    zombie_converting(16),
    play_ambient(17),
    spawn_alive(18),
    start_offer_flower(19),
    stop_offer_flower(20),
    love_hearts(21),
    villager_angry(22),
    villager_happy(23),
    witch_hat_magic(24),
    fireworks_explode(25),
    in_love_hearts(26),
    silverfish_merge_anim(27),
    guardian_attack_sound(28),
    drink_potion(29),
    throw_potion(30),
    prime_tntcart(31),
    prime_creeper(32),
    air_supply(33),
    deprecated_add_player_levels(34),
    guardian_mining_fatigue(35),
    agent_swing_arm(36),
    dragon_start_death_anim(37),
    ground_dust(38),
    shake(39),
    feed(57),
    baby_age(60),
    instant_death(61),
    notify_trade(62),
    leash_destroyed(63),
    caravan_updated(64),
    talisman_activate(65),
    deprecated_update_structure_feature(66),
    player_spawned_mob(67),
    puke(68),
    update_stack_size(69),
    start_swimming(70),
    balloon_pop(71),
    treasure_hunt(72),
    summon_agent(73),
    finished_charging_item(74),
    actor_grow_up(76),
    vibration_detected(77),
    drink_milk(78),
    shake_wetness_stop(79),
    kinetic_damage_dealt(80),
    hurt_without_receiving_damage(81),
    ;

    private static final Int2ObjectMap<ActorEvent> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ActorEvent value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ActorEvent getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ActorEvent getByValue(final int value, final ActorEvent fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ActorEvent getByName(final String name) {
        for (ActorEvent value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ActorEvent getByName(final String name, final ActorEvent fallback) {
        for (ActorEvent value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ActorEvent(final ActorEvent value) {
        this(value.value);
    }

    ActorEvent(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
