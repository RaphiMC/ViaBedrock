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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import net.raphimc.netminecraft.constants.MCPipeline;
import net.raphimc.netminecraft.util.LazyLoadBase;
import net.raphimc.viabedrock.netty.ZLibCompression;
import net.raphimc.viaproxy.proxy.ProxyConnection;
import network.ycc.raknet.RakNet;
import network.ycc.raknet.client.RakNetClient;

import java.util.function.Function;
import java.util.function.Supplier;

public class BedrockProxyConnection extends ProxyConnection {

    public BedrockProxyConnection(Supplier<ChannelHandler> handlerSupplier, Function<Supplier<ChannelHandler>, ChannelInitializer<Channel>> channelInitializerSupplier, Channel c2p) {
        super(handlerSupplier, channelInitializerSupplier, c2p);
    }

    @Override
    public void initialize(Bootstrap bootstrap) {
        bootstrap
                .group(LazyLoadBase.CLIENT_NIO_EVENTLOOP.getValue())
                .channel(RakNetClient.CHANNEL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4_000)
                .option(ChannelOption.IP_TOS, 0x18)
                .option(RakNet.PROTOCOL_VERSION, 11)
                .attr(ProxyConnection.PROXY_CONNECTION_ATTRIBUTE_KEY, this)
                .handler(this.channelInitializerSupplier.apply(this.handlerSupplier));

        this.channelFuture = bootstrap.register().syncUninterruptibly();
    }

    public void enableZLibCompression() {
        this.getChannel().pipeline().addBefore(MCPipeline.SIZER_HANDLER_NAME, MCPipeline.COMPRESSION_HANDLER_NAME, new ZLibCompression());
    }

}
