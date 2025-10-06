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
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.EnumUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorDataIDs;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorFlags;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.DataItemType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.SharedTypes_Legacy_LevelSoundEvent;
import net.raphimc.viabedrock.protocol.data.enums.java.BossEventOperationType;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.entitydata.EntityDataTypesBedrock;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;

public class Entity {

    protected final UserConnection user;
    protected final long uniqueId;
    protected final long runtimeId;
    protected final String type;
    protected final int javaId;
    protected final UUID javaUuid;
    protected final EntityTypes1_21_9 javaType;

    /**
     * x, y, z
     */
    protected Position3f position;
    /**
     * pitch, yaw, headYaw
     */
    protected Position3f rotation = Position3f.ZERO;
    protected boolean onGround;
    protected final Map<ActorDataIDs, EntityData> entityData = new EnumMap<>(ActorDataIDs.class);
    protected String name;
    protected int age;
    protected boolean hasBossBar;

    public Entity(final UserConnection user, final long uniqueId, final long runtimeId, final String type, final int javaId, final UUID javaUuid, final EntityTypes1_21_9 javaType) {
        this.user = user;
        this.uniqueId = uniqueId;
        this.runtimeId = runtimeId;
        this.type = type;
        this.javaId = javaId;
        this.javaUuid = javaUuid;
        this.javaType = javaType;
    }

    public void tick() {
        this.age++;
    }

    public void remove() {
        if (this.hasBossBar) {
            this.hasBossBar = false;
            final PacketWrapper bossEvent = PacketWrapper.create(ClientboundPackets1_21_9.BOSS_EVENT, this.user);
            bossEvent.write(Types.UUID, this.javaUuid()); // uuid
            bossEvent.write(Types.VAR_INT, BossEventOperationType.REMOVE.ordinal()); // operation
            bossEvent.send(BedrockProtocol.class);
        }
    }

    public final void updateEntityData(final EntityData[] entityData) {
        final List<EntityData> javaEntityData = new ArrayList<>();
        this.updateEntityData(entityData, javaEntityData);
        final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_21_9.SET_ENTITY_DATA, this.user);
        setEntityData.write(Types.VAR_INT, this.javaId); // entity id
        setEntityData.write(VersionedTypes.V1_21_9.entityDataList, javaEntityData); // entity data
        setEntityData.send(BedrockProtocol.class);
    }

    public final void updateEntityData(final EntityData[] entityData, final List<EntityData> javaEntityData) {
        for (EntityData data : entityData) {
            final ActorDataIDs dataId = ActorDataIDs.getByValue(data.id());
            if (dataId == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown ActorDataIDs: " + data.id());
                continue;
            }
            final DataItemType expectedType = BedrockProtocol.MAPPINGS.getBedrockEntityDataTypes().get(dataId);
            if (expectedType != null && expectedType != ((EntityDataTypesBedrock) data.dataType()).dataItemType()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Discarding entity data " + dataId + " for entity type " + this.type + " due to unexpected data type: " + data.dataType());
                continue;
            }
            this.entityData.put(dataId, data);
            if (!this.translateEntityData(dataId, data, javaEntityData)) {
                // TODO: Log warning when entity data translation is fully implemented
                // ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received unknown entity data: " + dataId + " for entity type: " + this.type);
            }
        }
        this.onEntityDataChanged();
    }

    public void playSound(final SharedTypes_Legacy_LevelSoundEvent soundEvent) {
        final PacketWrapper levelSoundEvent = PacketWrapper.create(ClientboundBedrockPackets.LEVEL_SOUND_EVENT, this.user);
        levelSoundEvent.write(BedrockTypes.UNSIGNED_VAR_INT, soundEvent.getValue()); // event
        levelSoundEvent.write(BedrockTypes.POSITION_3F, this.position); // position
        levelSoundEvent.write(BedrockTypes.VAR_INT, 0); // data
        levelSoundEvent.write(BedrockTypes.STRING, this.type); // entity identifier
        levelSoundEvent.write(Types.BOOLEAN, false); // is baby mob
        levelSoundEvent.write(Types.BOOLEAN, false); // is global sound
        levelSoundEvent.write(BedrockTypes.LONG_LE, -1L); // unique entity id
        levelSoundEvent.send(BedrockProtocol.class, false);
    }

    public float eyeOffset() {
        return 0F;
    }

    public long uniqueId() {
        return this.uniqueId;
    }

    public long runtimeId() {
        return this.runtimeId;
    }

    public String type() {
        return this.type;
    }

    public int javaId() {
        return this.javaId;
    }

    public UUID javaUuid() {
        return this.javaUuid;
    }

    public EntityTypes1_21_9 javaType() {
        return this.javaType;
    }

    public Position3f position() {
        return this.position;
    }

    public void setPosition(final Position3f position) {
        this.position = position;
    }

    public Position3f rotation() {
        return this.rotation;
    }

    public void setRotation(final Position3f rotation) {
        this.rotation = rotation;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setOnGround(final boolean onGround) {
        this.onGround = onGround;
    }

    public Map<ActorDataIDs, EntityData> entityData() {
        return this.entityData;
    }

    public Set<ActorFlags> entityFlags() {
        BigInteger combinedFlags = BigInteger.ZERO;
        if (this.entityData.containsKey(ActorDataIDs.RESERVED_0)) {
            combinedFlags = combinedFlags.add(BigInteger.valueOf(this.entityData.get(ActorDataIDs.RESERVED_0).<Long>value().longValue()));
        }
        if (this.entityData.containsKey(ActorDataIDs.RESERVED_092)) {
            combinedFlags = combinedFlags.add(BigInteger.valueOf(this.entityData.get(ActorDataIDs.RESERVED_092).<Long>value().longValue()).shiftLeft(64));
        }
        return EnumUtil.getEnumSetFromBitmask(ActorFlags.class, combinedFlags, ActorFlags::getValue);
    }

    public String name() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int age() {
        return this.age;
    }

    public boolean hasBossBar() {
        return this.hasBossBar;
    }

    public void setHasBossBar(final boolean hasBossBar) {
        this.hasBossBar = hasBossBar;
    }

    public final int getJavaEntityDataIndex(final String fieldName) {
        final int index = BedrockProtocol.MAPPINGS.getJavaEntityData().get(this.javaType).indexOf(fieldName);
        if (index == -1) {
            throw new IllegalStateException("Unknown java entity data field: " + fieldName + " for entity type: " + this.javaType);
        }
        return index;
    }

    protected boolean translateEntityData(final ActorDataIDs id, final EntityData entityData, final List<EntityData> javaEntityData) {
        return false;
    }

    protected void onEntityDataChanged() {
    }

}
