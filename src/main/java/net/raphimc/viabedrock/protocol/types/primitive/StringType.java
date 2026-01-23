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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.nio.charset.StandardCharsets;

public class StringType extends Type<String> {

    public StringType() {
        super(String.class);
    }

    @Override
    public String read(ByteBuf buffer) {
        return new String(BedrockTypes.BYTE_ARRAY.read(buffer), StandardCharsets.UTF_8);
    }

    @Override
    public void write(ByteBuf buffer, String value) {
        BedrockTypes.BYTE_ARRAY.write(buffer, value.getBytes(StandardCharsets.UTF_8));
    }

    public static final class OptionalStringType extends OptionalType<String> {

        public OptionalStringType() {
            super(BedrockTypes.STRING);
        }

    }

}
