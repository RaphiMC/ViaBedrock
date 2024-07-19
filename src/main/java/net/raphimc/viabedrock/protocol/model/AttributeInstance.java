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
package net.raphimc.viabedrock.protocol.model;

import net.raphimc.viabedrock.protocol.data.enums.bedrock.AttributeModifierOperation;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.AttributeOperands;

public record AttributeInstance(String name, float currentValue, float minValue, float maxValue, float defaultValue, Modifier[] modifiers) {

    public AttributeInstance(final String name, final float currentValue, final float minValue, final float maxValue) {
        this(name, currentValue, minValue, maxValue, currentValue, new Modifier[0]);
    }

    public record Modifier(String id, String name, float amount, AttributeModifierOperation operation, AttributeOperands operand, boolean isSerializable) {
    }

}
