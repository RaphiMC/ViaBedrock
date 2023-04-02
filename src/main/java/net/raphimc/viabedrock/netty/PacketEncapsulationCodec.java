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

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.List;

public class PacketEncapsulationCodec extends ByteToMessageCodec<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        final int packetId = Type.VAR_INT.readPrimitive(in);
        final int header = packetId & 1023;
        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(out, header);
        out.writeBytes(in);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        final int header = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(in);
        final int packetId = header & 1023;
        final int senderId = (header >>> 10) & 3;
        final int clientId = (header >>> 12) & 3;

        if (senderId != 0) {
            throw new UnsupportedOperationException("Sender ID " + senderId + " is not supported");
        }
        if (clientId != 0) {
            throw new UnsupportedOperationException("Client ID " + clientId + " is not supported");
        }

        final ByteBuf packet = ctx.alloc().buffer();
        Type.VAR_INT.writePrimitive(packet, packetId);
        packet.writeBytes(in);
        out.add(packet);
    }

}
