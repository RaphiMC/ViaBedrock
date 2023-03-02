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
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.protocol.model.entity.Entity;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class EntityPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_ENTITY_ABSOLUTE, ClientboundPackets1_19_3.ENTITY_TELEPORT, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final short flags = wrapper.read(Type.UNSIGNED_BYTE); // flags
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            float pitch = wrapper.read(Type.BYTE) * (360F / 256F); // pitch
            float yaw = wrapper.read(Type.BYTE) * (360F / 256F); // yaw
            float headYaw = wrapper.read(Type.BYTE) * (360F / 256F); // head yaw
            final boolean onGround = (flags & 1) != 0;
            final boolean teleported = (flags & 2) != 0;

            final Entity entity = entityTracker.getEntity(runtimeEntityId);
            if (entity == null) {
                // TODO: handle this
                wrapper.cancel();
                return;
            }

            if (entity instanceof ClientPlayerEntity) {
                if (!teleported) {
                    wrapper.cancel();
                    return;
                }

                entity.setPosition(position);
                entity.setOnGround(onGround);
                pitch = entity.rotation().x();
                yaw = entity.rotation().y();
                headYaw = entity.rotation().z();
            } else {
                entity.setPosition(position);
                entity.setRotation(new Position3f(pitch, yaw, headYaw));
                entity.setOnGround(onGround);
            }

            wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Type.DOUBLE, (double) position.x()); // x
            wrapper.write(Type.DOUBLE, (double) position.y() - 1.62F); // y
            wrapper.write(Type.DOUBLE, (double) position.z()); // z
            wrapper.write(Type.BYTE, (byte) (yaw * (256F / 360F))); // yaw
            wrapper.write(Type.BYTE, (byte) (pitch * (256F / 360F))); // pitch
            wrapper.write(Type.BOOLEAN, onGround); // on ground
        });
    }

}
