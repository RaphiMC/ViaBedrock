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
package net.raphimc.viabedrock.netty.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;

import java.net.InetSocketAddress;
import java.util.List;

public class DatagramCodec extends MessageToMessageCodec<DatagramPacket, ByteBuf> {

    private final InetSocketAddress remoteAddress;

    public DatagramCodec() {
        this(null);
    }

    public DatagramCodec(final InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        if (this.remoteAddress != null) {
            out.add(new DatagramPacket(in.retain(), this.remoteAddress));
        } else if (ctx.channel().remoteAddress() instanceof InetSocketAddress remoteAddress) {
            out.add(new DatagramPacket(in.retain(), remoteAddress));
        } else {
            throw new IllegalStateException("Channel remote address is not an InetSocketAddress and no remote address was specified");
        }
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final DatagramPacket in, final List<Object> out) {
        if (in.sender() == null || in.sender().equals(this.remoteAddress) || in.sender().equals(ctx.channel().remoteAddress())) {
            out.add(in.content().retain());
        }
    }

}
