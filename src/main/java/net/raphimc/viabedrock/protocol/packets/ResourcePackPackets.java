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
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ResourcePackStatus;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ResourcePackPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACKS_INFO, null, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    final PacketWrapper resourcePackClientResponse = wrapper.create(ClientboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE);
                    resourcePackClientResponse.write(Type.UNSIGNED_BYTE, ResourcePackStatus.HAVE_ALL_PACKS); // status
                    resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // resource pack ids
                    resourcePackClientResponse.sendToServer(BedrockProtocol.class);
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACK_STACK, null, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    final PacketWrapper resourcePackClientResponse = wrapper.create(ClientboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE);
                    resourcePackClientResponse.write(Type.UNSIGNED_BYTE, ResourcePackStatus.COMPLETED); // status
                    resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // resource pack ids
                    resourcePackClientResponse.sendToServer(BedrockProtocol.class);
                });
            }
        });
    }

}
