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
package net.raphimc.viabedrockplugin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import network.ycc.raknet.frame.FrameData;

import java.util.List;

public class FrameDataEncapsulationCodec extends MessageToMessageCodec<FrameData, ByteBuf> {

    private static final int PACKET_ID = 0xFE;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        final FrameData frame = FrameData.create(ctx.channel().alloc(), PACKET_ID, msg);
        frame.setReliability(FrameData.Reliability.RELIABLE_ORDERED);
        frame.setOrderChannel(0);
        out.add(frame);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FrameData msg, List<Object> out) {
        final ByteBuf buf = msg.createData();
        final int packetId = buf.readUnsignedByte();
        if (packetId != PACKET_ID) throw new IllegalStateException("Received invalid RakNet packet: " + packetId);
        out.add(buf.retain());
    }

}
