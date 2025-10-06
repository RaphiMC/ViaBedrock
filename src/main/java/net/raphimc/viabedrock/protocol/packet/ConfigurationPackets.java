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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

public class ConfigurationPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_21_9.CLIENT_INFORMATION, null, new PacketHandlers() {
            @Override
            protected void register() {
                handler(MultiStatePackets.CLIENT_SETTINGS_HANDLER);
                handler(PacketWrapper::cancel);
            }
        });
        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_21_9.CUSTOM_PAYLOAD, null, MultiStatePackets.CUSTOM_PAYLOAD_HANDLER);
        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_21_9.FINISH_CONFIGURATION, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().getProtocolInfo().setClientState(State.PLAY);
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.CONFIGURATION_ACKNOWLEDGED, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().getProtocolInfo().setClientState(State.CONFIGURATION);
        });
    }

}
