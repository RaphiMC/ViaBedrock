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
package net.raphimc.viabedrock.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.io.compression.CompressionAlgorithm;
import net.raphimc.viabedrock.api.io.compression.NoopCompression;
import net.raphimc.viabedrock.api.io.compression.ProtocolCompression;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PacketCompressionAlgorithm;

import java.util.List;
import java.util.logging.Level;

public class CompressionCodec extends ByteToMessageCodec<ByteBuf> {

    private final ProtocolCompression protocolCompression;

    public CompressionCodec(final ProtocolCompression protocolCompression) {
        this.protocolCompression = protocolCompression;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        this.protocolCompression.end();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        if (!in.isReadable()) {
            return;
        }

        final int inputSize = in.readableBytes();
        final CompressionAlgorithm compressionAlgorithm = this.protocolCompression.getCompressionAlgorithmForSize(inputSize);
        if (compressionAlgorithm instanceof NoopCompression) {
            out.writeByte(PacketCompressionAlgorithm.None.getValue());
            out.writeBytes(in);
            return;
        }
        final ByteBuf compressedData = ctx.alloc().buffer();
        in.markReaderIndex();
        compressionAlgorithm.compress(in, compressedData);
        if (compressedData.readableBytes() < inputSize) {
            out.writeByte(compressionAlgorithm.getAlgorithm().getValue());
            out.writeBytes(compressedData);
        } else {
            in.resetReaderIndex();
            out.writeByte(PacketCompressionAlgorithm.None.getValue());
            out.writeBytes(in);
        }
        compressedData.release();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable()) {
            return;
        }

        final PacketCompressionAlgorithm algorithm = PacketCompressionAlgorithm.getByValue(in.readUnsignedByte());
        if (algorithm == null) { // Bedrock client just drops the packet if it doesn't know the algorithm
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received unknown compression algorithm. Dropping packet.");
            return;
        }

        final CompressionAlgorithm compressionAlgorithm = this.protocolCompression.getCompressionAlgorithm(algorithm);
        if (compressionAlgorithm instanceof NoopCompression) {
            out.add(in.retain());
            return;
        }
        final ByteBuf uncompressedData = ctx.alloc().buffer();
        compressionAlgorithm.decompress(in, uncompressedData); // Bedrock client would drop packets with invalid data, but this would be too insane to do
        out.add(uncompressedData);
    }

}
