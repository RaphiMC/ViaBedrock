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
package net.raphimc.viabedrock.api.model.entity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Vector3f;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_4;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21_4;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import net.raphimc.viabedrock.api.model.resourcepack.EntityDefinitions;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.CustomEntityResourceRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomEntity extends Entity {

    private final EntityDefinitions.EntityDefinition entityDefinition;
    private final List<ItemDisplayEntity> partEntities = new ArrayList<>();
    private boolean spawned;

    public CustomEntity(final UserConnection user, final long uniqueId, final long runtimeId, final String type, final int javaId, final EntityDefinitions.EntityDefinition entityDefinition) {
        super(user, uniqueId, runtimeId, type, javaId, UUID.randomUUID(), EntityTypes1_21_4.INTERACTION);
        this.entityDefinition = entityDefinition;
    }

    @Override
    public void setPosition(final Position3f position) {
        super.setPosition(position);

        if (!this.spawned) {
            this.spawn();
        } else {
            this.partEntities.forEach(ItemDisplayEntity::updatePositionAndRotation);
        }
    }

    @Override
    public void setRotation(final Position3f rotation) {
        super.setRotation(rotation);

        if (this.spawned) {
            this.partEntities.forEach(ItemDisplayEntity::updatePositionAndRotation);
        }
    }

    @Override
    public void remove() {
        super.remove();
        this.despawn();
    }

    private void spawn() {
        this.spawned = true;
        final EntityTracker entityTracker = user.get(EntityTracker.class);
        final ResourcePacksStorage resourcePacksStorage = user.get(ResourcePacksStorage.class);
        final int parts = (int) resourcePacksStorage.getConverterData().get("ce_" + this.entityDefinition.identifier() + "_default");
        for (int i = 0; i < parts; i++) {
            final ItemDisplayEntity partEntity = new ItemDisplayEntity(entityTracker.getNextJavaEntityId());
            this.partEntities.add(partEntity);
            final List<EntityData> javaEntityData = new ArrayList<>();

            final StructuredDataContainer data = ProtocolConstants.createStructuredDataContainer();
            data.set(StructuredDataKey.ITEM_MODEL, "viabedrock:entity");
            data.set(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4, CustomEntityResourceRewriter.getCustomModelData(this.entityDefinition.identifier() + "_default_" + i));
            final StructuredItem item = new StructuredItem(BedrockProtocol.MAPPINGS.getJavaItems().get("minecraft:paper"), 1, data);
            javaEntityData.add(new EntityData(partEntity.getJavaEntityDataIndex("ITEM_STACK"), Types1_21_4.ENTITY_DATA_TYPES.itemType, item));

            final float scale = (float) resourcePacksStorage.getConverterData().get("ce_" + this.entityDefinition.identifier() + "_default_" + i + "_scale");
            javaEntityData.add(new EntityData(partEntity.getJavaEntityDataIndex("SCALE"), Types1_21_4.ENTITY_DATA_TYPES.vector3FType, new Vector3f(scale, scale, scale)));
            javaEntityData.add(new EntityData(partEntity.getJavaEntityDataIndex("TRANSLATION"), Types1_21_4.ENTITY_DATA_TYPES.vector3FType, new Vector3f(0F, scale * 0.5F, 0F)));

            final PacketWrapper addEntity = PacketWrapper.create(ClientboundPackets1_21_2.ADD_ENTITY, user);
            addEntity.write(Types.VAR_INT, partEntity.javaId()); // entity id
            addEntity.write(Types.UUID, partEntity.javaUuid()); // uuid
            addEntity.write(Types.VAR_INT, partEntity.javaType().getId()); // type id
            addEntity.write(Types.DOUBLE, (double) this.position.x()); // x
            addEntity.write(Types.DOUBLE, (double) this.position.y()); // y
            addEntity.write(Types.DOUBLE, (double) this.position.z()); // z
            addEntity.write(Types.BYTE, MathUtil.float2Byte(this.rotation.x())); // pitch
            addEntity.write(Types.BYTE, MathUtil.float2Byte(this.rotation.y())); // yaw
            addEntity.write(Types.BYTE, MathUtil.float2Byte(this.rotation.z())); // head yaw
            addEntity.write(Types.VAR_INT, 0); // data
            addEntity.write(Types.SHORT, (short) 0); // velocity x
            addEntity.write(Types.SHORT, (short) 0); // velocity y
            addEntity.write(Types.SHORT, (short) 0); // velocity z
            addEntity.send(BedrockProtocol.class);

            final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_21_2.SET_ENTITY_DATA, user);
            setEntityData.write(Types.VAR_INT, partEntity.javaId()); // entity id
            setEntityData.write(Types1_21_4.ENTITY_DATA_LIST, javaEntityData); // entity data
            setEntityData.send(BedrockProtocol.class);
        }
    }

    private void despawn() {
        this.spawned = false;
        final int[] entityIds = new int[partEntities.size()];
        for (int i = 0; i < partEntities.size(); i++) {
            entityIds[i] = partEntities.get(i).javaId();
        }
        final PacketWrapper removeEntities = PacketWrapper.create(ClientboundPackets1_21_2.REMOVE_ENTITIES, this.user);
        removeEntities.write(Types.VAR_INT_ARRAY_PRIMITIVE, entityIds); // entity ids
        removeEntities.send(BedrockProtocol.class);
    }

    private class ItemDisplayEntity extends Entity {

        public ItemDisplayEntity(final int javaId) {
            super(CustomEntity.this.user, 0L, 0L, null, javaId, UUID.randomUUID(), EntityTypes1_21_4.ITEM_DISPLAY);
        }

        public void updatePositionAndRotation() {
            final PacketWrapper entityPositionSync = PacketWrapper.create(ClientboundPackets1_21_2.ENTITY_POSITION_SYNC, this.user);
            entityPositionSync.write(Types.VAR_INT, this.javaId()); // entity id
            entityPositionSync.write(Types.DOUBLE, (double) CustomEntity.this.position.x()); // x
            entityPositionSync.write(Types.DOUBLE, (double) CustomEntity.this.position.y()); // y
            entityPositionSync.write(Types.DOUBLE, (double) CustomEntity.this.position.z()); // z
            entityPositionSync.write(Types.DOUBLE, 0D); // velocity x
            entityPositionSync.write(Types.DOUBLE, 0D); // velocity y
            entityPositionSync.write(Types.DOUBLE, 0D); // velocity z
            entityPositionSync.write(Types.FLOAT, CustomEntity.this.rotation.y()); // yaw
            entityPositionSync.write(Types.FLOAT, CustomEntity.this.rotation.x()); // pitch
            entityPositionSync.write(Types.BOOLEAN, CustomEntity.this.onGround); // on ground
            entityPositionSync.send(BedrockProtocol.class);
        }

    }

}
