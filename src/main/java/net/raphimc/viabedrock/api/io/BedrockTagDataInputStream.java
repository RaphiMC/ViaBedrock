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

import com.google.common.io.LittleEndianDataInputStream;

import java.io.DataInput;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BedrockTagDataInputStream extends FilterInputStream implements DataInput {

    public BedrockTagDataInputStream(final InputStream in) {
        super(new LittleEndianDataInputStream(in));
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        ((DataInput) this.in).readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        ((DataInput) this.in).readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return ((DataInput) this.in).skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return ((DataInput) this.in).readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return ((DataInput) this.in).readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return ((DataInput) this.in).readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return ((DataInput) this.in).readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return ((DataInput) this.in).readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return ((DataInput) this.in).readChar();
    }

    @Override
    public int readInt() throws IOException {
        final int v = (int) this.readUnsignedVarLong((DataInput) this.in);
        return (v >>> 1) ^ -(v & 1);
    }

    @Override
    public long readLong() throws IOException {
        final long v = this.readUnsignedVarLong((DataInput) this.in);
        return (v >>> 1) ^ -(v & 1);
    }

    @Override
    public float readFloat() throws IOException {
        return ((DataInput) this.in).readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return ((DataInput) this.in).readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return ((DataInput) this.in).readLine();
    }

    @Override
    public String readUTF() throws IOException {
        final int length = (int) readUnsignedVarLong((DataInput) this.in);
        final byte[] bytes = new byte[length];
        this.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private long readUnsignedVarLong(final DataInput in) throws IOException {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = in.readByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }

        throw new RuntimeException("VarInt was too large");
    }

}
