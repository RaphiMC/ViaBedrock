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
package net.raphimc.viabedrock.protocol.packets;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

public class ConfigurationPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_20_2.CLIENT_INFORMATION, null, new PacketHandlers() {
            @Override
            protected void register() {
                handler(PacketWrapper::cancel);
                handler(MultiStatePackets.CLIENT_SETTINGS_HANDLER);
            }
        });
        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_20_2.CUSTOM_PAYLOAD, null, MultiStatePackets.CUSTOM_PAYLOAD_HANDLER);
        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().getProtocolInfo().setClientState(State.PLAY);
        });
        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_20_2.PONG, null, MultiStatePackets.PONG_HANDLER);
        protocol.registerServerbound(ServerboundPackets1_20_2.CONFIGURATION_ACKNOWLEDGED, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().getProtocolInfo().setClientState(State.CONFIGURATION);
        });
    }

}