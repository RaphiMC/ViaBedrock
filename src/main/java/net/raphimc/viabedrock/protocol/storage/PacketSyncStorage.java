/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class PacketSyncStorage extends StoredObject {

    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private final Int2ObjectMap<Long> pendingNetworkStackLatencyResponses = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Runnable> pendingActions = new Int2ObjectOpenHashMap<>();

    public PacketSyncStorage(final UserConnection user) {
        super(user);
    }

    public int addNetworkStackLatencyResponse(final long timestamp) {
        if (ID_COUNTER.get() >= Short.MAX_VALUE) { // VB compatibility
            ID_COUNTER.set(0);
        }
        final int id = this.ID_COUNTER.getAndIncrement();
        if (this.pendingNetworkStackLatencyResponses.put(id, Long.valueOf(timestamp)) != null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Overwrote pending network stack latency response with id " + id);
        }
        return id;
    }

    public Long getNetworkStackLatencyResponse(final int id) {
        return this.pendingNetworkStackLatencyResponses.remove(id);
    }

    public void syncWithClient(final Runnable runnable) {
        if (ID_COUNTER.get() >= Short.MAX_VALUE) { // VB compatibility
            ID_COUNTER.set(0);
        }
        final int id = ID_COUNTER.getAndIncrement();
        if (this.pendingActions.put(id, runnable) != null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Overwrote pending action with id " + id);
        }

        final State state = this.user().getProtocolInfo().getServerState();
        final PacketWrapper pingPacket = PacketWrapper.create(state == State.PLAY ? ClientboundPackets1_21_11.PING : ClientboundConfigurationPackets1_21_9.PING, this.user());
        pingPacket.write(Types.INT, id); // parameter
        pingPacket.send(BedrockProtocol.class);
    }

    public boolean handleSyncTask(final int id) {
        final Runnable runnable = this.pendingActions.remove(id);
        if (runnable != null) {
            runnable.run();
            return true;
        } else {
            return false;
        }
    }

}
