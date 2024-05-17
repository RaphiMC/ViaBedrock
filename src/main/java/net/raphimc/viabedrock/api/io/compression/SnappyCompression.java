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
package net.raphimc.viabedrock.api.io.compression;

import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.compression.Snappy;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PacketCompressionAlgorithm;

public class SnappyCompression implements CompressionAlgorithm {

    private final Snappy snappy = new Snappy();

    @Override
    public void compress(final ByteBuf in, final ByteBuf out) {
        if (in.readableBytes() <= Short.MAX_VALUE) {
            this.snappy.encode(in, out, in.readableBytes());
            this.snappy.reset();
        } else { // Netty's snappy implementation can't handle more than that (https://github.com/netty/netty/issues/13226)
            Types.VAR_INT.writePrimitive(out, in.readableBytes());

            int value = in.readableBytes() - 1;
            int highestOneBit = Integer.highestOneBit(value);
            int bitLength = 0;
            while ((highestOneBit >>= 1) != 0) {
                bitLength++;
            }
            int bytesToEncode = 1 + bitLength / 8;
            out.writeByte(59 + bytesToEncode << 2);
            for (int i = 0; i < bytesToEncode; i++) {
                out.writeByte(in.readableBytes() - 1 >> i * 8 & 0x0ff);
            }
            out.writeBytes(in);
        }
    }

    @Override
    public void decompress(final ByteBuf in, final ByteBuf out) {
        this.snappy.decode(in, out);
        this.snappy.reset();
    }

    @Override
    public PacketCompressionAlgorithm getAlgorithm() {
        return PacketCompressionAlgorithm.Snappy;
    }

}
