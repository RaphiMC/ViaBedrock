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
package net.raphimc.viabedrock.protocol.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundPackets1_20_5;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.net.InetSocketAddress;

public class TransferProvider implements Provider {

    public void connectToServer(final UserConnection user, final InetSocketAddress newAddress) throws Exception {
        final PacketWrapper disconnect = PacketWrapper.create(ClientboundPackets1_20_5.DISCONNECT, user);
        disconnect.write(Type.TAG, TextUtil.stringToNbt("§cThe server tried to transfer you to §e" + newAddress.getHostString() + ":" + newAddress.getPort() + "§c.\nPlease connect manually to that address."));
        disconnect.send(BedrockProtocol.class);
    }

}
