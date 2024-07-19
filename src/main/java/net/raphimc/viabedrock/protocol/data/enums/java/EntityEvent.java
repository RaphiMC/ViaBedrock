/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viabedrock.protocol.data.enums.java;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EntityEvent {

    JUMP(1),
    DEATH(3),
    START_ATTACKING(4),
    STOP_ATTACKING(5),
    TAMING_FAILED(6),
    TAMING_SUCCEEDED(7),
    SHAKE_WETNESS(8),
    USE_ITEM_COMPLETE(9),
    EAT_GRASS(10),
    OFFER_FLOWER(11),
    LOVE_HEARTS(12),
    VILLAGER_ANGRY(13),
    VILLAGER_HAPPY(14),
    WITCH_HAT_MAGIC(15),
    ZOMBIE_CONVERTING(16),
    FIREWORKS_EXPLODE(17),
    IN_LOVE_HEARTS(18),
    SQUID_ANIM_SYNCH(19),
    SILVERFISH_MERGE_ANIM(20),
    GUARDIAN_ATTACK_SOUND(21),
    REDUCED_DEBUG_INFO(22),
    FULL_DEBUG_INFO(23),
    PERMISSION_LEVEL_ALL(24),
    PERMISSION_LEVEL_MODERATORS(25),
    PERMISSION_LEVEL_GAMEMASTERS(26),
    PERMISSION_LEVEL_ADMINS(27),
    PERMISSION_LEVEL_OWNERS(28),
    ATTACK_BLOCKED(29),
    SHIELD_DISABLED(30),
    FISHING_ROD_REEL_IN(31),
    ARMORSTAND_WOBBLE(32),
    STOP_OFFER_FLOWER(34),
    TALISMAN_ACTIVATE(35),
    DOLPHIN_LOOKING_FOR_TREASURE(38),
    RAVAGER_STUNNED(39),
    TRUSTING_FAILED(40),
    TRUSTING_SUCCEEDED(41),
    VILLAGER_SWEAT(42),
    FOX_EAT(45),
    TELEPORT(46),
    MAINHAND_BREAK(47),
    OFFHAND_BREAK(48),
    HEAD_BREAK(49),
    CHEST_BREAK(50),
    LEGS_BREAK(51),
    FEET_BREAK(52),
    HONEY_SLIDE(53),
    HONEY_JUMP(54),
    SWAP_HANDS(55),
    CANCEL_SHAKE_WETNESS(56),
    START_RAM(58),
    END_RAM(59),
    POOF(60),
    TENDRILS_SHIVER(61),
    SONIC_CHARGE(62),
    SNIFFER_DIGGING_SOUND(63),
    ARMADILLO_PEEK(64),
    BODY_BREAK(65);

    private static final Int2ObjectMap<EntityEvent> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (EntityEvent value : values()) {
            BY_VALUE.put(value.value, value);
        }
    }

    public static EntityEvent getByValue(final byte value) {
        return BY_VALUE.get(value);
    }

    public static EntityEvent getByValue(final byte value, final EntityEvent fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final byte value;

    EntityEvent(final int value) {
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }

}
