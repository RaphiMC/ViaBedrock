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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.ChannelHandlerContext;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.reflect.Enums;
import net.lenni0451.reflect.stream.RStream;
import net.raphimc.mcauth.step.bedrock.StepMCChain;
import net.raphimc.netminecraft.constants.ConnectionState;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.protocol.providers.NettyPipelineProvider;
import net.raphimc.viabedrock.protocol.storage.AuthChainData;
import net.raphimc.viabedrockplugin.provider.ViaProxyNettyPipelineProvider;
import net.raphimc.viaprotocolhack.util.VersionEnum;
import net.raphimc.viaproxy.cli.options.Options;
import net.raphimc.viaproxy.plugins.PluginManager;
import net.raphimc.viaproxy.plugins.ViaProxyPlugin;
import net.raphimc.viaproxy.plugins.events.*;
import net.raphimc.viaproxy.proxy.ProxyConnection;
import net.raphimc.viaproxy.proxy.client2proxy.Client2ProxyHandler;
import net.raphimc.viaproxy.proxy.proxy2server.Proxy2ServerHandler;
import net.raphimc.viaproxy.saves.impl.accounts.BedrockAccount;
import net.raphimc.viaproxy.util.logging.Logger;

import java.util.Map;

public class ViaBedrockPlugin extends ViaProxyPlugin {

    public static VersionEnum bedrock;

    @Override
    public void onEnable() {
        Logger.LOGGER.error("ViaBedrock is in very early stages of development and NOT intended for regular use yet!");
        Logger.LOGGER.error("Continue at your own risk!");

        Enums.addEnumInstance(VersionEnum.class, bedrock = Enums.newInstance(VersionEnum.class, "bedrock", 1_000_000, new Class[]{ProtocolVersion.class}, new Object[]{BedrockProtocolVersion.bedrockLatest}));
        VersionEnum.SORTED_VERSIONS.add(bedrock);
        RStream.of(VersionEnum.class).fields().by("VERSION_REGISTRY").<Map<ProtocolVersion, VersionEnum>>get().put(BedrockProtocolVersion.bedrockLatest, bedrock);

        PluginManager.EVENT_MANAGER.register(this);
    }

    @EventHandler
    public void onProtocolHackInit(final ProtocolHackInitEvent event) {
        event.registerPlatform(ViaBedrockPlatformImpl::new);
    }

    @EventHandler
    public void onViaLoading(final ViaLoadingEvent event) {
        Via.getManager().getProviders().use(NettyPipelineProvider.class, new ViaProxyNettyPipelineProvider());
    }

    @EventHandler
    public void onPreConnect(final PreConnectEvent event) {
        final ProxyConnection proxyConnection = ProxyConnection.fromChannel(event.getClientChannel());
        if (!Options.PROTOCOL_VERSION.equals(bedrock)) return;

        if (proxyConnection.getConnectionState() != ConnectionState.LOGIN) {
            event.setCancelMessage("§cStatus is not yet implemented for Bedrock Edition!");
        }

        if (event.getServerAddress().getPort() == 25565) { // Janky fix for the default port being hardcoded everywhere
            RStream.of(event.getServerAddress()).fields().by("port").set(19132);
        }
    }

    @EventHandler
    public void onClient2ProxyHandlerCreation(final Client2ProxyHandlerCreationEvent event) {
        event.setHandler(new Client2ProxyHandler() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
                if (!Options.PROTOCOL_VERSION.equals(bedrock)) return;

                final BedrockProxyConnection bedrockProxyConnection = new BedrockProxyConnection(() -> PluginManager.EVENT_MANAGER.call(new Proxy2ServerHandlerCreationEvent(new Proxy2ServerHandler())).getHandler(), Proxy2ServerRakNetChannelInitializer::new, ctx.channel());
                ctx.channel().attr(ProxyConnection.PROXY_CONNECTION_ATTRIBUTE_KEY).set(bedrockProxyConnection);
                RStream.of(this).withSuper().fields().by("proxyConnection").set(bedrockProxyConnection);
            }
        });
    }

    @EventHandler
    public void onFillPlayerData(final FillPlayerDataEvent event) {
        final UserConnection user = event.getProxyConnection().getUserConnection();

        if (Options.MC_ACCOUNT instanceof BedrockAccount) {
            final BedrockAccount bedrockAccount = (BedrockAccount) Options.MC_ACCOUNT;
            final StepMCChain.MCChain mcChain = bedrockAccount.getMcChain();
            user.put(new AuthChainData(user, mcChain.mojangJwt(), mcChain.identityJwt(), mcChain.publicKey(), mcChain.privateKey()));
        }
    }

}
