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
package net.raphimc.viabedrock.protocol;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.protocol.StatelessTransitionProtocol;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.platform.ViaBedrockConfig;
import net.raphimc.viabedrock.protocol.data.BedrockMappingData;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayStatus;
import net.raphimc.viabedrock.protocol.packet.*;
import net.raphimc.viabedrock.protocol.provider.BlobCacheProvider;
import net.raphimc.viabedrock.protocol.provider.NettyPipelineProvider;
import net.raphimc.viabedrock.protocol.provider.ResourcePackProvider;
import net.raphimc.viabedrock.protocol.provider.SkinProvider;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.task.*;

import java.util.EnumSet;
import java.util.logging.Level;

public class BedrockProtocol extends StatelessTransitionProtocol<ClientboundBedrockPackets, ClientboundPackets1_21, ServerboundBedrockPackets, ServerboundPackets1_20_5> {

    public static final BedrockMappingData MAPPINGS = new BedrockMappingData();

    private static final EnumSet<ClientboundBedrockPackets> LOGIN_STATE_WHITELIST = EnumSet.of(
            ClientboundBedrockPackets.NETWORK_SETTINGS,
            ClientboundBedrockPackets.SERVER_TO_CLIENT_HANDSHAKE,
            ClientboundBedrockPackets.PLAY_STATUS,
            ClientboundBedrockPackets.DISCONNECT,
            ClientboundBedrockPackets.PACKET_VIOLATION_WARNING,
            ClientboundBedrockPackets.NETWORK_STACK_LATENCY,
            ClientboundBedrockPackets.START_GAME
    );

    private static final EnumSet<ClientboundBedrockPackets> BEFORE_PLAY_STATE_WHITELIST = EnumSet.of(
            ClientboundBedrockPackets.RESOURCE_PACKS_INFO,
            ClientboundBedrockPackets.RESOURCE_PACK_DATA_INFO,
            ClientboundBedrockPackets.RESOURCE_PACK_CHUNK_DATA,
            ClientboundBedrockPackets.RESOURCE_PACK_STACK,
            ClientboundBedrockPackets.BIOME_DEFINITION_LIST,
            ClientboundBedrockPackets.COMPRESSED_BIOME_DEFINITION_LIST,
            ClientboundBedrockPackets.DIMENSION_DATA,
            ClientboundBedrockPackets.AVAILABLE_COMMANDS
    );

    static {
        BEFORE_PLAY_STATE_WHITELIST.addAll(LOGIN_STATE_WHITELIST);
    }

    public BedrockProtocol() {
        super(ClientboundBedrockPackets.class, ClientboundPackets1_21.class, ServerboundBedrockPackets.class, ServerboundPackets1_20_5.class);
    }

    @Override
    protected void registerPackets() {
        StatusPackets.register(this);
        LoginPackets.register(this);
        ConfigurationPackets.register(this);
        PlayPackets.register(this);
        MultiStatePackets.register(this);
        ResourcePackPackets.register(this);
        JoinPackets.register(this);
        ChatPackets.register(this);
        ClientPlayerPackets.register(this);
        OtherPlayerPackets.register(this);
        WorldPackets.register(this);
        EntityPackets.register(this);
        HudPackets.register(this);
        InventoryPackets.register(this);

        // Fallback for unhandled packets (Temporary)

        for (ClientboundBedrockPackets packet : this.unmappedClientboundPacketType.getEnumConstants()) {
            if (!this.hasRegisteredClientbound(packet)) {
                this.cancelClientbound(packet);
            }
        }
        for (ServerboundPackets1_20_5 packet : this.unmappedServerboundPacketType.getEnumConstants()) {
            if (!this.hasRegisteredServerbound(packet)) {
                this.cancelServerbound(packet);
            }
        }
    }

    @Override
    public void register(ViaProviders providers) {
        providers.require(NettyPipelineProvider.class);
        providers.register(ResourcePackProvider.class, ViaBedrock.getConfig().getPackCacheMode().createProvider());
        providers.register(BlobCacheProvider.class, ViaBedrock.getConfig().getBlobCacheMode().createProvider());
        providers.register(SkinProvider.class, new SkinProvider());

        if (!ViaBedrock.getConfig().getBlobCacheMode().equals(ViaBedrockConfig.BlobCacheMode.DISABLED)) {
            providers.get(BlobCacheProvider.class).addBlob(0L, new byte[0]);
        }

        Via.getPlatform().runRepeatingSync(new KeepAliveTask(), 20L);
        Via.getPlatform().runRepeatingSync(new ChunkTrackerTickTask(), 2L);
        Via.getPlatform().runRepeatingSync(new BlobCacheTickTask(), 2L);
        Via.getPlatform().runRepeatingSync(new EntityTrackerTickTask(), 1L);
        Via.getPlatform().runRepeatingSync(new InventoryTrackerTickTask(), 1L);
    }

