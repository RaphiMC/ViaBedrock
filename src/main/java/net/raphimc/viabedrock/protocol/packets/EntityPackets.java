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

import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_3Types;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.model.EntityLink;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Map;
import java.util.logging.Level;

public class EntityPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_ENTITY, ClientboundPackets1_19_3.SPAWN_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final String identifier = wrapper.read(BedrockTypes.STRING); // identifier
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final Position3f motion = wrapper.read(BedrockTypes.POSITION_3F); // motion
            final Position3f rotation = wrapper.read(BedrockTypes.POSITION_3F); // rotation
            final float bodyRotation = wrapper.read(BedrockTypes.FLOAT_LE); // body rotation
            final int attributeCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // attribute count
            for (int i = 0; i < attributeCount; i++) {
                final String attributeIdentifier = wrapper.read(BedrockTypes.STRING); // attribute identifier
                final float min = wrapper.read(BedrockTypes.FLOAT_LE); // min
                final float max = wrapper.read(BedrockTypes.FLOAT_LE); // max
                final float value = wrapper.read(BedrockTypes.FLOAT_LE); // value
            }
            final Metadata[] metadata = wrapper.read(BedrockTypes.METADATA_ARRAY); // metadata
            final Int2IntMap intProperties = wrapper.read(BedrockTypes.INT_PROPERTIES); // int properties
            final Map<Integer, Float> floatProperties = wrapper.read(BedrockTypes.FLOAT_PROPERTIES); // float properties
            final EntityLink[] entityLinks = wrapper.read(BedrockTypes.ENTITY_LINK_ARRAY); // entity links

            // TODO: Handle remaining fields

            final String javaIdentifier = BedrockProtocol.MAPPINGS.getEntityIdentifiers().get(Key.namespaced(identifier));
            if (javaIdentifier == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock entity identifier: " + identifier);
                wrapper.cancel();
                return;
            }

            Entity1_19_3Types javaEntityType = null;
            for (Entity1_19_3Types type : Entity1_19_3Types.values()) {
                if (!type.isAbstractType() && type.identifier().equals(javaIdentifier)) {
                    javaEntityType = type;
                    break;
                }
            }
            if (javaEntityType == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown java entity identifier: " + javaIdentifier);
                wrapper.cancel();
                return;
            }

            final Entity entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, null, javaEntityType);
            entity.setPosition(position);
            entity.setRotation(rotation);

            wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Type.UUID, entity.javaUuid()); // uuid
            wrapper.write(Type.VAR_INT, javaEntityType.getId()); // type id
            wrapper.write(Type.DOUBLE, (double) position.x()); // x
            wrapper.write(Type.DOUBLE, (double) position.y()); // y
            wrapper.write(Type.DOUBLE, (double) position.z()); // z
            wrapper.write(Type.BYTE, MathUtil.float2Byte(rotation.x())); // pitch
            wrapper.write(Type.BYTE, MathUtil.float2Byte(rotation.y())); // yaw
            wrapper.write(Type.BYTE, MathUtil.float2Byte(rotation.z())); // head yaw
            wrapper.write(Type.VAR_INT, 0); // data
            wrapper.write(Type.SHORT, (short) 0); // velocity x
            wrapper.write(Type.SHORT, (short) 0); // velocity y
            wrapper.write(Type.SHORT, (short) 0); // velocity z
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_ENTITY_ABSOLUTE, ClientboundPackets1_19_3.ENTITY_TELEPORT, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final short flags = wrapper.read(Type.UNSIGNED_BYTE); // flags
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final float pitch = MathUtil.byte2Float(wrapper.read(Type.BYTE)); // pitch
            final float yaw = MathUtil.byte2Float(wrapper.read(Type.BYTE)); // yaw
            final float headYaw = MathUtil.byte2Float(wrapper.read(Type.BYTE)); // head yaw
            final boolean onGround = (flags & 1) != 0;
            final boolean teleported = (flags & 2) != 0; // Whether the position should be interpolated

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            entity.setPosition(position);
            entity.setOnGround(onGround);

            if (entity instanceof ClientPlayerEntity) {
                if (!teleported) {
                    wrapper.cancel();
                    return;
                }
                // The player should keep the motions, but this is not possible with the current Java Edition protocol
                wrapper.setPacketType(ClientboundPackets1_19_3.PLAYER_POSITION);
                entityTracker.getClientPlayer().writePlayerPositionPacketToClient(wrapper, true, true);
                return;
            }

            entity.setRotation(new Position3f(pitch, yaw, headYaw));

            final PacketWrapper entityHeadLook = PacketWrapper.create(ClientboundPackets1_19_3.ENTITY_HEAD_LOOK, wrapper.user());
            entityHeadLook.write(Type.VAR_INT, entity.javaId()); // entity id
            entityHeadLook.write(Type.BYTE, MathUtil.float2Byte(headYaw)); // head yaw
            entityHeadLook.send(BedrockProtocol.class);

            wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Type.DOUBLE, (double) position.x()); // x
            wrapper.write(Type.DOUBLE, (double) position.y() - 1.62F); // y
            wrapper.write(Type.DOUBLE, (double) position.z()); // z
            wrapper.write(Type.BYTE, MathUtil.float2Byte(yaw)); // yaw
            wrapper.write(Type.BYTE, MathUtil.float2Byte(pitch)); // pitch
            wrapper.write(Type.BOOLEAN, onGround); // on ground
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_ENTITY_DELTA, ClientboundPackets1_19_3.ENTITY_TELEPORT, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final int flags = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE); // flags

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            if (entity instanceof ClientPlayerEntity) {
                if ((flags & 128) == 0 && (flags & 256) == 0) { // !teleport && !force move local entity
                    wrapper.cancel();
                    return;
                }

                float x = 0F;
                float y = 0F;
                float z = 0F;
                if ((flags & 1) != 0) { // has x
                    x = wrapper.read(BedrockTypes.FLOAT_LE);
                }
                if ((flags & 2) != 0) { // has y
                    y = wrapper.read(BedrockTypes.FLOAT_LE);
                }
                if ((flags & 4) != 0) { // has z
                    z = wrapper.read(BedrockTypes.FLOAT_LE);
                }
                entity.setPosition(new Position3f(x, y, z));

                wrapper.clearPacket();
                if ((flags & 256) != 0) { // force move local entity
                    wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
                    wrapper.write(Type.DOUBLE, (double) entity.position().x()); // x
                    wrapper.write(Type.DOUBLE, (double) entity.position().y() - 1.62F); // y
                    wrapper.write(Type.DOUBLE, (double) entity.position().z()); // z
                    wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().y())); // yaw
                    wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().x())); // pitch
                    wrapper.write(Type.BOOLEAN, entity.isOnGround()); // on ground
                } else { // teleport
                    // The player should keep the motions, but this is not possible with the current Java Edition protocol
                    wrapper.setPacketType(ClientboundPackets1_19_3.PLAYER_POSITION);
                    entityTracker.getClientPlayer().writePlayerPositionPacketToClient(wrapper, true, true);
                }
                return;
            }

            if ((flags & 1) != 0) { // has x
                entity.setPosition(new Position3f(wrapper.read(BedrockTypes.FLOAT_LE), entity.position().y(), entity.position().z()));
            }
            if ((flags & 2) != 0) { // has y
                entity.setPosition(new Position3f(entity.position().x(), wrapper.read(BedrockTypes.FLOAT_LE), entity.position().z()));
            }
            if ((flags & 4) != 0) { // has z
                entity.setPosition(new Position3f(entity.position().x(), entity.position().y(), wrapper.read(BedrockTypes.FLOAT_LE)));
            }
            if ((flags & 8) != 0) { // has pitch
                entity.setRotation(new Position3f(MathUtil.byte2Float(wrapper.read(Type.BYTE)), entity.rotation().y(), entity.rotation().z()));
            }
            if ((flags & 16) != 0) { // has yaw
                entity.setRotation(new Position3f(entity.rotation().x(), MathUtil.byte2Float(wrapper.read(Type.BYTE)), entity.rotation().z()));
            }
            if ((flags & 32) != 0) { // has head yaw
                entity.setRotation(new Position3f(entity.rotation().x(), entity.rotation().y(), MathUtil.byte2Float(wrapper.read(Type.BYTE))));

                final PacketWrapper entityHeadLook = PacketWrapper.create(ClientboundPackets1_19_3.ENTITY_HEAD_LOOK, wrapper.user());
                entityHeadLook.write(Type.VAR_INT, entity.javaId()); // entity id
                entityHeadLook.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().z())); // head yaw
                entityHeadLook.send(BedrockProtocol.class);
            }
            entity.setOnGround((flags & 64) != 0);

            wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Type.DOUBLE, (double) entity.position().x()); // x
            wrapper.write(Type.DOUBLE, (double) entity.position().y()); // y
            wrapper.write(Type.DOUBLE, (double) entity.position().z()); // z
            wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().y())); // yaw
            wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().x())); // pitch
            wrapper.write(Type.BOOLEAN, entity.isOnGround()); // on ground
        });
        protocol.registerClientbound(ClientboundBedrockPackets.REMOVE_ENTITY, ClientboundPackets1_19_3.REMOVE_ENTITIES, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id

            final Entity entity = entityTracker.getEntityByUid(uniqueEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }
            entityTracker.removeEntity(entity);

            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{entity.javaId()}); // entity ids
        });
    }

}
