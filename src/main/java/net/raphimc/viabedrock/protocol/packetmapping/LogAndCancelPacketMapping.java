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
package net.raphimc.viabedrock.protocol.packetmapping;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.mapping.PacketMapping;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.raphimc.viabedrock.ViaBedrock;

public class LogAndCancelPacketMapping implements PacketMapping {

    public static final LogAndCancelPacketMapping INSTANCE = new LogAndCancelPacketMapping();

    private LogAndCancelPacketMapping() {
    }

    @Override
    public void applyType(PacketWrapper wrapper) {
    }

    @Override
    public PacketHandler handler() {
        return wrapper -> {
            wrapper.cancel();
            final State state = wrapper.user().getProtocolInfo().getServerState();
            final ByteBuf content = ((PacketWrapperImpl) wrapper).getInputBuffer();
            ViaBedrock.getPlatform().getLogger().warning("Received unknown packet " + wrapper.getId() + " in state " + state + " with content: " + ByteBufUtil.hexDump(content));
        };
    }

}
