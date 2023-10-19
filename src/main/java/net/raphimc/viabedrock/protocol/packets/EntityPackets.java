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

import com.google.common.collect.Lists;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.api.type.types.version.Types1_20;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.Direction;
import net.raphimc.viabedrock.protocol.data.enums.PaintingVariant;
import net.raphimc.viabedrock.protocol.model.EntityLink;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Map;
import java.util.logging.Level;

public class EntityPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_ENTITY, ClientboundPackets1_19_4.SPAWN_ENTITY, wrapper -> {
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

            final EntityTypes1_19_4 javaEntityType = BedrockProtocol.MAPPINGS.getBedrockToJavaEntities().get(Key.namespaced(identifier));
            if (javaEntityType == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock entity identifier: " + identifier);
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
            wrapper.write(Type.SHORT, (short) (motion.x() * 8000F)); // velocity x
            wrapper.write(Type.SHORT, (short) (motion.y() * 8000F)); // velocity y
            wrapper.write(Type.SHORT, (short) (motion.z() * 8000F)); // velocity z
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_ENTITY_ABSOLUTE, ClientboundPackets1_19_4.ENTITY_TELEPORT, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final short flags = wrapper.read(Type.UNSIGNED_BYTE); // flags
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final float pitch = MathUtil.byte2Float(wrapper.read(Type.BYTE)); // pitch
            final float yaw = MathUtil.byte2Float(wrapper.read(Type.BYTE)); // yaw
            final float headYaw = MathUtil.byte2Float(wrapper.read(Type.BYTE)); // head yaw
            final boolean onGround = (flags & 1) != 0;
            final boolean teleported = (flags & 2) != 0; // If the position shouldn't be interpolated
            final boolean forceMoveLocalEntity = (flags & 4) != 0;

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            if (entity instanceof ClientPlayerEntity) {
                if (!teleported && !forceMoveLocalEntity) {
                    wrapper.cancel();
                    return;
                }
                entity.setPosition(position);

                if (forceMoveLocalEntity) {
                    wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
                    wrapper.write(Type.DOUBLE, (double) entity.position().x()); // x
                    wrapper.write(Type.DOUBLE, (double) entity.position().y() - entity.eyeOffset()); // y
                    wrapper.write(Type.DOUBLE, (double) entity.position().z()); // z
                    wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().y())); // yaw
                    wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().x())); // pitch
                    wrapper.write(Type.BOOLEAN, entity.isOnGround()); // on ground
                } else { // teleport
                    // The player should keep the motions, but this is not possible with the current Java Edition protocol
                    wrapper.setPacketType(ClientboundPackets1_19_4.PLAYER_POSITION);
                    entityTracker.getClientPlayer().writePlayerPositionPacketToClient(wrapper, true, true);
                }
                return;
            }

            entity.setPosition(position);
            entity.setRotation(new Position3f(pitch, yaw, headYaw));
            entity.setOnGround(onGround);

            final PacketWrapper entityHeadLook = PacketWrapper.create(ClientboundPackets1_19_4.ENTITY_HEAD_LOOK, wrapper.user());
            entityHeadLook.write(Type.VAR_INT, entity.javaId()); // entity id
            entityHeadLook.write(Type.BYTE, MathUtil.float2Byte(headYaw)); // head yaw
            entityHeadLook.send(BedrockProtocol.class);

            wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Type.DOUBLE, (double) position.x()); // x
            wrapper.write(Type.DOUBLE, (double) position.y() - entity.eyeOffset()); // y
            wrapper.write(Type.DOUBLE, (double) position.z()); // z
            wrapper.write(Type.BYTE, MathUtil.float2Byte(yaw)); // yaw
            wrapper.write(Type.BYTE, MathUtil.float2Byte(pitch)); // pitch
            wrapper.write(Type.BOOLEAN, onGround); // on ground
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_ENTITY_DELTA, ClientboundPackets1_19_4.ENTITY_TELEPORT, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final int flags = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE); // flags
            final boolean hasX = (flags & 1) != 0;
            final boolean hasY = (flags & 2) != 0;
            final boolean hasZ = (flags & 4) != 0;
            final boolean hasPitch = (flags & 8) != 0;
            final boolean hasYaw = (flags & 16) != 0;
            final boolean hasHeadYaw = (flags & 32) != 0;
            final boolean onGround = (flags & 64) != 0;
            final boolean teleported = (flags & 128) != 0; // If the position shouldn't be interpolated
            final boolean forceMoveLocalEntity = (flags & 256) != 0;

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            if (entity instanceof ClientPlayerEntity) {
                if (!teleported && !forceMoveLocalEntity) {
                    wrapper.cancel();
                    return;
                }

                float x = 0F;
                float y = 0F;
                float z = 0F;
                if (hasX) {
                    x = wrapper.read(BedrockTypes.FLOAT_LE);
                }
                if (hasY) {
                    y = wrapper.read(BedrockTypes.FLOAT_LE);
                }
                if (hasZ) {
                    z = wrapper.read(BedrockTypes.FLOAT_LE);
                }
                entity.setPosition(new Position3f(x, y, z));

                wrapper.clearPacket();
                if (forceMoveLocalEntity) {
                    wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
                    wrapper.write(Type.DOUBLE, (double) entity.position().x()); // x
                    wrapper.write(Type.DOUBLE, (double) entity.position().y() - entity.eyeOffset()); // y
                    wrapper.write(Type.DOUBLE, (double) entity.position().z()); // z
                    wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().y())); // yaw
                    wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().x())); // pitch
                    wrapper.write(Type.BOOLEAN, entity.isOnGround()); // on ground
                } else { // teleport
                    // The player should keep the motions, but this is not possible with the current Java Edition protocol
                    wrapper.setPacketType(ClientboundPackets1_19_4.PLAYER_POSITION);
                    entityTracker.getClientPlayer().writePlayerPositionPacketToClient(wrapper, true, true);
                }
                return;
            }

            if (hasX) {
                entity.setPosition(new Position3f(wrapper.read(BedrockTypes.FLOAT_LE), entity.position().y(), entity.position().z()));
            }
            if (hasY) {
                entity.setPosition(new Position3f(entity.position().x(), wrapper.read(BedrockTypes.FLOAT_LE), entity.position().z()));
            }
            if (hasZ) {
                entity.setPosition(new Position3f(entity.position().x(), entity.position().y(), wrapper.read(BedrockTypes.FLOAT_LE)));
            }
            if (hasPitch) {
                entity.setRotation(new Position3f(MathUtil.byte2Float(wrapper.read(Type.BYTE)), entity.rotation().y(), entity.rotation().z()));
            }
            if (hasYaw) {
                entity.setRotation(new Position3f(entity.rotation().x(), MathUtil.byte2Float(wrapper.read(Type.BYTE)), entity.rotation().z()));
            }
            if (hasHeadYaw) {
                entity.setRotation(new Position3f(entity.rotation().x(), entity.rotation().y(), MathUtil.byte2Float(wrapper.read(Type.BYTE))));

                final PacketWrapper entityHeadLook = PacketWrapper.create(ClientboundPackets1_19_4.ENTITY_HEAD_LOOK, wrapper.user());
                entityHeadLook.write(Type.VAR_INT, entity.javaId()); // entity id
                entityHeadLook.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().z())); // head yaw
                entityHeadLook.send(BedrockProtocol.class);
            }
            entity.setOnGround(onGround);

            wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Type.DOUBLE, (double) entity.position().x()); // x
            wrapper.write(Type.DOUBLE, (double) entity.position().y() - entity.eyeOffset()); // y
            wrapper.write(Type.DOUBLE, (double) entity.position().z()); // z
            wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().y())); // yaw
            wrapper.write(Type.BYTE, MathUtil.float2Byte(entity.rotation().x())); // pitch
            wrapper.write(Type.BOOLEAN, entity.isOnGround()); // on ground
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_ENTITY_MOTION, ClientboundPackets1_19_4.ENTITY_VELOCITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final Position3f motion = wrapper.read(BedrockTypes.POSITION_3F); // motion

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Type.SHORT, (short) (motion.x() * 8000F)); // velocity x
            wrapper.write(Type.SHORT, (short) (motion.y() * 8000F)); // velocity y
            wrapper.write(Type.SHORT, (short) (motion.z() * 8000F)); // velocity z
        });
        protocol.registerClientbound(ClientboundBedrockPackets.REMOVE_ENTITY, ClientboundPackets1_19_4.REMOVE_ENTITIES, wrapper -> {
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
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_PAINTING, ClientboundPackets1_19_4.SPAWN_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final Direction direction = Direction.getFromHorizontalId(wrapper.read(BedrockTypes.VAR_INT), Direction.NORTH); // direction
            final PaintingVariant painting = PaintingVariant.getByName(wrapper.read(BedrockTypes.STRING)); // motive
            final Position3f positionOffset = painting.getJavaPositionOffset(direction);

            final Entity entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, null, EntityTypes1_19_4.PAINTING);
            entity.setPosition(position);

            wrapper.write(Type.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Type.UUID, entity.javaUuid()); // uuid
            wrapper.write(Type.VAR_INT, EntityTypes1_19_4.PAINTING.getId()); // type id
            wrapper.write(Type.DOUBLE, (double) position.x() + positionOffset.x()); // x
            wrapper.write(Type.DOUBLE, (double) position.y() + positionOffset.y()); // y
            wrapper.write(Type.DOUBLE, (double) position.z() + positionOffset.z()); // z
            wrapper.write(Type.BYTE, (byte) 0); // pitch
            wrapper.write(Type.BYTE, (byte) 0); // yaw
            wrapper.write(Type.BYTE, (byte) 0); // head yaw
            wrapper.write(Type.VAR_INT, direction.verticalId()); // data
            wrapper.write(Type.SHORT, (short) 0); // velocity x
            wrapper.write(Type.SHORT, (short) 0); // velocity y
            wrapper.write(Type.SHORT, (short) 0); // velocity z

            final PacketWrapper entityMetadata = PacketWrapper.create(ClientboundPackets1_19_4.ENTITY_METADATA, wrapper.user());
            entityMetadata.write(Type.VAR_INT, entity.javaId()); // entity id
            entityMetadata.write(Types1_19.METADATA_LIST, Lists.newArrayList(new Metadata(ProtocolConstants.PAINTING_VARIANT_ID, Types1_20.META_TYPES.paintingVariantType, painting.ordinal()))); // metadata

            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();
            entityMetadata.send(BedrockProtocol.class);
        });
    }

}
