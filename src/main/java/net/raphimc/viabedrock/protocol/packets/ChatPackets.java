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
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.BitSetType;
import com.viaversion.viaversion.api.type.types.ByteArrayType;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import net.raphimc.viabedrock.api.JsonUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.storage.AuthChainData;
import net.raphimc.viabedrock.protocol.storage.ChatSettingsStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ChatPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerServerbound(ServerboundPackets1_19_3.CHAT_MESSAGE, ServerboundBedrockPackets.TEXT, new PacketRemapper() {
            @Override
            public void registerMap() {
                create(Type.UNSIGNED_BYTE, (short) 1); // type
                create(Type.BOOLEAN, false); // needs translation
                handler(wrapper -> wrapper.write(BedrockTypes.STRING, wrapper.user().getProtocolInfo().getUsername())); // source name
                map(Type.STRING, BedrockTypes.STRING); // message
                handler(wrapper -> wrapper.write(BedrockTypes.STRING, wrapper.user().get(AuthChainData.class).getXuid())); // xuid
                create(BedrockTypes.STRING, ""); // platform chat id
                read(Type.LONG); // timestamp
                read(Type.LONG); // salt
                read(new ByteArrayType.OptionalByteArrayType(256)); // signature
                read(Type.VAR_INT); // offset
                read(new BitSetType(20)); // acknowledged
                handler(wrapper -> {
                    final ChatSettingsStorage chatSettings = wrapper.user().get(ChatSettingsStorage.class);
                    if (chatSettings.isServerRestricted()) {
                        wrapper.cancel();
                        final PacketWrapper systemChat = PacketWrapper.create(ClientboundPackets1_19_3.SYSTEM_CHAT, wrapper.user());
                        systemChat.write(Type.COMPONENT, JsonUtil.textToComponent("§eChat is currently disabled")); // message
                        systemChat.write(Type.BOOLEAN, false); // overlay
                        systemChat.send(BedrockProtocol.class);
                    }
                });
            }
        });
    }

}
