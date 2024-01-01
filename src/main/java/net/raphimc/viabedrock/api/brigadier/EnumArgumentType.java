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
package net.raphimc.viabedrock.api.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class EnumArgumentType implements ArgumentType<Object> {

    private static final SimpleCommandExceptionType INVALID_ENUM_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Invalid enum"));

    private final Set<String> values;
    private final Set<String> completions;

    public EnumArgumentType(final Set<String> values, final Set<String> completions) {
        this.values = values;
        this.completions = completions;
    }

    public static EnumArgumentType values(final Set<String> values) {
        return new EnumArgumentType(values, values);
    }

    public static EnumArgumentType valuesAndCompletions(final Set<String> values, final Set<String> completions) {
        return new EnumArgumentType(values, completions);
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        final String s = reader.readUnquotedString();
        if (!this.values.contains(s)) {
            throw INVALID_ENUM_EXCEPTION.createWithContext(reader);
        }

        return null;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SuggestionsUtil.suggestMatching(this.completions, builder);
    }

}
