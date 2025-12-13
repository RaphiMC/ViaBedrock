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
package net.raphimc.viabedrock.protocol.packet;

import com.google.common.collect.Lists;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.PaintingVariant;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.model.entity.CustomEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.model.entity.LivingEntity;
import net.raphimc.viabedrock.api.model.resourcepack.EntityDefinitions;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.RegistryUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.Direction;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.data.enums.java.AnimateAction;
import net.raphimc.viabedrock.protocol.data.enums.java.EntityEvent;
import net.raphimc.viabedrock.protocol.data.enums.java.EquipmentSlot;
import net.raphimc.viabedrock.protocol.data.enums.java.Relative;
import net.raphimc.viabedrock.protocol.model.*;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class EntityPackets {

    private static final float PAINTING_POS_OFFSET = -0.46875F;

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_ENTITY, ClientboundPackets1_21_9.ADD_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

            final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final String type = Key.namespaced(wrapper.read(BedrockTypes.STRING)); // type
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final Position3f motion = wrapper.read(BedrockTypes.POSITION_3F); // motion
            final Position3f rotation = wrapper.read(BedrockTypes.POSITION_3F); // rotation
            wrapper.read(BedrockTypes.FLOAT_LE); // body rotation
            final EntityAttribute[] attributes = new EntityAttribute[wrapper.read(BedrockTypes.UNSIGNED_VAR_INT)]; // attribute count
            for (int i = 0; i < attributes.length; i++) {
                final String name = wrapper.read(BedrockTypes.STRING); // name
                final float minValue = wrapper.read(BedrockTypes.FLOAT_LE); // min
                final float currentValue = wrapper.read(BedrockTypes.FLOAT_LE); // current
                final float maxValue = wrapper.read(BedrockTypes.FLOAT_LE); // max
                attributes[i] = new EntityAttribute(name, currentValue, minValue, maxValue);
            }
            final EntityData[] entityData = wrapper.read(BedrockTypes.ENTITY_DATA_ARRAY); // entity data
            final EntityProperties entityProperties = wrapper.read(BedrockTypes.ENTITY_PROPERTIES); // entity properties
            final EntityLink[] entityLinks = wrapper.read(BedrockTypes.ENTITY_LINK_ARRAY); // entity links

            final Entity entity;
            final EntityTypes1_21_9 javaEntityType = BedrockProtocol.MAPPINGS.getBedrockToJavaEntities().get(type);
            if (javaEntityType != null) {
                entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, type, javaEntityType);
            } else if (gameSession.getAvailableEntityIdentifiers().contains(type)) {
                final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
                final EntityDefinitions.EntityDefinition entityDefinition = resourcePacksStorage.getEntities().get(type);
                if (entityDefinition != null) {
                    if (resourcePacksStorage.isLoadedOnJavaClient()) {
                        entity = new CustomEntity(wrapper.user(), uniqueEntityId, runtimeEntityId, type, entityTracker.getNextJavaEntityId(), entityDefinition);
                        entityTracker.addEntity(entity);
                    } else {
                        entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, type, EntityTypes1_21_9.PIG);
                    }
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing bedrock entity type: " + type);
                    wrapper.cancel();
                    return;
                }
            } else {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock entity type: " + type);
                wrapper.cancel();
                return;
            }
            entity.setPosition(position);
            entity.setRotation(rotation);

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.UUID, entity.javaUuid()); // uuid
            wrapper.write(Types.VAR_INT, entity.javaType().getId()); // type id
            wrapper.write(Types.DOUBLE, (double) position.x()); // x
            wrapper.write(Types.DOUBLE, (double) position.y()); // y
            wrapper.write(Types.DOUBLE, (double) position.z()); // z
            wrapper.write(Types.MOVEMENT_VECTOR, new Vector3d(motion.x(), motion.y(), motion.z())); // velocity
            wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.x())); // pitch
            wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.y())); // yaw
            wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.z())); // head yaw
            wrapper.write(Types.VAR_INT, 0); // data
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.updateAttributes(attributes);
            }
            entity.updateEntityData(entityData);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_ITEM_ENTITY, ClientboundPackets1_21_9.ADD_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);

            final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final BedrockItem item = wrapper.read(itemRewriter.itemType()); // item
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final Position3f motion = wrapper.read(BedrockTypes.POSITION_3F); // motion
            final EntityData[] entityData = wrapper.read(BedrockTypes.ENTITY_DATA_ARRAY); // entity data
            wrapper.read(Types.BOOLEAN); // from fishing

            final Entity entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, "minecraft:item", EntityTypes1_21_9.ITEM);
            entity.setPosition(position);

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.UUID, entity.javaUuid()); // uuid
            wrapper.write(Types.VAR_INT, entity.javaType().getId()); // type id
            wrapper.write(Types.DOUBLE, (double) position.x()); // x
            wrapper.write(Types.DOUBLE, (double) position.y()); // y
            wrapper.write(Types.DOUBLE, (double) position.z()); // z
            wrapper.write(Types.MOVEMENT_VECTOR, new Vector3d(motion.x(), motion.y(), motion.z())); // velocity
            wrapper.write(Types.BYTE, (byte) 0); // pitch
            wrapper.write(Types.BYTE, (byte) 0); // yaw
            wrapper.write(Types.BYTE, (byte) 0); // head yaw
            wrapper.write(Types.VAR_INT, 0); // data
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();

            final List<EntityData> javaEntityData = new ArrayList<>();
            entity.updateEntityData(entityData, javaEntityData);
            javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("ITEM"), VersionedTypes.V1_21_9.entityDataTypes.itemType, itemRewriter.javaItem(item)));
            final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_21_9.SET_ENTITY_DATA, wrapper.user());
            setEntityData.write(Types.VAR_INT, entity.javaId()); // entity id
            setEntityData.write(VersionedTypes.V1_21_9.entityDataList, javaEntityData); // entity data
            setEntityData.send(BedrockProtocol.class);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_ENTITY_ABSOLUTE, ClientboundPackets1_21_9.ENTITY_POSITION_SYNC, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final short flags = wrapper.read(Types.UNSIGNED_BYTE); // flags
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final float pitch = MathUtil.byte2Float(wrapper.read(Types.BYTE)); // pitch
            final float yaw = MathUtil.byte2Float(wrapper.read(Types.BYTE)); // yaw
            final float headYaw = MathUtil.byte2Float(wrapper.read(Types.BYTE)); // head yaw
            final boolean onGround = (flags & 1) != 0;
            final boolean teleported = (flags & 2) != 0; // If the position shouldn't be interpolated
            final boolean forceMoveLocalEntity = (flags & 4) != 0;

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            if (entity == entityTracker.getClientPlayer()) {
                if (!teleported && !forceMoveLocalEntity) {
                    wrapper.cancel();
                    return;
                }
                entity.setPosition(position);

                if (teleported) {
                    wrapper.setPacketType(ClientboundPackets1_21_9.PLAYER_POSITION);
                    entityTracker.getClientPlayer().writePlayerPositionPacketToClient(wrapper, Relative.union(Relative.ROTATION, Relative.VELOCITY), true);
                } else { // force move local entity
                    wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
                    wrapper.write(Types.DOUBLE, (double) entity.position().x()); // x
                    wrapper.write(Types.DOUBLE, (double) entity.position().y() - entity.eyeOffset()); // y
                    wrapper.write(Types.DOUBLE, (double) entity.position().z()); // z
                    wrapper.write(Types.DOUBLE, 0D); // velocity x
                    wrapper.write(Types.DOUBLE, 0D); // velocity y
                    wrapper.write(Types.DOUBLE, 0D); // velocity z
                    wrapper.write(Types.FLOAT, entity.rotation().y()); // yaw
                    wrapper.write(Types.FLOAT, entity.rotation().x()); // pitch
                    wrapper.write(Types.BOOLEAN, entity.isOnGround()); // on ground
                }
                return;
            }

            entity.setPosition(position);
            entity.setRotation(new Position3f(pitch, yaw, headYaw));
            entity.setOnGround(onGround);

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.DOUBLE, (double) position.x()); // x
            wrapper.write(Types.DOUBLE, (double) position.y() - entity.eyeOffset()); // y
            wrapper.write(Types.DOUBLE, (double) position.z()); // z
            wrapper.write(Types.DOUBLE, 0D); // velocity x
            wrapper.write(Types.DOUBLE, 0D); // velocity y
            wrapper.write(Types.DOUBLE, 0D); // velocity z
            wrapper.write(Types.FLOAT, yaw); // yaw
            wrapper.write(Types.FLOAT, pitch); // pitch
            wrapper.write(Types.BOOLEAN, onGround); // on ground

            PacketFactory.sendJavaRotateHead(wrapper.user(), entity);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_ENTITY_DELTA, ClientboundPackets1_21_9.ENTITY_POSITION_SYNC, wrapper -> {
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

            if (entity == entityTracker.getClientPlayer()) {
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
                if (teleported) {
                    wrapper.setPacketType(ClientboundPackets1_21_9.PLAYER_POSITION);
                    entityTracker.getClientPlayer().writePlayerPositionPacketToClient(wrapper, Relative.union(Relative.ROTATION, Relative.VELOCITY), true);
                } else { // force move local entity
                    wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
                    wrapper.write(Types.DOUBLE, (double) entity.position().x()); // x
                    wrapper.write(Types.DOUBLE, (double) entity.position().y() - entity.eyeOffset()); // y
                    wrapper.write(Types.DOUBLE, (double) entity.position().z()); // z
                    wrapper.write(Types.DOUBLE, 0D); // velocity x
                    wrapper.write(Types.DOUBLE, 0D); // velocity y
                    wrapper.write(Types.DOUBLE, 0D); // velocity z
                    wrapper.write(Types.FLOAT, entity.rotation().y()); // yaw
                    wrapper.write(Types.FLOAT, entity.rotation().x()); // pitch
                    wrapper.write(Types.BOOLEAN, entity.isOnGround()); // on ground
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
                entity.setRotation(new Position3f(MathUtil.byte2Float(wrapper.read(Types.BYTE)), entity.rotation().y(), entity.rotation().z()));
            }
            if (hasYaw) {
                entity.setRotation(new Position3f(entity.rotation().x(), MathUtil.byte2Float(wrapper.read(Types.BYTE)), entity.rotation().z()));
            }
            if (hasHeadYaw) {
                entity.setRotation(new Position3f(entity.rotation().x(), entity.rotation().y(), MathUtil.byte2Float(wrapper.read(Types.BYTE))));
                PacketFactory.sendJavaRotateHead(wrapper.user(), entity);
            }
            entity.setOnGround(onGround);

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.DOUBLE, (double) entity.position().x()); // x
            wrapper.write(Types.DOUBLE, (double) entity.position().y() - entity.eyeOffset()); // y
            wrapper.write(Types.DOUBLE, (double) entity.position().z()); // z
            wrapper.write(Types.DOUBLE, 0D); // velocity x
            wrapper.write(Types.DOUBLE, 0D); // velocity y
            wrapper.write(Types.DOUBLE, 0D); // velocity z
            wrapper.write(Types.FLOAT, entity.rotation().y()); // yaw
            wrapper.write(Types.FLOAT, entity.rotation().x()); // pitch
            wrapper.write(Types.BOOLEAN, entity.isOnGround()); // on ground
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_ENTITY_MOTION, ClientboundPackets1_21_9.SET_ENTITY_MOTION, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final Position3f motion = wrapper.read(BedrockTypes.POSITION_3F); // motion
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // tick

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.MOVEMENT_VECTOR, new Vector3d(motion.x(), motion.y(), motion.z())); // velocity
        });
        protocol.registerClientbound(ClientboundBedrockPackets.REMOVE_ENTITY, ClientboundPackets1_21_9.REMOVE_ENTITIES, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id

            final Entity entity = entityTracker.getEntityByUid(uniqueEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }
            entityTracker.removeEntity(entity);

            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{entity.javaId()}); // entity ids
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_PAINTING, ClientboundPackets1_21_9.ADD_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final CompoundTag paintingRegistry = gameSession.getJavaRegistries().getCompoundTag("minecraft:painting_variant");

            final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final Direction direction = Direction.getFromHorizontalId(wrapper.read(BedrockTypes.VAR_INT), Direction.NORTH); // direction
            final String motive = wrapper.read(BedrockTypes.STRING); // motive

            String javaIdentifier = BedrockProtocol.MAPPINGS.getBedrockToJavaPaintings().get(motive);
            if (javaIdentifier == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock painting motive: " + motive);
                javaIdentifier = "minecraft:kebab";
            }
            final CompoundTag paintingEntry = paintingRegistry.getCompoundTag(javaIdentifier);
            final Holder<PaintingVariant> paintingHolder = Holder.of(RegistryUtil.getRegistryIndex(paintingRegistry, paintingEntry));
            final int width = paintingEntry.getInt("width");
            final int height = paintingEntry.getInt("height");
            final float widthOffset = width % 2 == 0 ? 0.5F : 0;
            final float heightOffset = height % 2 == 0 ? 0.5F : 0;
            Position3f positionOffset = new Position3f(-0.5F, -0.5F, -0.5F);
            positionOffset = switch (direction) {
                case NORTH -> positionOffset.subtract(-widthOffset, heightOffset, -PAINTING_POS_OFFSET);
                case EAST -> positionOffset.subtract(PAINTING_POS_OFFSET, heightOffset, -widthOffset);
                case SOUTH -> positionOffset.subtract(widthOffset, heightOffset, PAINTING_POS_OFFSET);
                case WEST -> positionOffset.subtract(-PAINTING_POS_OFFSET, heightOffset, widthOffset);
                default -> positionOffset;
            };

            final Entity entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, "minecraft:painting", EntityTypes1_21_9.PAINTING);
            entity.setPosition(position);

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.UUID, entity.javaUuid()); // uuid
            wrapper.write(Types.VAR_INT, entity.javaType().getId()); // type id
            wrapper.write(Types.DOUBLE, (double) position.x() + positionOffset.x()); // x
            wrapper.write(Types.DOUBLE, (double) position.y() + positionOffset.y()); // y
            wrapper.write(Types.DOUBLE, (double) position.z() + positionOffset.z()); // z
            wrapper.write(Types.MOVEMENT_VECTOR, Vector3d.ZERO); // velocity
            wrapper.write(Types.BYTE, (byte) 0); // pitch
            wrapper.write(Types.BYTE, (byte) 0); // yaw
            wrapper.write(Types.BYTE, (byte) 0); // head yaw
            wrapper.write(Types.VAR_INT, direction.verticalId()); // data
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();

            final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_21_9.SET_ENTITY_DATA, wrapper.user());
            setEntityData.write(Types.VAR_INT, entity.javaId()); // entity id
            setEntityData.write(VersionedTypes.V1_21_9.entityDataList, Lists.newArrayList(new EntityData(entity.getJavaEntityDataIndex("PAINTING_VARIANT"), VersionedTypes.V1_21_9.entityDataTypes.paintingVariantType, paintingHolder))); // entity data
            setEntityData.send(BedrockProtocol.class);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ENTITY_EVENT, ClientboundPackets1_21_9.ENTITY_EVENT, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final byte rawEvent = wrapper.read(Types.BYTE); // event
            final ActorEvent event = ActorEvent.getByValue(rawEvent); // event
            if (event == null) {
                wrapper.cancel();
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown ActorEvent: " + rawEvent);
                return;
            }
            final int data = wrapper.read(BedrockTypes.VAR_INT); // data

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            ViaBedrock.getPlatform().getLogger().warning("ActorEvent: " + event); // TODO: Test logging (remove for merge)

            //https://minecraft.wiki/w/Bedrock_Edition_protocol/Entity_Events
            //https://minecraft.wiki/w/Java_Edition_protocol/Entity_statuses
            switch (event) {
                case HURT -> { // Sent when an entity gets hurt
                    final CompoundTag damageTypeRegistry = gameSession.getJavaRegistries().getCompoundTag("minecraft:damage_type");
                    final SharedTypes_Legacy_ActorDamageCause damageCause = SharedTypes_Legacy_ActorDamageCause.getByValue(data, SharedTypes_Legacy_ActorDamageCause.Override);
                    final CompoundTag damageTypeEntry = damageTypeRegistry.getCompoundTag(BedrockProtocol.MAPPINGS.getBedrockToJavaDamageCauses().get(damageCause));

                    wrapper.setPacketType(ClientboundPackets1_21_9.DAMAGE_EVENT);
                    wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
                    wrapper.write(Types.VAR_INT, RegistryUtil.getRegistryIndex(damageTypeRegistry, damageTypeEntry)); // source type
                    wrapper.write(Types.VAR_INT, 0); // source cause id
                    wrapper.write(Types.VAR_INT, 0); // source direct id
                    wrapper.write(Types.BOOLEAN, false); // has source position
                    if (entity != entityTracker.getClientPlayer()) {
                        entity.playSound(SharedTypes_Legacy_LevelSoundEvent.Hurt);
                    }
                }
                case DEATH -> { // Sent when an entity dies
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.setHealth(0F);
                        livingEntity.sendAttribute("minecraft:health");
                    }
                    if (entity == entityTracker.getClientPlayer() && entityTracker.getClientPlayer().isDead() && gameSession.getDeathMessage() != null) {
                        final PacketWrapper playerCombatKill = PacketWrapper.create(ClientboundPackets1_21_9.PLAYER_COMBAT_KILL, wrapper.user());
                        playerCombatKill.write(Types.VAR_INT, entityTracker.getClientPlayer().javaId()); // entity id
                        playerCombatKill.write(Types.TAG, TextUtil.textComponentToNbt(gameSession.getDeathMessage())); // message
                        playerCombatKill.send(BedrockProtocol.class);
                    }
                    if (entity != entityTracker.getClientPlayer()) {
                        //TODO: Java has an entity event for death but that only plays the sound no particles, find out how java syncs both
                        entity.playSound(SharedTypes_Legacy_LevelSoundEvent.Death);
                        wrapper.write(Types.INT, entity.javaId()); // entity id
                        wrapper.write(Types.BYTE, EntityEvent.POOF.getValue()); // entity event
                    } else {
                        wrapper.cancel();
                    }
                }
                case TAMING_FAILED -> { // Sent when you fail to tame an entity, used for particles
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.TAMING_FAILED.getValue()); // entity event
                }
                case TAMING_SUCCEEDED -> { // Sent when you successfully tame an entity, used for particles
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.TAMING_SUCCEEDED.getValue()); // entity event
                }
                case SHAKE_WETNESS -> { // Sent to sync wolf shake animation
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.SHAKE_WETNESS.getValue()); // entity event
                }
                case EAT_GRASS -> { // Sent to sync sheep eating animation
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.EAT_GRASS.getValue()); // entity event
                }
                case SQUID_FLEEING -> { // Sent when you attack a squid, most likely used for the ink particles
                    //TODO: Java has no equivalent event, send particles here
                }
                case ZOMBIE_CONVERTING -> { // TODO
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.ZOMBIE_CONVERTING.getValue()); // entity event
                    //TODO: Doesnt seem to make the zombie shake on java, needs testing
                }
                case START_OFFER_FLOWER -> { // Sent when an iron golem starts offering a flower
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.OFFER_FLOWER.getValue()); // entity event
                }
                case STOP_OFFER_FLOWER -> { // Sent when an iron golem stops offering a flower
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.STOP_OFFER_FLOWER.getValue()); // entity event
                }
                case LOVE_HEARTS -> { // Sent when an animal was bred, also sent when villagers are breeding
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    // Java splits these events and bedrock has cases for both but only seems to use one???
                    if (entityTracker.getEntityByRid(entity.runtimeId()).javaType().is(EntityTypes1_21_9.VILLAGER)) {
                        wrapper.write(Types.BYTE, EntityEvent.LOVE_HEARTS.getValue()); // entity event
                    } else {
                        wrapper.write(Types.BYTE, EntityEvent.IN_LOVE_HEARTS.getValue()); // entity event
                    }
                }
                case VILLAGER_ANGRY -> { // Sent to sync angry villager particles
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.VILLAGER_ANGRY.getValue()); // entity event
                }
                case VILLAGER_HAPPY -> { // Sent to sync happy villager particles
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.VILLAGER_HAPPY.getValue()); // entity event
                }
                case WITCH_HAT_MAGIC -> { // Sent to sync witch magic particles
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.WITCH_HAT_MAGIC.getValue()); // entity event
                }
                case FIREWORKS_EXPLODE -> { // Sent to sync firework explosions
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.FIREWORKS_EXPLODE.getValue()); // entity event
                }
                case IN_LOVE_HEARTS -> { //TODO: What calls this?
                    wrapper.cancel();
                    //wrapper.write(Types.INT, entity.javaId()); // entity id
                    //wrapper.write(Types.BYTE, EntityEvent.IN_LOVE_HEARTS.getValue()); // entity event
                }
                case SILVERFISH_MERGE_ANIM -> { // Displays particles when a silverfish merges into a block
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.SILVERFISH_MERGE_ANIM.getValue()); // entity event
                }
                case GUARDIAN_ATTACK_SOUND -> { // TODO: Gets called when a guardian attacks, Java does not seem to receive the sound effect properly
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.GUARDIAN_ATTACK_SOUND.getValue()); // entity event
                }
                case AIR_SUPPLY -> { // TODO: Test
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.DROWN_PARTICLES.getValue()); // entity event
                }
                case SHAKE -> { // TODO: Test
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.SHAKE.getValue()); // entity event
                }
                case INSTANT_DEATH -> { // TODO: Test
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.POOF.getValue()); // entity event
                }
                case TALISMAN_ACTIVATE -> { // Sent to sync totem activate animation
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.PROTECTED_FROM_DEATH.getValue()); // entity event
                }
                case TREASURE_HUNT -> { // TODO: Test
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.DOLPHIN_LOOKING_FOR_TREASURE.getValue()); // entity event
                }
                case VIBRATION_DETECTED -> { // Sent to sync warden tendril vibration animation
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.TENDRILS_SHIVER.getValue()); // entity event
                }
                case START_ATTACKING -> { // Sent to sync ravager and evoker attack animations TODO: doesnt play the evoker arm wave anim
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.START_ATTACKING.getValue()); // entity event
                }
                case GUARDIAN_MINING_FATIGUE -> {
                    // Handled in WorldEffectPackets under ParticleSoundGuardianGhost
                    //TODO: Should it be moved here / also handled here?
                    wrapper.cancel();
                }
                case SHAKE_WETNESS_STOP -> { // Sent to sync wolf shake animation
                    wrapper.write(Types.INT, entity.javaId()); // entity id
                    wrapper.write(Types.BYTE, EntityEvent.CANCEL_SHAKE_WETNESS.getValue()); // entity event
                }
                case DRAGON_START_DEATH_ANIM -> {
                    wrapper.cancel();

                    EntityData entityData = new EntityData(entity.getJavaEntityDataIndex("PHASE"),  VersionedTypes.V1_21_9.entityDataTypes().varIntType, 9); // DYING phase
                    //TODO: Allow java entity data to be set
                    //TODO: Test
                }
                case PUKE -> {
                    wrapper.cancel();

                    EntityData entityData = new EntityData(entity.getJavaEntityDataIndex("PHASE"),  VersionedTypes.V1_21_9.entityDataTypes().varIntType, 5); // Breath attack phase
                    //TODO: Allow java entity data to be set
                    //TODO: Check this is correct and test if it works
                }
                case NONE, // TODO: Whats the point of this????
                     JUMP, // TODO: What calls this?
                     FISHHOOK_TEASE, // TODO: Sync
                     FISHHOOK_BUBBLE, // TODO: Sync
                     FISHHOOK_FISHPOS, // TODO: Sync
                     FISHHOOK_HOOKTIME, // TODO: Sync
                     PLAY_AMBIENT, // TODO: Why the fuck is this an entity event? Does it ever actually get used????????
                     DRINK_POTION, // TODO: Find java equivalent
                     THROW_POTION, // TODO: Find java equivalent
                     PRIME_TNTCART, // TODO: Find java equivalent
                     PRIME_CREEPER, // TODO: Find java equivalent
                     GROUND_DUST, // TODO: What calls this?
                     FEED, // Sent when an animal is fed, java does not have an equivalent animation
                     BABY_AGE, // Sent to display "aging" particles when a baby is fed TODO: find java equivalent
                     NOTIFY_TRADE, // TODO: Find java equivalent
                     STOP_ATTACKING, // Not used in java and doesnt seem to be sent in bedrock
                     FINISHED_CHARGING_ITEM, // Sent when a crossbow finishes charging (might also be other senders) TODO: Add this
                     PLAYER_SPAWNED_MOB, //Sent when a player uses a spawn egg, idk what this is for
                     LEASH_DESTROYED, // TODO: Leash handling
                     CARAVAN_UPDATED, // TODO: Leash handling
                     AGENT_SWING_ARM, // Education edition feature
                     SUMMON_AGENT, // Education edition feature
                     BALLOON_POP, // Education edition feature TODO: double check
                     SPAWN_ALIVE, // TODO: Seems to get called when you respawn, not sure what it does
                     UPDATE_STACK_SIZE, // Sent to sync stack size updates for items on the ground TODO: find the java equivalent (might not be needed)
                     START_SWIMMING, // TODO: What calls this?
                     ACTOR_GROW_UP, // TODO: Find java equivalent
                     DRINK_MILK, //TODO: Find java equivalent
                     DEPRECATED_ADD_PLAYER_LEVELS, // Deprecated
                     DEPRECATED_UPDATE_STRUCTURE_FEATURE // Deprecated
                        -> wrapper.cancel();
                default -> {
                    wrapper.cancel();
                    throw new IllegalStateException("Unhandled ActorEvent: " + event);
                }
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_ATTRIBUTES, ClientboundPackets1_21_9.UPDATE_ATTRIBUTES, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final EntityAttribute[] attributes = new EntityAttribute[wrapper.read(BedrockTypes.UNSIGNED_VAR_INT)]; // attribute count
            for (int i = 0; i < attributes.length; i++) {
                final float minValue = wrapper.read(BedrockTypes.FLOAT_LE); // min value
                final float maxValue = wrapper.read(BedrockTypes.FLOAT_LE); // max value
                final float currentValue = wrapper.read(BedrockTypes.FLOAT_LE); // current value
                final float defaultMinValue = wrapper.read(BedrockTypes.FLOAT_LE); // default min value
                final float defaultMaxValue = wrapper.read(BedrockTypes.FLOAT_LE); // default max value
                final float defaultValue = wrapper.read(BedrockTypes.FLOAT_LE); // default value
                final String name = wrapper.read(BedrockTypes.STRING); // name
                final EntityAttribute.Modifier[] modifiers = new EntityAttribute.Modifier[wrapper.read(BedrockTypes.UNSIGNED_VAR_INT)]; // modifier count
                for (int j = 0; j < modifiers.length; j++) {
                    final String id = wrapper.read(BedrockTypes.STRING); // id
                    final String modifierName = wrapper.read(BedrockTypes.STRING); // name
                    final float amount = wrapper.read(BedrockTypes.FLOAT_LE); // amount
                    final AttributeModifierOperation operation = AttributeModifierOperation.getByValue(wrapper.read(BedrockTypes.INT_LE), AttributeModifierOperation.OPERATION_INVALID); // operation
                    final AttributeOperands operand = AttributeOperands.getByValue(wrapper.read(BedrockTypes.INT_LE), AttributeOperands.OPERAND_INVALID); // operand
                    final boolean isSerializable = wrapper.read(Types.BOOLEAN); // is serializable
                    modifiers[j] = new EntityAttribute.Modifier(id, modifierName, amount, operation, operand, isSerializable);
                }
                attributes[i] = new EntityAttribute(name, currentValue, minValue, maxValue, defaultValue, defaultMinValue, defaultMaxValue, modifiers);
            }
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // tick

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.updateAttributes(attributes, wrapper);
            } else {
                wrapper.cancel();
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_ENTITY_DATA, ClientboundPackets1_21_9.SET_ENTITY_DATA, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final EntityData[] entityData = wrapper.read(BedrockTypes.ENTITY_DATA_ARRAY); // entity data
            final EntityProperties entityProperties = wrapper.read(BedrockTypes.ENTITY_PROPERTIES); // entity properties
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // tick

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            final List<EntityData> javaEntityData = new ArrayList<>();
            entity.updateEntityData(entityData, javaEntityData);
            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(VersionedTypes.V1_21_9.entityDataList, javaEntityData); // entity data
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOB_EFFECT, ClientboundPackets1_21_9.UPDATE_MOB_EFFECT, wrapper -> {
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final MobEffectPacketPayload_Event event = MobEffectPacketPayload_Event.getByValue(wrapper.read(Types.BYTE), MobEffectPacketPayload_Event.Invalid); // event id
            final int effectId = wrapper.read(BedrockTypes.VAR_INT); // effect id
            final int amplifier = wrapper.read(BedrockTypes.VAR_INT); // amplifier
            final boolean showParticles = wrapper.read(Types.BOOLEAN); // show particles
            final int duration = wrapper.read(BedrockTypes.VAR_INT); // duration
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // tick

            final Entity entity = wrapper.user().get(EntityTracker.class).getEntityByRid(runtimeEntityId);
            if (!(entity instanceof LivingEntity livingEntity) || effectId == 0) {
                wrapper.cancel();
                return;
            }

            final String bedrockIdentifier = BedrockProtocol.MAPPINGS.getBedrockEffects().inverse().get(effectId);
            if (bedrockIdentifier == null) { // Bedrock client crashes
                throw new IllegalStateException("Unknown bedrock effect: " + effectId);
            }
            final EntityEffect effect = new EntityEffect(bedrockIdentifier, amplifier, duration, showParticles);
            switch (event) {
                case Invalid -> wrapper.cancel();
                case Add, Update -> livingEntity.updateEffect(effect, wrapper);
                case Remove -> {
                    wrapper.setPacketType(ClientboundPackets1_21_9.REMOVE_MOB_EFFECT);
                    livingEntity.removeEffect(bedrockIdentifier, wrapper);
                }
                default -> throw new IllegalStateException("Unhandled MobEffectPacketPayload_Event: " + event);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ANIMATE, ClientboundPackets1_21_9.ANIMATE, wrapper -> {
            final AnimatePacket_Action action = AnimatePacket_Action.getByValue(wrapper.read(BedrockTypes.VAR_INT), AnimatePacket_Action.NoAction); // action
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            wrapper.read(BedrockTypes.FLOAT_LE); // data

            final Entity entity = wrapper.user().get(EntityTracker.class).getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.UNSIGNED_BYTE, (short) (switch (action) {
                case NoAction, RowLeft, RowRight -> {
                    wrapper.cancel();
                    yield AnimateAction.SWING_MAIN_HAND; // any action
                }
                case Swing -> AnimateAction.SWING_MAIN_HAND;
                case WakeUp -> {
                    if (entity instanceof ClientPlayerEntity clientPlayer) {
                        clientPlayer.sendPlayerActionPacketToServer(PlayerActionType.StopSleeping);
                    }
                    yield AnimateAction.WAKE_UP;
                }
                case CriticalHit -> AnimateAction.CRITICAL_HIT;
                case MagicCriticalHit -> AnimateAction.MAGIC_CRITICAL_HIT;
                default -> throw new IllegalStateException("Unhandled AnimatePacket_Action: " + action);
            }).ordinal()); // action
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOB_ARMOR_EQUIPMENT, ClientboundPackets1_21_9.SET_EQUIPMENT, wrapper -> {
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final BedrockItem head = wrapper.read(itemRewriter.itemType()); // head
            final BedrockItem chest = wrapper.read(itemRewriter.itemType()); // chest
            final BedrockItem legs = wrapper.read(itemRewriter.itemType()); // legs
            final BedrockItem feet = wrapper.read(itemRewriter.itemType()); // feet
            final BedrockItem body = wrapper.read(itemRewriter.itemType()); // body

            final Entity entity = wrapper.user().get(EntityTracker.class).getEntityByRid(runtimeEntityId);
            if (entity == null || entity instanceof ClientPlayerEntity) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.BYTE, (byte) (EquipmentSlot.FEET.ordinal() | Byte.MIN_VALUE)); // slot
            wrapper.write(VersionedTypes.V1_21_9.item, itemRewriter.javaItem(feet)); // item
            wrapper.write(Types.BYTE, (byte) (EquipmentSlot.LEGS.ordinal() | Byte.MIN_VALUE)); // slot
            wrapper.write(VersionedTypes.V1_21_9.item, itemRewriter.javaItem(legs)); // item
            wrapper.write(Types.BYTE, (byte) (EquipmentSlot.CHEST.ordinal() | Byte.MIN_VALUE)); // slot
            wrapper.write(VersionedTypes.V1_21_9.item, itemRewriter.javaItem(chest)); // item
            wrapper.write(Types.BYTE, (byte) (EquipmentSlot.HEAD.ordinal() | Byte.MIN_VALUE)); // slot
            wrapper.write(VersionedTypes.V1_21_9.item, itemRewriter.javaItem(head)); // item
            wrapper.write(Types.BYTE, (byte) EquipmentSlot.BODY.ordinal()); // slot
            wrapper.write(VersionedTypes.V1_21_9.item, itemRewriter.javaItem(body)); // item
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOB_EQUIPMENT, ClientboundPackets1_21_9.SET_EQUIPMENT, wrapper -> {
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final BedrockItem item = wrapper.read(itemRewriter.itemType()); // item
            final byte slot = wrapper.read(Types.BYTE); // slot
            final byte selectedSlot = wrapper.read(Types.BYTE); // selected slot
            final byte containerId = wrapper.read(Types.BYTE); // container id

            final Entity entity = wrapper.user().get(EntityTracker.class).getEntityByRid(runtimeEntityId);
            if (entity == null || entity instanceof ClientPlayerEntity) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            if (containerId == ContainerID.CONTAINER_ID_INVENTORY.getValue() && slot >= 0 && slot < 9 && (slot == selectedSlot || selectedSlot < 0)) {
                wrapper.write(Types.BYTE, (byte) EquipmentSlot.MAINHAND.ordinal()); // slot
                wrapper.write(VersionedTypes.V1_21_9.item, itemRewriter.javaItem(item)); // item
            } else if (containerId == ContainerID.CONTAINER_ID_OFFHAND.getValue()) {
                wrapper.write(Types.BYTE, (byte) EquipmentSlot.OFFHAND.ordinal()); // slot
                wrapper.write(VersionedTypes.V1_21_9.item, itemRewriter.javaItem(item)); // item
            } else {
                wrapper.cancel();
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.TAKE_ITEM_ENTITY, ClientboundPackets1_21_9.TAKE_ITEM_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final long itemRuntimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // item runtime entity id
            final long collectorRuntimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // collector runtime entity id

            final Entity itemEntity = entityTracker.getEntityByRid(itemRuntimeEntityId);
            final Entity collectorEntity = entityTracker.getEntityByRid(collectorRuntimeEntityId);
            if (itemEntity == null || collectorEntity == null || itemEntity.javaType() != EntityTypes1_21_9.ITEM) {
                wrapper.cancel();
                return;
            }
            wrapper.write(Types.VAR_INT, itemEntity.javaId()); // item entity id
            wrapper.write(Types.VAR_INT, collectorEntity.javaId()); // collector entity id
            wrapper.write(Types.VAR_INT, 0); // amount
        });
    }

}
