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

import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.io.DataInput;
import java.io.IOException;

public class BedrockDataInput implements DataInput {

    private final DataInput dataInput;

    public BedrockDataInput(DataInput dataInput) {
        this.dataInput = dataInput;
    }

    @Override
    public void readFully(byte[] bytes) throws IOException {
        dataInput.readFully(bytes);
    }

    @Override
    public void readFully(byte[] bytes, int offset, int len) throws IOException {
        dataInput.readFully(bytes, offset, len);
    }

    @Override
    public int skipBytes(int i) throws IOException {
        return dataInput.skipBytes(i);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return dataInput.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return dataInput.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return dataInput.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return dataInput.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return dataInput.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return dataInput.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return BedrockTypes.VAR_INT.readPrimitive(new InputStreamByteBuf(dataInput));
    }

    @Override
    public long readLong() throws IOException {
        return BedrockTypes.VAR_LONG.readPrimitive(new InputStreamByteBuf(dataInput));
    }

    @Override
    public float readFloat() throws IOException {
        return dataInput.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return dataInput.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return dataInput.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        try {
            return BedrockTypes.STRING.read(new InputStreamByteBuf(dataInput));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
