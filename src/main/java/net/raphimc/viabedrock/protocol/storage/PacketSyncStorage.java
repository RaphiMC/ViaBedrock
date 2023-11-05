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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class PacketSyncStorage extends StoredObject {

    private final AtomicInteger ID = new AtomicInteger(0);
    private final Int2ObjectMap<Callable<Void>> pendingActions = new Int2ObjectOpenHashMap<>();

    public PacketSyncStorage(final UserConnection user) {
        super(user);
    }

    public void syncWithClient(final Callable<Void> callable) throws Exception {
        if (ID.get() >= Short.MAX_VALUE) { // VB compatibility
            ID.set(0);
        }
        final int id = ID.getAndIncrement();

        final State state = this.getUser().getProtocolInfo().getServerState();
        final PacketWrapper pingPacket = PacketWrapper.create(state == State.PLAY ? ClientboundPackets1_20_2.PING : ClientboundConfigurationPackets1_20_2.PING, this.getUser());
        pingPacket.write(Type.INT, id); // parameter
        pingPacket.send(BedrockProtocol.class);

        if (this.pendingActions.put(id, callable) != null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Overwrote pending action with id " + id);
        }
    }

    public void handleResponse(final int id) throws Exception {
        final Callable<Void> callable = this.pendingActions.remove(id);
        if (callable != null) {
            callable.call();
        } else {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received unexpected packet sync response with id " + id);
        }
    }

}
