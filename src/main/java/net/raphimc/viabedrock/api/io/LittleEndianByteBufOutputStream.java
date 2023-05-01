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
package net.raphimc.viabedrock.api.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LittleEndianByteBufOutputStream extends ByteBufOutputStream {

    private final ByteBuf buffer;

    public LittleEndianByteBufOutputStream(final ByteBuf buffer) {
        super(buffer);

        this.buffer = buffer;
    }

    @Override
    public void writeChar(int v) {
        this.buffer.writeChar(Character.reverseBytes((char) v));
    }

    @Override
    public void writeDouble(double v) {
        this.buffer.writeDoubleLE(v);
    }

    @Override
    public void writeFloat(float v) {
        this.buffer.writeFloatLE(v);
    }

    @Override
    public void writeShort(int v) {
        this.buffer.writeShortLE(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.buffer.writeLongLE(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.buffer.writeIntLE(v);
    }

    @Override
    public void writeUTF(String v) throws IOException {
        this.writeShort(ByteBufUtil.utf8Bytes(v));
        this.buffer.writeCharSequence(v, StandardCharsets.UTF_8);
    }

}