    @Override
    public void init(UserConnection user) {
        user.put(new ClientSettingsStorage("en_us", 12, 0, true, (short) 127, 1, false, true));
        user.put(new GameSessionStorage());
        user.put(new BlobCache(user));
        user.put(new PacketSyncStorage(user));
        user.put(new ChannelStorage());
        user.put(new PlayerListStorage());
        user.put(new ScoreboardTracker());
        user.put(new InventoryTracker(user));
    }

    @Override
    protected void registerConfigurationChangeHandlers() {
    }

    @Override
    public BedrockMappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper wrapper) throws InformativeException, CancelException {
        if (direction == Direction.CLIENTBOUND && state != State.STATUS) {
            State serverState = wrapper.user().getProtocolInfo().getServerState();
            final ClientboundBedrockPackets packet = ClientboundBedrockPackets.getPacket(wrapper.getId());
            if (packet == null) {
                final ByteBuf content = ((PacketWrapperImpl) wrapper).getInputBuffer();
                ViaBedrock.getPlatform().getLogger().warning("Received unknown packet " + wrapper.getId() + " in state " + serverState + " with content: " + ByteBufUtil.hexDump(content));
                throw CancelException.generate();
            }
            if (serverState == State.LOGIN && !LOGIN_STATE_WHITELIST.contains(packet)) { // Mojang client can skip the login state
                ViaBedrock.getPlatform().getLogger().warning("Server skipped LOGIN state");
                final PacketWrapper playStatus = PacketWrapper.create(ClientboundBedrockPackets.PLAY_STATUS, wrapper.user());
                playStatus.write(Types.INT, PlayStatus.LoginSuccess.getValue()); // status
                playStatus.send(BedrockProtocol.class, false);
                wrapper.user().getProtocolInfo().setServerState(State.CONFIGURATION);
                serverState = State.CONFIGURATION;
            }
            if (serverState != State.PLAY && !BEFORE_PLAY_STATE_WHITELIST.contains(packet)) { // Mojang client ignores most packets before receiving the START_GAME packet
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received packet " + packet + " outside PLAY state. Ignoring it.");
                throw CancelException.generate();
            }
        }

        /*if (direction == Direction.CLIENTBOUND) {
            System.out.println("PRE: direction = " + direction + ", state = " + state + ", packet=" + ClientboundBedrockPackets.getPacket(wrapper.getId()) + ", wrapper = " + wrapper);
        } else {
            System.out.println("PRE: direction = " + direction + ", state = " + state + ", packet=" + ServerboundPackets1_20_5.values()[wrapper.getId()] + ", wrapper = " + wrapper);
        }*/
        super.transform(direction, state, wrapper);
        /*if (direction == Direction.CLIENTBOUND) {
            System.out.println("POST: direction = " + direction + ", state = " + state + ", packet=" + ClientboundPackets1_21.values()[wrapper.getId()] + ", wrapper = " + wrapper);
        } else {
            System.out.println("POST: direction = " + direction + ", state = " + state + ", packet=" + ServerboundBedrockPackets.getPacket(wrapper.getId()) + ", wrapper = " + wrapper);
        }*/
    }

    public static void kickForIllegalState(final UserConnection user, final String reason) {
        kickForIllegalState(user, reason, null);
    }

    public static void kickForIllegalState(final UserConnection user, final String reason, final Throwable e) {
        ViaBedrock.getPlatform().getLogger().log(Level.SEVERE, "Illegal state: " + reason, e);

        final PacketType disconnectPacketType = switch (user.getProtocolInfo().getServerState()) {
            case LOGIN -> ClientboundLoginPackets.LOGIN_DISCONNECT;
            case CONFIGURATION -> ClientboundConfigurationPackets1_21.DISCONNECT;
            case PLAY -> ClientboundPackets1_21.DISCONNECT;
            default -> throw new IllegalStateException("Unexpected state: " + user.getProtocolInfo().getServerState());
        };
        try {
            final PacketWrapper disconnect = PacketWrapper.create(disconnectPacketType, user);
            PacketFactory.writeJavaDisconnect(disconnect, "§4ViaBedrock encountered an error:\n§c" + reason + "\n\n§rPlease report this issue on the ViaBedrock GitHub page.");
            disconnect.send(BedrockProtocol.class);
        } catch (Throwable ignored) {
        }

        if (user.getChannel() != null) {
            user.getChannel().flush();
            user.getChannel().close();
        }
    }

}
