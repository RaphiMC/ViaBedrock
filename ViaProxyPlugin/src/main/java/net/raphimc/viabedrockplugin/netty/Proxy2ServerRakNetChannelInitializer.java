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
package net.raphimc.viabedrockplugin.netty;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import net.raphimc.netminecraft.constants.MCPipeline;
import net.raphimc.netminecraft.netty.connection.MinecraftChannelInitializer;
import net.raphimc.netminecraft.packet.registry.PacketRegistryUtil;
import net.raphimc.viabedrock.netty.BatchLengthCodec;
import net.raphimc.viabedrock.netty.PacketEncapsulationCodec;
import net.raphimc.viabedrock.protocol.BedrockBaseProtocol;
import net.raphimc.viaprotocolhack.netty.VPHEncodeHandler;
import net.raphimc.viaprotocolhack.netty.VPHPipeline;
import net.raphimc.viaproxy.protocolhack.impl.ViaProxyViaDecodeHandler;
import net.raphimc.viaproxy.proxy.ProxyConnection;

import java.util.function.Supplier;

public class Proxy2ServerRakNetChannelInitializer extends MinecraftChannelInitializer {

    public static final String DISCONNECT_HANDLER_NAME = "disconnect_handler";
    public static final String FRAME_ENCAPSULATION_HANDLER_NAME = "frame_encapsulation";
    public static final String PACKET_ENCAPSULATION_HANDLER_NAME = "packet_encapsulation";

    public static final ChannelHandler DUMMY_HANDLER = new ChannelHandlerAdapter() {
        @Override
        public boolean isSharable() {
            return true;
        }
    };

    public Proxy2ServerRakNetChannelInitializer(final Supplier<ChannelHandler> handlerSupplier) {
        super(handlerSupplier);
    }

    @Override
    protected void initChannel(Channel channel) {
        final UserConnection user = new UserConnectionImpl(channel, true);
        new ProtocolPipelineImpl(user);
        ProxyConnection.fromChannel(channel).setUserConnection(user);
        user.getProtocolInfo().getPipeline().add(BedrockBaseProtocol.INSTANCE);

        channel.pipeline().addLast(DISCONNECT_HANDLER_NAME, new DisconnectHandler());
        channel.pipeline().addLast(FRAME_ENCAPSULATION_HANDLER_NAME, new RakMessageEncapsulationCodec());
        channel.pipeline().addLast(MCPipeline.ENCRYPTION_HANDLER_NAME, DUMMY_HANDLER);
        channel.pipeline().addLast(MCPipeline.COMPRESSION_HANDLER_NAME, DUMMY_HANDLER);
        channel.pipeline().addLast(MCPipeline.SIZER_HANDLER_NAME, new BatchLengthCodec());
        channel.pipeline().addLast(PACKET_ENCAPSULATION_HANDLER_NAME, new PacketEncapsulationCodec());
        channel.pipeline().addLast(MCPipeline.PACKET_CODEC_HANDLER_NAME, MCPipeline.PACKET_CODEC_HANDLER.get());
        channel.pipeline().addLast(MCPipeline.HANDLER_HANDLER_NAME, this.handlerSupplier.get());

        channel.attr(MCPipeline.PACKET_REGISTRY_ATTRIBUTE_KEY).set(PacketRegistryUtil.getHandshakeRegistry(true));
        channel.pipeline().addBefore(MCPipeline.PACKET_CODEC_HANDLER_NAME, VPHPipeline.ENCODER_HANDLER_NAME, new VPHEncodeHandler(user));
        channel.pipeline().addBefore(MCPipeline.PACKET_CODEC_HANDLER_NAME, VPHPipeline.DECODER_HANDLER_NAME, new ViaProxyViaDecodeHandler(user));
    }

}
