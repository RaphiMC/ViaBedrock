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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;

import java.nio.charset.StandardCharsets;

public class AsciiStringType extends Type<AsciiString> implements TypeConverter<AsciiString> {

    public AsciiStringType() {
        super(AsciiString.class);
    }

    @Override
    public AsciiString read(ByteBuf buffer) throws Exception {
        return AsciiString.of(buffer.readCharSequence(buffer.readIntLE(), StandardCharsets.US_ASCII));
    }

    @Override
    public void write(ByteBuf buffer, AsciiString value) throws Exception {
        buffer.writeIntLE(value.length());
        buffer.writeCharSequence(value, StandardCharsets.US_ASCII);
    }

    @Override
    public AsciiString from(Object o) {
        return AsciiString.of(o.toString());
    }

}
