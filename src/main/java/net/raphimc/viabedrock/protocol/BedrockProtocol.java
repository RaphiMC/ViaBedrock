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
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import net.raphimc.viabedrock.api.JsonUtil;
import net.raphimc.viabedrock.protocol.data.BedrockMappingData;
import net.raphimc.viabedrock.protocol.packets.*;
import net.raphimc.viabedrock.protocol.providers.NettyPipelineProvider;
import net.raphimc.viabedrock.protocol.storage.ChatSettingsStorage;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.SpawnPositionStorage;
import net.raphimc.viabedrock.protocol.task.EntityTrackerTickTask;

public class BedrockProtocol extends AbstractProtocol<ClientboundBedrockPackets, ClientboundPackets1_19_3, ServerboundBedrockPackets, ServerboundPackets1_19_3> {

    public static final BedrockMappingData MAPPINGS = new BedrockMappingData();

    public BedrockProtocol() {
        super(ClientboundBedrockPackets.class, ClientboundPackets1_19_3.class, ServerboundBedrockPackets.class, ServerboundPackets1_19_3.class);
    }

    @Override
    protected void registerPackets() {
        LoginPackets.register(this);
        PlayPackets.register(this);
        ResourcePackPackets.register(this);
        JoinPackets.register(this);
        ChatPackets.register(this);
        PlayerPackets.register(this);
        WorldPackets.register(this);

        // Fallback for unhandled packets

        for (ClientboundBedrockPackets packet : this.oldClientboundPacketEnum.getEnumConstants()) {
            if (!this.hasRegisteredClientbound(State.LOGIN, packet.getId())) {
                this.registerClientbound(State.LOGIN, packet.getId(), ClientboundLoginPackets.LOGIN_DISCONNECT.getId(), new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        handler(wrapper -> {
                            wrapper.clearPacket();
                            wrapper.write(Type.COMPONENT, JsonUtil.textToComponent("§cReceived unhandled packet: " + packet.name() + " in state LOGIN\n\n§cPlease report this issue on the ViaBedrock GitHub page!"));
                        });
                    }
                });
            }
        }
        for (ClientboundBedrockPackets packet : this.oldClientboundPacketEnum.getEnumConstants()) {
            if (!this.hasRegisteredClientbound(packet)) {
                this.cancelClientbound(packet);
            }
        }
        for (ServerboundPackets1_19_3 packet : this.newServerboundPacketEnum.getEnumConstants()) {
            if (!this.hasRegisteredServerbound(packet)) {
                this.cancelServerbound(packet);
            }
        }
    }

    @Override
    public void register(ViaProviders providers) {
        providers.require(NettyPipelineProvider.class);

        Via.getPlatform().runRepeatingSync(new EntityTrackerTickTask(), 1L);
    }

    @Override
    public void init(UserConnection user) {
        user.put(new ChatSettingsStorage(user));
        user.put(new SpawnPositionStorage(user));
        user.put(new EntityTracker(user));
    }

    @Override
    public BedrockMappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
        if (direction == Direction.CLIENTBOUND) {
            System.out.println("PRE: direction = " + direction + ", state = " + state + ", packet=" + ClientboundBedrockPackets.values()[packetWrapper.getId() - 1] + ", packetWrapper = " + packetWrapper);
        } else {
            System.out.println("PRE: direction = " + direction + ", state = " + state + ", packet=" + ServerboundPackets1_19_3.values()[packetWrapper.getId()] + ", packetWrapper = " + packetWrapper);
        }
        super.transform(direction, state, packetWrapper);
        if (direction == Direction.CLIENTBOUND) {
            System.out.println("POST: direction = " + direction + ", state = " + state + ", packet=" + ClientboundPackets1_19_3.values()[packetWrapper.getId()] + ", packetWrapper = " + packetWrapper);
        } else {
            System.out.println("POST: direction = " + direction + ", state = " + state + ", packet=" + ServerboundBedrockPackets.values()[packetWrapper.getId() - 1] + ", packetWrapper = " + packetWrapper);
        }
    }

}
