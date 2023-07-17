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

public class PositionArgumentType implements ArgumentType<Object> {

    private static final SimpleCommandExceptionType INVALID_BLOCK_POSITION_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Invalid block position"));

    public static PositionArgumentType position() {
        return new PositionArgumentType();
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        boolean hasHat = this.readCoordinate(reader, false);
        if (reader.canRead()) reader.skip();
        boolean hasHat2 = this.readCoordinate(reader, hasHat);
        if (hasHat != hasHat2) throw INVALID_BLOCK_POSITION_EXCEPTION.createWithContext(reader);
        if (reader.canRead()) reader.skip();
        boolean hasHat3 = this.readCoordinate(reader, hasHat);
        if (hasHat2 != hasHat3) throw INVALID_BLOCK_POSITION_EXCEPTION.createWithContext(reader);
        return null;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        if (!reader.canRead()) {
            builder.suggest("~ ~ ~");
            builder.suggest("^ ^ ^");
        }
        return builder.buildFuture();
    }

    private boolean readCoordinate(final StringReader reader, final boolean requiresHat) throws CommandSyntaxException {
        if (!reader.canRead()) throw INVALID_BLOCK_POSITION_EXCEPTION.createWithContext(reader);
        boolean hasHat = false;
        boolean hasWave = false;
        if (reader.peek() == '^') {
            reader.skip();
            hasHat = true;
        } else if (requiresHat) {
            throw INVALID_BLOCK_POSITION_EXCEPTION.createWithContext(reader);
        } else if (reader.peek() == '~') {
            reader.skip();
            hasWave = true;
        }
        if (hasWave) {
            if (!reader.canRead() || reader.peek() == ' ') return hasHat;
        }
        reader.readDouble();
        if (reader.canRead() && reader.peek() != ' ') throw INVALID_BLOCK_POSITION_EXCEPTION.createWithContext(reader);
        return hasHat;
    }

}
