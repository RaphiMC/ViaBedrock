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
package net.raphimc.viabedrock.api.http;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.UUID;

public class ResourcePackHttpServer {

    private final InetSocketAddress bindAddress;
    private final ChannelFuture channelFuture;
    private final BiMap<UUID, UserConnection> connections = HashBiMap.create();

    public ResourcePackHttpServer(final InetSocketAddress bindAddress) {
        this.bindAddress = bindAddress;
        this.channelFuture = new ServerBootstrap()
                .group(new NioEventLoopGroup(0))
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        channel.pipeline().addLast("http_codec", new HttpServerCodec());
                        channel.pipeline().addLast("chunked_writer", new ChunkedWriteHandler());
                        channel.pipeline().addLast("http_handler", new SimpleChannelInboundHandler<Object>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                                if (msg instanceof HttpRequest) {
                                    final HttpRequest request = (HttpRequest) msg;
                                    if (!request.method().equals(HttpMethod.GET)) {
                                        ctx.close();
                                        return;
                                    }

                                    final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
                                    if (!queryStringDecoder.parameters().containsKey("token")) {
                                        ctx.close();
                                        return;
                                    }
                                    final UUID uuid = UUID.fromString(queryStringDecoder.parameters().get("token").get(0));
                                    final UserConnection user = ResourcePackHttpServer.this.connections.get(uuid);
                                    if (user == null) {
                                        ctx.close();
                                        return;
                                    }

                                    final ResourcePacksStorage resourcePacksStorage = user.get(ResourcePacksStorage.class);
                                    resourcePacksStorage.setHttpConsumer(data -> {
                                        if (!ctx.channel().isActive() || !ctx.channel().isOpen()) {
                                            throw new IllegalStateException("Channel is not open");
                                        }

                                        final DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                                        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
                                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
                                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, data.length);
                                        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                                        ctx.write(response);
                                        ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(new ByteArrayInputStream(data), 65535))).addListener(ChannelFutureListener.CLOSE);
                                    });
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                ctx.close();
                            }
                        });
                    }
                })
                .bind(bindAddress)
                .syncUninterruptibly();
    }

    public void addConnection(final UUID uuid, final UserConnection connection) {
        synchronized (this.connections) {
            this.connections.put(uuid, connection);
        }

        connection.getChannel().closeFuture().addListener(future -> {
            synchronized (this.connections) {
                this.connections.remove(uuid);
            }
        });
    }

    public void stop() {
        if (this.channelFuture != null) {
            this.channelFuture.channel().close();
        }
    }

    public String getUrl() {
        final InetSocketAddress bindAddress = (InetSocketAddress) this.channelFuture.channel().localAddress();
        return "http://" + this.bindAddress.getHostString() + ":" + bindAddress.getPort() + "/";
    }

    public Channel getChannel() {
        return this.channelFuture.channel();
    }

}
