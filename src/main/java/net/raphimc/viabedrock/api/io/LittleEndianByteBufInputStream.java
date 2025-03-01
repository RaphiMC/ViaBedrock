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
package net.raphimc.viabedrock.api.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LittleEndianByteBufInputStream extends ByteBufInputStream {

    private final ByteBuf buffer;

    public LittleEndianByteBufInputStream(final ByteBuf buffer) {
        super(buffer);

        this.buffer = buffer;
    }

    @Override
    public char readChar() {
        return Character.reverseBytes(this.buffer.readChar());
    }

    @Override
    public double readDouble() {
        return this.buffer.readDoubleLE();
    }

    @Override
    public float readFloat() {
        return this.buffer.readFloatLE();
    }

    @Override
    public short readShort() {
        return this.buffer.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return this.buffer.readUnsignedShortLE();
    }

    @Override
    public long readLong() throws IOException {
        return this.buffer.readLongLE();
    }

    @Override
    public int readInt() throws IOException {
        return this.buffer.readIntLE();
    }

    @Override
    public String readUTF() throws IOException {
        return (String) this.buffer.readCharSequence(this.readUnsignedShort(), StandardCharsets.UTF_8);
    }

}
