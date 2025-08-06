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
package net.raphimc.viabedrock.api.io.compression;

import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PacketCompressionAlgorithm;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZLibCompression implements CompressionAlgorithm {

    private final Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
    private final Inflater inflater = new Inflater(true);
    private final byte[] deflateBuffer = new byte[8192];
    private final byte[] inflateBuffer = new byte[8192];

    @Override
    public void compress(final ByteBuf in, final ByteBuf out) {
        final byte[] uncompressedData = new byte[in.readableBytes()];
        in.readBytes(uncompressedData);
        this.deflater.setInput(uncompressedData);
        this.deflater.finish();
        while (!this.deflater.finished()) {
            out.writeBytes(this.deflateBuffer, 0, this.deflater.deflate(this.deflateBuffer));
        }
        this.deflater.reset();
    }

    @Override
    public void decompress(final ByteBuf in, final ByteBuf out) throws Exception {
        final byte[] compressedData = new byte[in.readableBytes()];
        in.readBytes(compressedData);
        this.inflater.setInput(compressedData);
        while (!this.inflater.finished()) {
            out.writeBytes(this.inflateBuffer, 0, this.inflater.inflate(this.inflateBuffer));
        }
        this.inflater.reset();
    }

    @Override
    public void end() {
        this.inflater.end();
        this.deflater.end();
    }

    @Override
    public PacketCompressionAlgorithm getAlgorithm() {
        return PacketCompressionAlgorithm.ZLib;
    }

}
