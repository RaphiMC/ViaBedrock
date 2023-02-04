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

import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class PlayPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.SET_DIFFICULTY, ClientboundPackets1_19_3.SERVER_DIFFICULTY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(BedrockTypes.UNSIGNED_VAR_INT, Type.UNSIGNED_BYTE); // difficulty
                create(Type.BOOLEAN, false); // locked
            }
        });

        protocol.registerServerbound(ServerboundPackets1_19_3.CLIENT_SETTINGS, ServerboundBedrockPackets.REQUEST_CHUNK_RADIUS, new PacketRemapper() {
            @Override
            public void registerMap() {
                read(Type.STRING); // locale
                map(Type.BYTE, BedrockTypes.VAR_INT); // view distance
                read(Type.VAR_INT); // chat visibility
                read(Type.BOOLEAN); // chat colors
                read(Type.UNSIGNED_BYTE); // skin parts
                read(Type.VAR_INT); // main hand
                read(Type.BOOLEAN); // text filtering
                read(Type.BOOLEAN); // server listing
            }
        });
    }

}
