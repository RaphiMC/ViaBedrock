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

import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.AttributeModifierOperation;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.AttributeOperands;

import java.util.HashSet;
import java.util.Set;

public record EntityAttribute(String name, float currentValue, float minValue, float maxValue, float defaultValue, Modifier[] modifiers) {

    public EntityAttribute(final String name, final float currentValue, final float minValue, final float maxValue) {
        this(name, currentValue, minValue, maxValue, currentValue, new Modifier[0]);
    }

    public EntityAttribute withValue(final float value) {
        return new EntityAttribute(this.name, value, this.minValue, this.maxValue, this.defaultValue, this.modifiers);
    }

    public float computeValue(final boolean computeCurrentValue) {
        final float currentValue = computeCurrentValue ? this.computeCurrentValue() : this.currentValue;
        final float minValue = this.computeMinValue();
        final float maxValue = this.computeMaxValue();
        return MathUtil.clamp(currentValue, minValue, maxValue);
    }

    public float computeCurrentValue() {
        return this.applyModifiers(this.currentValue, AttributeOperands.OPERAND_CURRENT);
    }

    public float computeMinValue() {
        return Math.max(this.applyModifiers(this.minValue, AttributeOperands.OPERAND_MIN), this.minValue);
    }

    public float computeMaxValue() {
        return Math.min(this.applyModifiers(this.maxValue, AttributeOperands.OPERAND_MAX), this.maxValue);
    }

    private float applyModifiers(final float value, final AttributeOperands operand) {
        if (this.modifiers.length == 0) return value;

        float result = value;
        final Set<String> applied = new HashSet<>();
        for (Modifier modifier : this.modifiers) {
            if (modifier.operation == AttributeModifierOperation.OPERATION_ADDITION && modifier.operand == operand && !applied.contains(modifier.name)) {
                result += modifier.amount;
                applied.add(modifier.name);
            }
        }
        applied.clear();
        final float base = result;
        for (Modifier modifier : this.modifiers) {
            if (modifier.operation == AttributeModifierOperation.OPERATION_MULTIPLY_BASE && modifier.operand == operand && !applied.contains(modifier.name)) {
                result += base * modifier.amount;
                applied.add(modifier.name);
            }
        }
        applied.clear();
        for (Modifier modifier : this.modifiers) {
            if (modifier.operation == AttributeModifierOperation.OPERATION_MULTIPLY_TOTAL && modifier.operand == operand && !applied.contains(modifier.name)) {
                result *= 1F + modifier.amount;
                applied.add(modifier.name);
            }
        }
        applied.clear();
        for (Modifier modifier : this.modifiers) {
            if (modifier.operation == AttributeModifierOperation.OPERATION_CAP && modifier.operand == operand && !applied.contains(modifier.name)) {
                result = Math.min(result, modifier.amount);
                applied.add(modifier.name);
            }
        }
        return result;
    }

    public record Modifier(String id, String name, float amount, AttributeModifierOperation operation, AttributeOperands operand, boolean isSerializable) {
    }

}
