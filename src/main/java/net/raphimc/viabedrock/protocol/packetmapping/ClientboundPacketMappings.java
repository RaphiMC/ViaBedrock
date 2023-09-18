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
package net.raphimc.viabedrock.protocol.packetmapping;

import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.mapping.PacketMapping;
import com.viaversion.viaversion.api.protocol.packet.mapping.PacketMappings;

public class ClientboundPacketMappings implements PacketMappings {

    private final PacketMappings delegate = PacketMappings.arrayMappings();

    @Override
    public PacketMapping mappedPacket(State state, int unmappedId) {
        final PacketMapping packetMapping = this.delegate.mappedPacket(state, unmappedId);
        if (packetMapping == null) {
            return LogAndCancelPacketMapping.INSTANCE;
        }
        return packetMapping;
    }

    @Override
    public boolean hasMapping(PacketType packetType) {
        return this.delegate.hasMapping(packetType);
    }

    @Override
    public boolean hasMapping(State state, int unmappedId) {
        return this.delegate.hasMapping(state, unmappedId);
    }

    @Override
    public void addMapping(State state, int unmappedId, PacketMapping mapping) {
        this.delegate.addMapping(state, unmappedId, mapping);
    }

}
