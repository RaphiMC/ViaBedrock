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
package net.raphimc.viabedrock.protocol;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.mapping.PacketMappings;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.protocol.data.BedrockMappingData;
import net.raphimc.viabedrock.protocol.packetmapping.ClientboundPacketMappings;
import net.raphimc.viabedrock.protocol.packets.*;
import net.raphimc.viabedrock.protocol.providers.BlobCacheProvider;
import net.raphimc.viabedrock.protocol.providers.NettyPipelineProvider;
import net.raphimc.viabedrock.protocol.providers.SkinProvider;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.task.BlobCacheTickTask;
import net.raphimc.viabedrock.protocol.task.ChunkTrackerTickTask;
import net.raphimc.viabedrock.protocol.task.EntityTrackerTickTask;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.logging.Level;

public class BedrockProtocol extends AbstractProtocol<ClientboundBedrockPackets, ClientboundPackets1_19_3, ServerboundBedrockPackets, ServerboundPackets1_19_3> {

    public static final BedrockMappingData MAPPINGS = new BedrockMappingData();

    public BedrockProtocol() {
        super(ClientboundBedrockPackets.class, ClientboundPackets1_19_3.class, ServerboundBedrockPackets.class, ServerboundPackets1_19_3.class);
    }

    @Override
    protected void registerPackets() {
        StatusPackets.register(this);
        LoginPackets.register(this);
        PlayPackets.register(this);
        ResourcePackPackets.register(this);
        JoinPackets.register(this);
        ChatPackets.register(this);
        ClientPlayerPackets.register(this);
        OtherPlayerPackets.register(this);
        WorldPackets.register(this);
        EntityPackets.register(this);

        this.registerClientbound(ClientboundBedrockPackets.PACKET_VIOLATION_WARNING, ClientboundPackets1_19_3.DISCONNECT, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final int type = wrapper.read(BedrockTypes.VAR_INT) + 1; // type
                    final int severity = wrapper.read(BedrockTypes.VAR_INT) + 1; // severity
                    final int packetIdCause = wrapper.read(BedrockTypes.VAR_INT) - 1; // cause packet id
                    final String context = wrapper.read(BedrockTypes.STRING); // context

                    final String[] types = new String[]{"Unknown", "Malformed packet"};
                    final String[] severities = new String[]{"Unknown", "Warning", "Final warning", "Terminating connection"};

                    final String reason = "§4Packet violation warning: §c"
                            + (type >= 0 && type <= types.length ? types[type] : type)
                            + " (" + (severity >= 0 && severity <= severities.length ? severities[severity] : severity) + ")\n"
                            + "Violating Packet: " + (packetIdCause >= 0 && packetIdCause <= ClientboundBedrockPackets.values().length ? ClientboundBedrockPackets.values()[packetIdCause].name() : packetIdCause) + "\n"
                            + (context.isEmpty() ? "No context provided" : (" Context: '" + context + "'"))
                            + "\n\nPlease report this issue on the ViaBedrock GitHub page!";
                    wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(reason));
                });
            }
        });

        // Fallback for unhandled packets (Temporary)

        for (ClientboundBedrockPackets packet : this.unmappedClientboundPacketType.getEnumConstants()) {
            if (!this.hasRegisteredClientbound(packet)) {
                this.cancelClientbound(packet);
            }
        }
        for (ServerboundPackets1_19_3 packet : this.unmappedServerboundPacketType.getEnumConstants()) {
            if (!this.hasRegisteredServerbound(packet)) {
                this.cancelServerbound(packet);
            }
        }
    }

    @Override
    public void register(ViaProviders providers) {
        providers.require(NettyPipelineProvider.class);
        providers.register(BlobCacheProvider.class, new BlobCacheProvider());
        providers.register(SkinProvider.class, new SkinProvider());

        Via.getPlatform().runRepeatingSync(new ChunkTrackerTickTask(), 5L);
        Via.getPlatform().runRepeatingSync(new EntityTrackerTickTask(), 1L);
        Via.getPlatform().runRepeatingSync(new BlobCacheTickTask(), 1L);
    }

    @Override
    public void init(UserConnection user) {
        user.put(new ResourcePacksStorage(user));
        user.put(new SpawnPositionStorage(user));
        user.put(new BlobCache(user));
        user.put(new PacketSyncStorage(user));
        user.put(new ChannelStorage(user));
    }

    @Override
    public BedrockMappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    protected PacketMappings createClientboundPacketMappings() {
        return new ClientboundPacketMappings();
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        /*if (direction == Direction.CLIENTBOUND) {
            System.out.println("PRE: direction = " + direction + ", state = " + state + ", packet=" + ClientboundBedrockPackets.values()[packetWrapper.getId() - 1] + ", packetWrapper = " + packetWrapper);
        } else {
            System.out.println("PRE: direction = " + direction + ", state = " + state + ", packet=" + ServerboundPackets1_19_3.values()[packetWrapper.getId()] + ", packetWrapper = " + packetWrapper);
        }*/
        super.transform(direction, state, packetWrapper);
        /*if (direction == Direction.CLIENTBOUND) {
            System.out.println("POST: direction = " + direction + ", state = " + state + ", packet=" + ClientboundPackets1_19_3.values()[packetWrapper.getId()] + ", packetWrapper = " + packetWrapper);
        } else {
            System.out.println("POST: direction = " + direction + ", state = " + state + ", packet=" + ServerboundBedrockPackets.values()[packetWrapper.getId() - 1] + ", packetWrapper = " + packetWrapper);
        }*/
    }

    public static void kickForIllegalState(final UserConnection user, final String reason) {
        kickForIllegalState(user, reason, null);
    }

    public static void kickForIllegalState(final UserConnection user, final String reason, final Throwable e) {
        ViaBedrock.getPlatform().getLogger().log(Level.SEVERE, "Illegal state: " + reason, e);
        try {
            final PacketWrapper disconnect = PacketWrapper.create(user.getProtocolInfo().getState() == State.PLAY ? ClientboundPackets1_19_3.DISCONNECT : ClientboundLoginPackets.LOGIN_DISCONNECT, user);
            disconnect.write(Type.COMPONENT, JsonUtil.textToComponent("§4ViaBedrock encountered an error:\n§c" + reason + "\n\n§rPlease report this issue on the ViaBedrock GitHub page."));
            disconnect.send(BedrockProtocol.class);
        } catch (Throwable ignored) {
        }

        if (user.getChannel() != null) {
            user.getChannel().flush();
            user.getChannel().close();
        }
    }

}
