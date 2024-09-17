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

import com.google.common.collect.Lists;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.PaintingVariant;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
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
import net.raphimc.viabedrock.protocol.data.enums.bedrock.*;
import net.raphimc.viabedrock.protocol.data.enums.java.AnimateAction;
import net.raphimc.viabedrock.protocol.data.enums.java.EquipmentSlot;
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
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_ENTITY, ClientboundPackets1_21.ADD_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

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
            final EntityTypes1_20_5 javaEntityType = BedrockProtocol.MAPPINGS.getBedrockToJavaEntities().get(type);
            if (javaEntityType != null) {
                entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, type, javaEntityType);
            } else {
                final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
                final EntityDefinitions.EntityDefinition entityDefinition = resourcePacksStorage.getEntities().get(type);
                if (entityDefinition != null) {
                    if (resourcePacksStorage.isLoadedOnJavaClient() && resourcePacksStorage.getConverterData().containsKey("ce_" + entityDefinition.identifier() + "_default")) {
                        entity = new CustomEntity(wrapper.user(), uniqueEntityId, runtimeEntityId, type, entityTracker.getNextJavaEntityId(), entityDefinition);
                        entityTracker.addEntity(entity);
                    } else {
                        entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, type, EntityTypes1_20_5.PIG);
                    }
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock entity type: " + type);
                    wrapper.cancel();
                    return;
                }
            }
            entity.setPosition(position);
            entity.setRotation(rotation);

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.UUID, entity.javaUuid()); // uuid
            wrapper.write(Types.VAR_INT, entity.javaType().getId()); // type id
            wrapper.write(Types.DOUBLE, (double) position.x()); // x
            wrapper.write(Types.DOUBLE, (double) position.y()); // y
            wrapper.write(Types.DOUBLE, (double) position.z()); // z
            wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.x())); // pitch
            wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.y())); // yaw
            wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.z())); // head yaw
            wrapper.write(Types.VAR_INT, 0); // data
            wrapper.write(Types.SHORT, (short) (motion.x() * 8000F)); // velocity x
            wrapper.write(Types.SHORT, (short) (motion.y() * 8000F)); // velocity y
            wrapper.write(Types.SHORT, (short) (motion.z() * 8000F)); // velocity z
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.updateAttributes(attributes);
            }
            entity.updateEntityData(entityData);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_ITEM_ENTITY, ClientboundPackets1_21.ADD_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);

            final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final BedrockItem item = wrapper.read(itemRewriter.itemType()); // item
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final Position3f motion = wrapper.read(BedrockTypes.POSITION_3F); // motion
            final EntityData[] entityData = wrapper.read(BedrockTypes.ENTITY_DATA_ARRAY); // entity data
            wrapper.read(Types.BOOLEAN); // from fishing

            final Entity entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, "minecraft:item", EntityTypes1_20_5.ITEM);
            entity.setPosition(position);

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.UUID, entity.javaUuid()); // uuid
            wrapper.write(Types.VAR_INT, entity.javaType().getId()); // type id
            wrapper.write(Types.DOUBLE, (double) position.x()); // x
            wrapper.write(Types.DOUBLE, (double) position.y()); // y
            wrapper.write(Types.DOUBLE, (double) position.z()); // z
            wrapper.write(Types.BYTE, (byte) 0); // pitch
            wrapper.write(Types.BYTE, (byte) 0); // yaw
            wrapper.write(Types.BYTE, (byte) 0); // head yaw
            wrapper.write(Types.VAR_INT, 0); // data
            wrapper.write(Types.SHORT, (short) (motion.x() * 8000F)); // velocity x
            wrapper.write(Types.SHORT, (short) (motion.y() * 8000F)); // velocity y
            wrapper.write(Types.SHORT, (short) (motion.z() * 8000F)); // velocity z
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();

            final List<EntityData> javaEntityData = new ArrayList<>();
            entity.updateEntityData(entityData, javaEntityData);
            javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("ITEM"), Types1_21.ENTITY_DATA_TYPES.itemType, itemRewriter.javaItem(item)));
            final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_21.SET_ENTITY_DATA, wrapper.user());
            setEntityData.write(Types.VAR_INT, entity.javaId()); // entity id
            setEntityData.write(Types1_21.ENTITY_DATA_LIST, javaEntityData); // entity data
            setEntityData.send(BedrockProtocol.class);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_ENTITY_ABSOLUTE, ClientboundPackets1_21.TELEPORT_ENTITY, wrapper -> {
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

                if (forceMoveLocalEntity) {
                    wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
                    wrapper.write(Types.DOUBLE, (double) entity.position().x()); // x
                    wrapper.write(Types.DOUBLE, (double) entity.position().y() - entity.eyeOffset()); // y
                    wrapper.write(Types.DOUBLE, (double) entity.position().z()); // z
                    wrapper.write(Types.BYTE, MathUtil.float2Byte(entity.rotation().y())); // yaw
                    wrapper.write(Types.BYTE, MathUtil.float2Byte(entity.rotation().x())); // pitch
                    wrapper.write(Types.BOOLEAN, entity.isOnGround()); // on ground
                } else { // teleport
                    // The player should keep the motions, but this is not possible with the current Java Edition protocol
                    wrapper.setPacketType(ClientboundPackets1_21.PLAYER_POSITION);
                    entityTracker.getClientPlayer().writePlayerPositionPacketToClient(wrapper, true, true);
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
            wrapper.write(Types.BYTE, MathUtil.float2Byte(yaw)); // yaw
            wrapper.write(Types.BYTE, MathUtil.float2Byte(pitch)); // pitch
            wrapper.write(Types.BOOLEAN, onGround); // on ground

            PacketFactory.sendJavaRotateHead(wrapper.user(), entity);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_ENTITY_DELTA, ClientboundPackets1_21.TELEPORT_ENTITY, wrapper -> {
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
                if (forceMoveLocalEntity) {
                    wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
                    wrapper.write(Types.DOUBLE, (double) entity.position().x()); // x
                    wrapper.write(Types.DOUBLE, (double) entity.position().y() - entity.eyeOffset()); // y
                    wrapper.write(Types.DOUBLE, (double) entity.position().z()); // z
                    wrapper.write(Types.BYTE, MathUtil.float2Byte(entity.rotation().y())); // yaw
                    wrapper.write(Types.BYTE, MathUtil.float2Byte(entity.rotation().x())); // pitch
                    wrapper.write(Types.BOOLEAN, entity.isOnGround()); // on ground
                } else { // teleport
                    // The player should keep the motions, but this is not possible with the current Java Edition protocol
                    wrapper.setPacketType(ClientboundPackets1_21.PLAYER_POSITION);
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
            wrapper.write(Types.BYTE, MathUtil.float2Byte(entity.rotation().y())); // yaw
            wrapper.write(Types.BYTE, MathUtil.float2Byte(entity.rotation().x())); // pitch
            wrapper.write(Types.BOOLEAN, entity.isOnGround()); // on ground
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_ENTITY_MOTION, ClientboundPackets1_21.SET_ENTITY_MOTION, wrapper -> {
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
            wrapper.write(Types.SHORT, (short) (motion.x() * 8000F)); // velocity x
            wrapper.write(Types.SHORT, (short) (motion.y() * 8000F)); // velocity y
            wrapper.write(Types.SHORT, (short) (motion.z() * 8000F)); // velocity z
        });
        protocol.registerClientbound(ClientboundBedrockPackets.REMOVE_ENTITY, ClientboundPackets1_21.REMOVE_ENTITIES, wrapper -> {
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
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_PAINTING, ClientboundPackets1_21.ADD_ENTITY, wrapper -> {
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

            final Entity entity = entityTracker.addEntity(uniqueEntityId, runtimeEntityId, "minecraft:painting", EntityTypes1_20_5.PAINTING);
            entity.setPosition(position);

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.UUID, entity.javaUuid()); // uuid
            wrapper.write(Types.VAR_INT, entity.javaType().getId()); // type id
            wrapper.write(Types.DOUBLE, (double) position.x() + positionOffset.x()); // x
            wrapper.write(Types.DOUBLE, (double) position.y() + positionOffset.y()); // y
            wrapper.write(Types.DOUBLE, (double) position.z() + positionOffset.z()); // z
            wrapper.write(Types.BYTE, (byte) 0); // pitch
            wrapper.write(Types.BYTE, (byte) 0); // yaw
            wrapper.write(Types.BYTE, (byte) 0); // head yaw
            wrapper.write(Types.VAR_INT, direction.verticalId()); // data
            wrapper.write(Types.SHORT, (short) 0); // velocity x
            wrapper.write(Types.SHORT, (short) 0); // velocity y
            wrapper.write(Types.SHORT, (short) 0); // velocity z
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();

            final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_21.SET_ENTITY_DATA, wrapper.user());
            setEntityData.write(Types.VAR_INT, entity.javaId()); // entity id
            setEntityData.write(Types1_21.ENTITY_DATA_LIST, Lists.newArrayList(new EntityData(entity.getJavaEntityDataIndex("PAINTING_VARIANT"), Types1_21.ENTITY_DATA_TYPES.paintingVariantType, paintingHolder))); // entity data
            setEntityData.send(BedrockProtocol.class);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ENTITY_EVENT, ClientboundPackets1_21.ENTITY_EVENT, wrapper -> {
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
            switch (event) {
                case HURT -> {
                    final CompoundTag damageTypeRegistry = gameSession.getJavaRegistries().getCompoundTag("minecraft:damage_type");
                    final ActorDamageCause damageCause = ActorDamageCause.getByValue(data, ActorDamageCause.None);
                    final CompoundTag damageTypeEntry = damageTypeRegistry.getCompoundTag(BedrockProtocol.MAPPINGS.getBedrockToJavaDamageCauses().get(damageCause));

                    wrapper.setPacketType(ClientboundPackets1_21.DAMAGE_EVENT);
                    wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
                    wrapper.write(Types.VAR_INT, RegistryUtil.getRegistryIndex(damageTypeRegistry, damageTypeEntry)); // source type
                    wrapper.write(Types.VAR_INT, 0); // source cause id
                    wrapper.write(Types.VAR_INT, 0); // source direct id
                    wrapper.write(Types.BOOLEAN, false); // has source position
                    if (entity != entityTracker.getClientPlayer()) {
                        entity.playSound(Puv_Legacy_LevelSoundEvent.Hurt);
                    }
                }
                case DEATH -> {
                    wrapper.cancel();
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.setHealth(0F);
                        livingEntity.sendAttribute("minecraft:health");
                    }
                    if (entity == entityTracker.getClientPlayer() && entityTracker.getClientPlayer().isDead() && gameSession.getDeathMessage() != null) {
                        final PacketWrapper playerCombatKill = PacketWrapper.create(ClientboundPackets1_21.PLAYER_COMBAT_KILL, wrapper.user());
                        playerCombatKill.write(Types.VAR_INT, entityTracker.getClientPlayer().javaId()); // entity id
                        playerCombatKill.write(Types.TAG, TextUtil.textComponentToNbt(gameSession.getDeathMessage())); // message
                        playerCombatKill.send(BedrockProtocol.class);
                    }
                    if (entity != entityTracker.getClientPlayer()) {
                        entity.playSound(Puv_Legacy_LevelSoundEvent.Death);
                    }
                }
                default -> {
                    wrapper.cancel();
                    // TODO: Handle remaining events
                    // throw new IllegalStateException("Unhandled ActorEvent: " + event);
                }
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_ATTRIBUTES, ClientboundPackets1_21.UPDATE_ATTRIBUTES, wrapper -> {
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
            wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // tick

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.updateAttributes(attributes, wrapper);
            } else {
                wrapper.cancel();
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_ENTITY_DATA, ClientboundPackets1_21.SET_ENTITY_DATA, wrapper -> {
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
            wrapper.write(Types1_21.ENTITY_DATA_LIST, javaEntityData); // entity data
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOB_EFFECT, ClientboundPackets1_21.UPDATE_MOB_EFFECT, wrapper -> {
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final MobEffectPacket_Event event = MobEffectPacket_Event.getByValue(wrapper.read(Types.BYTE), MobEffectPacket_Event.Invalid); // event id
            final int effectId = wrapper.read(BedrockTypes.VAR_INT); // effect id
            final int amplifier = wrapper.read(BedrockTypes.VAR_INT); // amplifier
            final boolean showParticles = wrapper.read(Types.BOOLEAN); // show particles
            final int duration = wrapper.read(BedrockTypes.VAR_INT); // duration
            wrapper.read(BedrockTypes.LONG_LE); // tick

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
                    wrapper.setPacketType(ClientboundPackets1_21.REMOVE_MOB_EFFECT);
                    livingEntity.removeEffect(bedrockIdentifier, wrapper);
                }
                default -> throw new IllegalStateException("Unhandled MobEffectPacket_Event: " + event);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ANIMATE, ClientboundPackets1_21.ANIMATE, wrapper -> {
            final AnimatePacket_Action action = AnimatePacket_Action.getByValue(wrapper.read(BedrockTypes.VAR_INT), AnimatePacket_Action.NoAction); // action
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id

            final Entity entity = wrapper.user().get(EntityTracker.class).getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.UNSIGNED_BYTE, (short) (switch (action) {
                case NoAction, RowRight, RowLeft -> {
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
        protocol.registerClientbound(ClientboundBedrockPackets.MOB_ARMOR_EQUIPMENT, ClientboundPackets1_21.SET_EQUIPMENT, wrapper -> {
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
            wrapper.write(Types1_21.ITEM, itemRewriter.javaItem(feet)); // item
            wrapper.write(Types.BYTE, (byte) (EquipmentSlot.LEGS.ordinal() | Byte.MIN_VALUE)); // slot
            wrapper.write(Types1_21.ITEM, itemRewriter.javaItem(legs)); // item
            wrapper.write(Types.BYTE, (byte) (EquipmentSlot.CHEST.ordinal() | Byte.MIN_VALUE)); // slot
            wrapper.write(Types1_21.ITEM, itemRewriter.javaItem(chest)); // item
            wrapper.write(Types.BYTE, (byte) (EquipmentSlot.HEAD.ordinal() | Byte.MIN_VALUE)); // slot
            wrapper.write(Types1_21.ITEM, itemRewriter.javaItem(head)); // item
            wrapper.write(Types.BYTE, (byte) EquipmentSlot.BODY.ordinal()); // slot
            wrapper.write(Types1_21.ITEM, itemRewriter.javaItem(body)); // item
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOB_EQUIPMENT, ClientboundPackets1_21.SET_EQUIPMENT, wrapper -> {
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final BedrockItem item = wrapper.read(itemRewriter.itemType()); // item
            final byte slot = wrapper.read(Types.BYTE); // slot
            final byte selectedSlot = wrapper.read(Types.BYTE); // selected slot
            final byte windowId = wrapper.read(Types.BYTE); // window id

            final Entity entity = wrapper.user().get(EntityTracker.class).getEntityByRid(runtimeEntityId);
            if (entity == null || entity instanceof ClientPlayerEntity) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            if (windowId == ContainerID.CONTAINER_ID_INVENTORY.getValue() && slot >= 0 && slot < 9 && (slot == selectedSlot || selectedSlot < 0)) {
                wrapper.write(Types.BYTE, (byte) EquipmentSlot.MAINHAND.ordinal()); // slot
                wrapper.write(Types1_21.ITEM, itemRewriter.javaItem(item)); // item
            } else if (windowId == ContainerID.CONTAINER_ID_OFFHAND.getValue()) {
                wrapper.write(Types.BYTE, (byte) EquipmentSlot.OFFHAND.ordinal()); // slot
                wrapper.write(Types1_21.ITEM, itemRewriter.javaItem(item)); // item
            } else {
                wrapper.cancel();
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.TAKE_ITEM_ENTITY, ClientboundPackets1_21.TAKE_ITEM_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final long itemRuntimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // item runtime entity id
            final long collectorRuntimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // collector runtime entity id

            final Entity itemEntity = entityTracker.getEntityByRid(itemRuntimeEntityId);
            final Entity collectorEntity = entityTracker.getEntityByRid(collectorRuntimeEntityId);
            if (itemEntity == null || collectorEntity == null || itemEntity.javaType() != EntityTypes1_20_5.ITEM) {
                wrapper.cancel();
                return;
            }
            wrapper.write(Types.VAR_INT, itemEntity.javaId()); // item entity id
            wrapper.write(Types.VAR_INT, collectorEntity.javaId()); // collector entity id
            wrapper.write(Types.VAR_INT, 0); // amount
        });
    }

}
