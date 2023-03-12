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
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.io.IOException;

public class NetworkByteBufInputStream extends LittleEndianByteBufInputStream {

    private final ByteBuf buffer;

    public NetworkByteBufInputStream(final ByteBuf buffer) {
        super(buffer);

        this.buffer = buffer;
    }

    @Override
    public int readInt() throws IOException {
        try {
            return BedrockTypes.VAR_INT.read(this.buffer);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public long readLong() throws IOException {
        try {
            return BedrockTypes.VAR_LONG.read(this.buffer);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public String readUTF() throws IOException {
        try {
            return BedrockTypes.STRING.read(this.buffer);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
