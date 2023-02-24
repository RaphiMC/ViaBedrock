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

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.mapping.PacketMapping;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Type;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayStatus;

import java.util.logging.Level;

public class RedirectToPlayPacketMapping implements PacketMapping {

    public static final RedirectToPlayPacketMapping INSTANCE = new RedirectToPlayPacketMapping();

    private RedirectToPlayPacketMapping() {
    }

    @Override
    public void applyType(PacketWrapper wrapper) {
    }

    @Override
    public PacketHandler handler() {
        return wrapper -> {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unexpected packet " + wrapper.getId() + " in state LOGIN. Redirecting to PLAY.");

            final PacketWrapper playStatus = PacketWrapper.create(ClientboundBedrockPackets.PLAY_STATUS, wrapper.user());
            playStatus.write(Type.INT, PlayStatus.LOGIN_SUCCESS); // status
            playStatus.send(BedrockProtocol.class, false);

            wrapper.send(BedrockProtocol.class, false);
            wrapper.cancel();
        };
    }

}
