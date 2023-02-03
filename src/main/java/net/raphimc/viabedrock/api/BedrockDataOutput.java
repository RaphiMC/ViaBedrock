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
package net.raphimc.viabedrock.api;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BedrockDataOutput implements DataOutput {

    private final DataOutput dataOutput;

    public BedrockDataOutput(DataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    @Override
    public void write(int i) throws IOException {
        dataOutput.write(i);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        dataOutput.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int offset, int len) throws IOException {
        dataOutput.write(bytes, offset, len);
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        dataOutput.writeBoolean(b);
    }

    @Override
    public void writeByte(int i) throws IOException {
        dataOutput.writeByte(i);
    }

    @Override
    public void writeShort(int i) throws IOException {
        dataOutput.writeShort(i);
    }

    @Override
    public void writeChar(int i) throws IOException {
        dataOutput.writeChar(i);
    }

    @Override
    public void writeInt(int i) throws IOException {
        final ByteBuf buf = Unpooled.buffer();
        BedrockTypes.VAR_INT.write(buf, i);
        dataOutput.write(ByteBufUtil.getBytes(buf));
    }

    @Override
    public void writeLong(long l) throws IOException {
        final ByteBuf buf = Unpooled.buffer();
        BedrockTypes.VAR_LONG.write(buf, l);
        dataOutput.write(ByteBufUtil.getBytes(buf));
    }

    @Override
    public void writeFloat(float v) throws IOException {
        dataOutput.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        dataOutput.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        dataOutput.writeBytes(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        dataOutput.writeChars(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        final byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        final ByteBuf buf = Unpooled.buffer();
        BedrockTypes.UNSIGNED_VAR_INT.write(buf, bytes.length);
        buf.writeBytes(bytes);
        dataOutput.write(ByteBufUtil.getBytes(buf));
    }
}
