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

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.MovePlayerMode;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.protocol.model.entity.Entity;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class OtherPlayerPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_PLAYER, ClientboundPackets1_19_3.ENTITY_TELEPORT, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final Position3f rotation = wrapper.read(BedrockTypes.POSITION_3F); // rotation
            final short mode = wrapper.read(Type.UNSIGNED_BYTE); // mode
            final boolean onGround = wrapper.read(Type.BOOLEAN); // on ground
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // riding runtime entity id
            if (mode == MovePlayerMode.TELEPORT) {
                wrapper.read(BedrockTypes.INT_LE); // teleportation cause
                wrapper.read(BedrockTypes.INT_LE); // entity type
            }
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // tick

            final Entity entity = entityTracker.getEntity(runtimeEntityId);
            if (entity == null) {
                // TODO: handle this
                wrapper.cancel();
                return;
            }

            if (mode == MovePlayerMode.HEAD_ROTATION) {
                BedrockProtocol.kickForIllegalState(wrapper.user(), "Head rotation is not implemented");
                return;
            }

            entity.setPosition(position);
            entity.setRotation(rotation);
            entity.setOnGround(onGround);

            if ((mode == MovePlayerMode.TELEPORT || mode == MovePlayerMode.RESET) && entity instanceof ClientPlayerEntity) {
                final ClientPlayerEntity clientPlayer = (ClientPlayerEntity) entity;
                wrapper.setPacketType(ClientboundPackets1_19_3.PLAYER_POSITION);
                clientPlayer.writePlayerPositionPacketToClient(wrapper, false, mode == MovePlayerMode.RESET);
                return;
            }

            wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Type.DOUBLE, (double) position.x()); // x
            wrapper.write(Type.DOUBLE, (double) position.y() - 1.62F); // y
            wrapper.write(Type.DOUBLE, (double) position.z()); // z
            wrapper.write(Type.BYTE, (byte) (rotation.y() * (256F / 360F))); // yaw
            wrapper.write(Type.BYTE, (byte) (rotation.x() * (256F / 360F))); // pitch
            wrapper.write(Type.BOOLEAN, onGround); // on ground
        });
    }

}
