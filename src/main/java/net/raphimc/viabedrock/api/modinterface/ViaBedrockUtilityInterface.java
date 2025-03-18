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
package net.raphimc.viabedrock.api.modinterface;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.nio.charset.StandardCharsets;

public class ViaBedrockUtilityInterface {
    // This channel WILL ONLY be used to confirm that viabedrockutility is present, the server will use viabedrockutility:data to respond.
    public static final String CONFIRM_CHANNEL = "viabedrockutility:confirm";

    public static final String CHANNEL = "viabedrockutility:data";
    private static final int MESSAGE_CONFIRM = 0;
    private static final int MESSAGE_SPAWN_REQUEST = 1;
    private static final int MESSAGE_ANIMATE = 2;

    // Confirm that ViaBedrock is present.
    public static void confirm(final UserConnection user) {
        final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundConfigurationPackets1_21.CUSTOM_PAYLOAD, user);
        pluginMessage.write(Types.STRING, CHANNEL); // Channel
        pluginMessage.write(Types.INT, MESSAGE_CONFIRM); // Type
        pluginMessage.send(BedrockProtocol.class);
    }

    public static PacketWrapper spawn(final UserConnection user, final String geometry, final String texture) {
        final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundPackets1_21_2.CUSTOM_PAYLOAD, user);
        pluginMessage.write(Types.STRING, CHANNEL); // Channel
        pluginMessage.write(Types.INT, MESSAGE_SPAWN_REQUEST); // Type
        writeString(pluginMessage, geometry);
        writeString(pluginMessage, texture);
        return pluginMessage;
    }

    private static void writeString(final PacketWrapper wrapper, final String s) {
        wrapper.write(Types.INT, s.length());
        wrapper.write(Types.REMAINING_BYTES, s.getBytes(StandardCharsets.UTF_8));
    }
}
