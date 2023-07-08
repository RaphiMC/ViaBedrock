/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol.data.enums.bedrock;

public class WindowIds {

    public static final byte DROP_CONTENTS = -100;

    public static final byte BEACON = -24;
    public static final byte TRADING_OUTPUT = -23;
    public static final byte TRADING_USE_INPUTS = -22;
    public static final byte TRADING_INPUT_2 = -21;
    public static final byte TRADING_INPUT_1 = -20;

    public static final byte ENCHANT_OUTPUT = -17;
    public static final byte ENCHANT_MATERIAL = -16;
    public static final byte ENCHANT_INPUT = -15;

    public static final byte ANVIL_OUTPUT = -13;
    public static final byte ANVIL_RESULT = -12;
    public static final byte ANVIL_MATERIAL = -11;
    public static final byte CONTAINER_INPUT = -10;

    public static final byte CRAFTING_USE_INGREDIENT = -5;
    public static final byte CRAFTING_RESULT = -4;
    public static final byte CRAFTING_REMOVE_INGREDIENT = -3;
    public static final byte CRAFTING_ADD_INGREDIENT = -2;
    public static final byte NONE = -1;
    public static final byte INVENTORY = 0;
    public static final byte FIRST = 1;
    public static final byte LAST = 100;

    public static final byte OFFHAND = 119;
    public static final byte ARMOR = 120;

    @Deprecated
    public static final byte CREATIVE = 121;
    public static final byte HOTBAR = 122;
    public static final byte FIXED_INVENTORY = 123;
    public static final byte UI = 124;

}
