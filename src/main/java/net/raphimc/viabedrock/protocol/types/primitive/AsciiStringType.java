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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class AsciiStringType extends Type<String> {

    public AsciiStringType() {
        super(String.class);
    }

    @Override
    public String read(ByteBuf buffer) {
        return buffer.readString(buffer.readIntLE(), StandardCharsets.US_ASCII);
    }

    @Override
    public void write(ByteBuf buffer, String value) {
        buffer.writeIntLE(value.length());
        buffer.writeCharSequence(value, StandardCharsets.US_ASCII);
    }

}
