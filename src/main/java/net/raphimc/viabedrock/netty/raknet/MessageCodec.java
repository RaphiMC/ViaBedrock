/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.netty.raknet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.raphimc.viabedrock.ViaBedrock;
import org.cloudburstmc.netty.channel.raknet.RakReliability;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;

import java.util.List;

public class MessageCodec extends MessageToMessageCodec<RakMessage, ByteBuf> {

    public static final String NAME = "viabedrock-raknet-message-codec";

    private static final int MINECRAFT_MESSAGE_ID = 0xFE;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        final CompositeByteBuf buf = ctx.alloc().compositeBuffer(2);
        try {
            buf.addComponent(true, ctx.alloc().ioBuffer(1).writeByte(MINECRAFT_MESSAGE_ID));
            buf.addComponent(true, in.retainedSlice());
            out.add(new RakMessage(buf.retain()));
        } finally {
            buf.release();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, RakMessage in, List<Object> out) {
        if (in.channel() != 0 && in.reliability() != RakReliability.RELIABLE_ORDERED) {
            return;
        }
        final ByteBuf buf = in.content();
        if (!buf.isReadable()) {
            return;
        }
        final int messageId = buf.readUnsignedByte();
        if (messageId != MINECRAFT_MESSAGE_ID) { // Mojang client seems to ignore invalid messages
            ViaBedrock.getPlatform().getLogger().warning("Received invalid RakNet message id: " + messageId);
            return;
        }
        out.add(buf.readRetainedSlice(buf.readableBytes()));
    }

}
