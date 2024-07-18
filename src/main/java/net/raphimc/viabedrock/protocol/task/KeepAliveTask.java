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
package net.raphimc.viabedrock.protocol.task;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

public class KeepAliveTask implements Runnable {

    public static final long INTERNAL_ID = 999; // ID which the server can't possibly send

    @Override
    public void run() {
        for (UserConnection info : Via.getManager().getConnectionManager().getConnections()) {
            final State state = info.getProtocolInfo().getServerState();
            if ((state == State.PLAY || state == State.CONFIGURATION) && info.getProtocolInfo().getPipeline().contains(BedrockProtocol.class)) {
                info.getChannel().eventLoop().submit(() -> {
                    if (!info.getChannel().isActive()) return;

                    try {
                        final PacketWrapper keepAlive = PacketWrapper.create(info.getProtocolInfo().getServerState() == State.PLAY ? ClientboundPackets1_21.KEEP_ALIVE : ClientboundConfigurationPackets1_21.KEEP_ALIVE, info);
                        keepAlive.write(Types.LONG, INTERNAL_ID); // id
                        keepAlive.send(BedrockProtocol.class);
                    } catch (Throwable e) {
                        BedrockProtocol.kickForIllegalState(info, "Error sending keep alive packet. See console for details.", e);
                    }
                });
            }
        }
    }

}
