/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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

import java.util.concurrent.CompletableFuture;

public class IntegerRangeArgumentType implements ArgumentType<Object> {

    private static final SimpleCommandExceptionType INVALID_INTEGER_RANGE_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Invalid integer range"));

    public static IntegerRangeArgumentType integerRange() {
        return new IntegerRangeArgumentType();
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead() || reader.peek() == ' ') throw INVALID_INTEGER_RANGE_EXCEPTION.createWithContext(reader);
        if (reader.peek() == '!') reader.skip();
        if (reader.canRead(2) && reader.peek() == '.' && reader.peek(1) == '.') {
            reader.skip();
            reader.skip();
        } else {
            this.readInt(reader);
            if (!reader.canRead() || reader.peek() == ' ') return null;
            if (!reader.canRead(2)) throw INVALID_INTEGER_RANGE_EXCEPTION.createWithContext(reader);
            for (int i = 0; i < 2; i++) {
                if (reader.read() != '.') throw INVALID_INTEGER_RANGE_EXCEPTION.createWithContext(reader);
            }
        }
        if (!reader.canRead() || reader.peek() == ' ') return null;
        this.readInt(reader);
        return null;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        if (!reader.canRead()) builder.suggest("!");
        return builder.buildFuture();
    }

    private int readInt(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        while (reader.canRead() && this.isAllowedNumber(reader.peek())) reader.skip();
        final String number = reader.getString().substring(start, reader.getCursor());
        if (number.isEmpty()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedInt().createWithContext(reader);
        try {
            return Integer.parseInt(number);
        } catch (final NumberFormatException ex) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(reader, number);
        }
    }

    private boolean isAllowedNumber(final char c) {
        return c >= '0' && c <= '9' || c == '-' || c == '+';
    }

}
