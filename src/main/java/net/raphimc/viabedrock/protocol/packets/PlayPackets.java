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
package net.raphimc.viabedrock.protocol.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.providers.TransferProvider;
import net.raphimc.viabedrock.protocol.storage.BlobCache;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.net.InetSocketAddress;

public class PlayPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.SET_DIFFICULTY, ClientboundPackets1_20_3.SERVER_DIFFICULTY, new PacketHandlers() {
            @Override
            public void register() {
                map(BedrockTypes.UNSIGNED_VAR_INT, Type.UNSIGNED_BYTE); // difficulty
                create(Type.BOOLEAN, false); // locked
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CLIENT_CACHE_MISS_RESPONSE, null, wrapper -> {
            wrapper.cancel();
            final int length = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // blob count
            for (int i = 0; i < length; i++) {
                final long hash = wrapper.read(BedrockTypes.LONG_LE); // blob hash
                final byte[] blob = wrapper.read(BedrockTypes.BYTE_ARRAY); // blob data
                wrapper.user().get(BlobCache.class).addBlob(hash, blob);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.TRANSFER, null, wrapper -> {
            wrapper.cancel();
            final String hostname = wrapper.read(BedrockTypes.STRING); // hostname
            final short port = wrapper.read(BedrockTypes.SHORT_LE); // port

            Via.getManager().getProviders().get(TransferProvider.class).connectToServer(wrapper.user(), new InetSocketAddress(hostname, port));
        });

        protocol.registerServerbound(ServerboundPackets1_20_3.CLIENT_SETTINGS, ServerboundBedrockPackets.REQUEST_CHUNK_RADIUS, MultiStatePackets.CLIENT_SETTINGS_HANDLER);
        protocol.registerServerbound(ServerboundPackets1_20_3.PONG, null, MultiStatePackets.PONG_HANDLER);
        protocol.registerServerbound(ServerboundPackets1_20_3.PLUGIN_MESSAGE, null, MultiStatePackets.CUSTOM_PAYLOAD_HANDLER);
        protocol.registerServerbound(ServerboundPackets1_20_3.PING_REQUEST, null, wrapper -> {
            wrapper.cancel();
            final PacketWrapper pongResponse = wrapper.create(ClientboundPackets1_20_3.PONG_RESPONSE);
            pongResponse.write(Type.LONG, wrapper.read(Type.LONG)); // time
            pongResponse.send(BedrockProtocol.class);
        });
    }

}
