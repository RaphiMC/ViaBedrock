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
package net.raphimc.viabedrock.experimental;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerActionType;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ExperimentalPacketFactory {

    public static void sendBedrockPlayerAction(final UserConnection user, long entityId, PlayerActionType actionType, BlockPosition position, BlockPosition resultPosition, int face) {
        final PacketWrapper startItemUseOn = PacketWrapper.create(ServerboundBedrockPackets.PLAYER_ACTION, user);
        startItemUseOn.write(BedrockTypes.UNSIGNED_VAR_LONG, entityId); // runtime entity id
        startItemUseOn.write(BedrockTypes.VAR_INT, actionType.getValue()); // action type
        startItemUseOn.write(BedrockTypes.BLOCK_POSITION, position); // block position
        startItemUseOn.write(BedrockTypes.BLOCK_POSITION, resultPosition); // result position
        startItemUseOn.write(BedrockTypes.VAR_INT, face); // face
        startItemUseOn.sendToServer(BedrockProtocol.class);
    }

}
