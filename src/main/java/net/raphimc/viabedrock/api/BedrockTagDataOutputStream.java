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

import com.google.common.io.LittleEndianDataOutputStream;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BedrockTagDataOutputStream extends FilterOutputStream implements DataOutput {

    public BedrockTagDataOutputStream(final OutputStream out) {
        super(new LittleEndianDataOutputStream(out));
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        ((DataOutput) this.out).writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        ((DataOutput) this.out).writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        ((DataOutput) this.out).writeShort(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        ((DataOutput) this.out).writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.writeUnsignedVarLong(((DataOutput) this.out), ((long) v << 1) ^ (v >> 31));
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.writeUnsignedVarLong(((DataOutput) this.out), (v << 1) ^ (v >> 63));
    }

    @Override
    public void writeFloat(float v) throws IOException {
        ((DataOutput) this.out).writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        ((DataOutput) this.out).writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        ((DataOutput) this.out).writeBytes(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        ((DataOutput) this.out).writeChars(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        final byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        this.writeUnsignedVarLong(((DataOutput) this.out), bytes.length);
        this.write(bytes);
    }

    private void writeUnsignedVarLong(final DataOutput out, long value) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.writeByte((byte) (value & 0x7F) | 0x80);
            value >>>= 7;
        }

        out.writeByte((int) value);
    }

}
