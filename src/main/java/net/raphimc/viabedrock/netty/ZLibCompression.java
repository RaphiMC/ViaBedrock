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
package net.raphimc.viabedrock.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZLibCompression extends ByteToMessageCodec<ByteBuf> {

    private final byte[] deflateBuffer = new byte[8192];
    private final byte[] inflateBuffer = new byte[8192];

    private Deflater deflater;
    private Inflater inflater;

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        if (this.inflater != null) this.inflater.end();
        if (this.deflater != null) this.deflater.end();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        if (this.deflater == null) this.deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);

        if (in.isReadable()) {
            final byte[] uncompressedData = new byte[in.readableBytes()];
            in.readBytes(uncompressedData);
            this.deflater.setInput(uncompressedData);
            this.deflater.finish();
            while (!this.deflater.finished()) {
                out.writeBytes(this.deflateBuffer, 0, this.deflater.deflate(this.deflateBuffer));
            }
            this.deflater.reset();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (this.inflater == null) this.inflater = new Inflater(true);

        if (in.isReadable()) {
            final byte[] compressedData = new byte[in.readableBytes()];
            in.readBytes(compressedData);
            this.inflater.setInput(compressedData);
            final ByteBuf uncompressedData = ctx.alloc().buffer();
            while (!this.inflater.finished()) {
                uncompressedData.writeBytes(this.inflateBuffer, 0, this.inflater.inflate(this.inflateBuffer));
            }
            this.inflater.reset();
            out.add(uncompressedData);
        }
    }

}
