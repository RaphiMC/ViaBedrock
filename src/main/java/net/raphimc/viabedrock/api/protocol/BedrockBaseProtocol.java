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
package net.raphimc.viabedrock.api.protocol;

import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.protocol.AbstractSimpleProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ServerboundHandshakePackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.protocol.storage.HandshakeStorage;

public class BedrockBaseProtocol extends AbstractSimpleProtocol {

    public static final BedrockBaseProtocol INSTANCE = new BedrockBaseProtocol();

    private BedrockBaseProtocol() {
        this.initialize();
    }

    @Override
    protected void registerPackets() {
        this.registerServerbound(State.HANDSHAKE, ServerboundHandshakePackets.CLIENT_INTENTION.getId(), -1, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    final int protocolVersion = wrapper.read(Type.VAR_INT) - BedrockProtocolVersion.PROTOCOL_ID_OVERLAP_PREVENTION_OFFSET; // protocol id
                    final String hostname = wrapper.read(Type.STRING); // hostname
                    final int port = wrapper.read(Type.UNSIGNED_SHORT); // port

                    wrapper.user().put(new HandshakeStorage(protocolVersion, hostname, port));
                });
            }
        });

        // Copied from BaseProtocol1_7
        this.registerServerbound(State.LOGIN, ServerboundLoginPackets.LOGIN_ACKNOWLEDGED.getId(), ServerboundLoginPackets.LOGIN_ACKNOWLEDGED.getId(), wrapper -> {
            final ProtocolInfo info = wrapper.user().getProtocolInfo();
            info.setState(State.CONFIGURATION);
        });
    }

    @Override
    public boolean isBaseProtocol() {
        return true;
    }

}
