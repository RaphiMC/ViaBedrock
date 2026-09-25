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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

public class TargetArgumentType implements ArgumentType<Object> {

    private static final SimpleCommandExceptionType INVALID_TARGET_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Invalid target"));

    public static TargetArgumentType target() {
        return new TargetArgumentType();
    }

    @Override
    public Object parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        int brackets = 0;
        boolean quoted = false;
        boolean escaped = false;

        while (reader.canRead()) {
            final char character = reader.peek();
            if (!quoted && brackets == 0 && character == ' ') {
                break;
            }

            reader.skip();
            if (escaped) {
                escaped = false;
            } else if (quoted && character == '\\') {
                escaped = true;
            } else if (character == '"') {
                quoted = !quoted;
            } else if (!quoted && character == '[') {
                brackets++;
            } else if (!quoted && character == ']' && brackets > 0) {
                brackets--;
            }
        }

        if (reader.getCursor() == start || brackets != 0 || quoted) {
            throw INVALID_TARGET_EXCEPTION.createWithContext(reader);
        }
        return reader.getString().substring(start, reader.getCursor());
    }

}
