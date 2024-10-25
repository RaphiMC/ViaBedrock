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
package net.raphimc.viabedrock.api.model.entity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ActorDataIDs;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.Puv_Legacy_LevelSoundEvent;
import net.raphimc.viabedrock.protocol.data.enums.java.BossEventOperationType;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.*;
import java.util.logging.Level;

public class Entity {

    protected final UserConnection user;
    protected final long uniqueId;
    protected final long runtimeId;
    protected final String type;
    protected final int javaId;
    protected final UUID javaUuid;
    protected final EntityTypes1_21_2 javaType;

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

    public Entity(final UserConnection user, final long uniqueId, final long runtimeId, final String type, final int javaId, final UUID javaUuid, final EntityTypes1_21_2 javaType) {
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
            final PacketWrapper bossEvent = PacketWrapper.create(ClientboundPackets1_21_2.BOSS_EVENT, this.user);
            bossEvent.write(Types.UUID, this.javaUuid()); // uuid
            bossEvent.write(Types.VAR_INT, BossEventOperationType.REMOVE.ordinal()); // operation
            bossEvent.send(BedrockProtocol.class);
        }
    }

    public final void updateEntityData(final EntityData[] entityData) {
        final List<EntityData> javaEntityData = new ArrayList<>();
        this.updateEntityData(entityData, javaEntityData);
        final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_21_2.SET_ENTITY_DATA, this.user);
        setEntityData.write(Types.VAR_INT, this.javaId); // entity id
        setEntityData.write(Types1_21_2.ENTITY_DATA_LIST, javaEntityData); // entity data
        setEntityData.send(BedrockProtocol.class);
    }

    public final void updateEntityData(final EntityData[] entityData, final List<EntityData> javaEntityData) {
        for (EntityData data : entityData) {
            final ActorDataIDs id = ActorDataIDs.getByValue(data.id());
            if (id == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown ActorDataIDs: " + data.id());
                continue;
            }
            this.entityData.put(id, data);
            if (!this.translateEntityData(id, data, javaEntityData)) {
                // TODO: Log warning when entity data translation is fully implemented
                // ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received unknown entity data: " + id + " for entity type: " + this.type);
            }
        }
    }

    public void playSound(final Puv_Legacy_LevelSoundEvent soundEvent) {
        final PacketWrapper levelSoundEvent = PacketWrapper.create(ClientboundBedrockPackets.LEVEL_SOUND_EVENT, this.user);
        levelSoundEvent.write(BedrockTypes.UNSIGNED_VAR_INT, soundEvent.getValue()); // event
        levelSoundEvent.write(BedrockTypes.POSITION_3F, this.position); // position
        levelSoundEvent.write(BedrockTypes.VAR_INT, 0); // data
        levelSoundEvent.write(BedrockTypes.STRING, this.type); // entity identifier
        levelSoundEvent.write(Types.BOOLEAN, false); // is baby mob
        levelSoundEvent.write(Types.BOOLEAN, false); // is global sound
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

    public EntityTypes1_21_2 javaType() {
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

}
