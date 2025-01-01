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
package net.raphimc.viabedrock.protocol.types.array;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ByteArrayType extends Type<byte[]> {

    private final int length;

    public ByteArrayType(final int length) {
        super(byte[].class);
        this.length = length;
    }

    public ByteArrayType() {
        super(byte[].class);
        this.length = -1;
    }

    @Override
    public byte[] read(ByteBuf buffer) {
        final int length = this.length == -1 ? BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer) : this.length;
        Preconditions.checkArgument(buffer.isReadable(length), "Length is larger than readable bytes: " + length + " > " + buffer.readableBytes());
        final byte[] array = new byte[length];
        buffer.readBytes(array);
        return array;
    }

    @Override
    public void write(ByteBuf buffer, byte[] value) {
        if (this.length != -1) {
            Preconditions.checkArgument(length == value.length, "Length does not match expected length");
        } else {
            BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.length);
        }
        buffer.writeBytes(value);
    }

}
