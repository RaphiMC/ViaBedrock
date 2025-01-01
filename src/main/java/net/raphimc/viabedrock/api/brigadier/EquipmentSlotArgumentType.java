/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.api.brigadier;

import com.google.common.collect.Lists;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EquipmentSlotArgumentType implements ArgumentType<Object> {

    private static final List<String> SLOTS = Lists.newArrayList("slot.armor", "slot.armor.chest", "slot.armor.feet", "slot.armor.head", "slot.armor.legs", "slot.chest", "slot.enderchest", "slot.equippable", "slot.hotbar", "slot.inventory", "slot.saddle", "slot.weapon.mainhand", "slot.weapon.offhand");
    private static final SimpleCommandExceptionType INVALID_EQUIPMENT_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Invalid equipment slot"));

    public static EquipmentSlotArgumentType equipmentSlot() {
        return new EquipmentSlotArgumentType();
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        final String slot = reader.readUnquotedString();
        if (!SLOTS.contains(slot)) {
            throw INVALID_EQUIPMENT_EXCEPTION.create();
        }

        return null;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SuggestionsUtil.suggestMatching(SLOTS, builder);
    }

}
