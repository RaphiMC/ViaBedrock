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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.storage.BlobCache;
import net.raphimc.viabedrock.protocol.storage.GameRulesStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class PlayPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.SET_DIFFICULTY, ClientboundPackets1_21.CHANGE_DIFFICULTY, new PacketHandlers() {
            @Override
            public void register() {
                map(BedrockTypes.UNSIGNED_VAR_INT, Types.UNSIGNED_BYTE); // difficulty
                create(Types.BOOLEAN, false); // locked
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CLIENT_CACHE_MISS_RESPONSE, null, wrapper -> {
            wrapper.cancel();
            final BlobCache blobCache = wrapper.user().get(BlobCache.class);
            final int length = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // blob count
            for (int i = 0; i < length; i++) {
                final long hash = wrapper.read(BedrockTypes.LONG_LE); // blob hash
                final byte[] blob = wrapper.read(BedrockTypes.BYTE_ARRAY); // blob data
                blobCache.addBlob(hash, blob);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.TRANSFER, ClientboundPackets1_21.TRANSFER, new PacketHandlers() {
            @Override
            protected void register() {
                map(BedrockTypes.STRING, Types.STRING); // address
                map(BedrockTypes.UNSIGNED_SHORT_LE, Types.VAR_INT); // port
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.GAME_RULES_CHANGED, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().get(GameRulesStorage.class).updateGameRules(wrapper.read(BedrockTypes.GAME_RULE_ARRAY)); // game rules
        });

        protocol.registerServerbound(ServerboundPackets1_20_5.CLIENT_INFORMATION, ServerboundBedrockPackets.REQUEST_CHUNK_RADIUS, MultiStatePackets.CLIENT_SETTINGS_HANDLER);
        protocol.registerServerbound(ServerboundPackets1_20_5.PONG, null, MultiStatePackets.PONG_HANDLER);
        protocol.registerServerbound(ServerboundPackets1_20_5.CUSTOM_PAYLOAD, null, MultiStatePackets.CUSTOM_PAYLOAD_HANDLER);
        protocol.registerServerbound(ServerboundPackets1_20_5.PING_REQUEST, null, wrapper -> {
            wrapper.cancel();
            final PacketWrapper pongResponse = wrapper.create(ClientboundPackets1_21.PONG_RESPONSE);
            pongResponse.write(Types.LONG, wrapper.read(Types.LONG)); // time
            pongResponse.send(BedrockProtocol.class);
        });
    }

}
