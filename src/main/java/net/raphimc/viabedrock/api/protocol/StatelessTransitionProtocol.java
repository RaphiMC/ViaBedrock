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

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;

public abstract class StatelessTransitionProtocol<CU extends ClientboundPacketType, CM extends ClientboundPacketType, SM extends ServerboundPacketType, SU extends ServerboundPacketType> extends StatelessProtocol<CU, CM, SM, SU> {

    public StatelessTransitionProtocol(final Class<CU> unmappedClientboundPacketType, final Class<CM> mappedClientboundPacketType, final Class<SM> mappedServerboundPacketType, final Class<SU> unmappedServerboundPacketType) {
        super(unmappedClientboundPacketType, mappedClientboundPacketType, mappedServerboundPacketType, unmappedServerboundPacketType);
    }

    public void registerServerboundTransition(final ServerboundPacketType unmappedPacketType, final SM mappedPacketType, final PacketHandler handler) {
        this.registerServerbound(unmappedPacketType.state(), unmappedPacketType.getId(), mappedPacketType != null ? mappedPacketType.getId() : -1, wrapper -> {
            wrapper.setPacketType(mappedPacketType);
            if (handler != null) {
                handler.handle(wrapper);
            }
        });
    }

    public void registerClientboundTransition(final CU unmappedPacketType, final Object... handlers) {
        if (handlers.length % 2 != 0) throw new IllegalArgumentException("handlers.length % 2 != 0");

        this.registerClientbound(unmappedPacketType.state(), unmappedPacketType.getId(), -1, wrapper -> {
            final State currentState = wrapper.user().getProtocolInfo().getServerState();

            for (int i = 0; i < handlers.length; i += 2) {
                if (handlers[i] instanceof State) {
                    final State state = (State) handlers[i];
                    if (state != currentState) continue;
                } else {
                    final ClientboundPacketType mappedPacketType = (ClientboundPacketType) handlers[i];
                    if (mappedPacketType.state() != currentState) continue;
                    wrapper.setPacketType(mappedPacketType);
                }

                final PacketHandler handler = (PacketHandler) handlers[i + 1];
                if (handler != null) {
                    handler.handle(wrapper);
                }
                return;
            }

            throw new IllegalStateException("No handler found for packet " + unmappedPacketType + " in state " + currentState);
        });
    }

}
