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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.model.entity.PlayerEntity;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class EntityTracker extends StoredObject {

    private final AtomicInteger ID_COUNTER = new AtomicInteger(1);

    private ClientPlayerEntity clientPlayerEntity = null;
    private final Map<Long, Long> runtimeIdToUniqueId = new HashMap<>();
    private final Map<Long, Entity> entities = new HashMap<>();
    private final Map<BlockPosition, Integer> itemFrames = new HashMap<>();

    public EntityTracker(final UserConnection user) {
        super(user);
    }

    public Entity addEntity(final long uniqueId, final long runtimeId, final UUID uuid, final EntityTypes1_20_5 type) {
        return switch (type) {
            default -> this.addEntity(new Entity(this.getUser(), uniqueId, runtimeId, ID_COUNTER.getAndIncrement(), uuid != null ? uuid : UUID.randomUUID(), type));
        };
    }

    public Entity addEntity(final Entity entity) {
        return this.addEntity(entity, true);
    }

    public Entity addEntity(final Entity entity, final boolean updateTeam) {
        if (entity instanceof ClientPlayerEntity) {
            this.clientPlayerEntity = (ClientPlayerEntity) entity;
        }

        if (this.runtimeIdToUniqueId.putIfAbsent(entity.runtimeId(), entity.uniqueId()) != null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Duplicate runtime entity ID: " + entity.runtimeId());
        }
        final Entity prevEntity = this.entities.put(entity.uniqueId(), entity);
        if (prevEntity != null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Duplicate unique entity ID: " + entity.uniqueId());
            final PacketWrapper removeEntities = PacketWrapper.create(ClientboundPackets1_21.REMOVE_ENTITIES, this.getUser());
            removeEntities.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{prevEntity.javaId()}); // entity ids
            removeEntities.send(BedrockProtocol.class);

            if (updateTeam && prevEntity instanceof PlayerEntity) {
                ((PlayerEntity) prevEntity).deleteTeam();
            }
        }

        if (updateTeam && entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).createTeam();
        }

        return entity;
    }

    public void removeEntity(final Entity entity) {
        if (entity instanceof ClientPlayerEntity) {
            throw new IllegalArgumentException("Cannot remove client player entity");
        }

        this.runtimeIdToUniqueId.remove(entity.runtimeId());
        this.entities.remove(entity.uniqueId());

        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).deleteTeam();
        }
    }

    public void spawnItemFrame(final BlockPosition position, final BlockState blockState) {
        this.removeItemFrame(position);

        if (!blockState.identifier().equals("frame") && !blockState.identifier().equals("glow_frame")) {
            throw new IllegalArgumentException("Block state must be a frame or glow_frame");
        }

        final int javaId = ID_COUNTER.getAndIncrement();
        this.itemFrames.put(position, javaId);

        final PacketWrapper spawnEntity = PacketWrapper.create(ClientboundPackets1_21.ADD_ENTITY, this.getUser());
        spawnEntity.write(Types.VAR_INT, javaId); // entity id
        spawnEntity.write(Types.UUID, UUID.randomUUID()); // uuid
        spawnEntity.write(Types.VAR_INT, blockState.identifier().equals("frame") ? EntityTypes1_20_5.ITEM_FRAME.getId() : EntityTypes1_20_5.GLOW_ITEM_FRAME.getId()); // type id
        spawnEntity.write(Types.DOUBLE, (double) position.x()); // x
        spawnEntity.write(Types.DOUBLE, (double) position.y()); // y
        spawnEntity.write(Types.DOUBLE, (double) position.z()); // z
        spawnEntity.write(Types.BYTE, (byte) 0); // pitch
        spawnEntity.write(Types.BYTE, (byte) 0); // yaw
        spawnEntity.write(Types.BYTE, (byte) 0); // head yaw
        spawnEntity.write(Types.VAR_INT, Integer.valueOf(blockState.properties().get("facing_direction"))); // data
        spawnEntity.write(Types.SHORT, (short) 0); // velocity x
        spawnEntity.write(Types.SHORT, (short) 0); // velocity y
        spawnEntity.write(Types.SHORT, (short) 0); // velocity z
        spawnEntity.send(BedrockProtocol.class);
    }

    public int getItemFrameId(final BlockPosition position) {
        return this.itemFrames.getOrDefault(position, -1);
    }

    public void removeItemFrame(final BlockPosition position) {
        final Integer javaId = this.itemFrames.remove(position);
        if (javaId == null) {
            return;
        }

        final PacketWrapper removeEntities = PacketWrapper.create(ClientboundPackets1_21.REMOVE_ENTITIES, this.getUser());
        removeEntities.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{javaId}); // entity ids
        removeEntities.send(BedrockProtocol.class);
    }

    public void removeItemFrame(final ChunkPosition chunkPos) {
        final List<BlockPosition> toRemove = new ArrayList<>();
        for (final Map.Entry<BlockPosition, Integer> entry : this.itemFrames.entrySet()) {
            final BlockPosition position = entry.getKey();
            if (position.x() >> 4 == chunkPos.chunkX() && position.z() >> 4 == chunkPos.chunkZ()) {
                toRemove.add(position);
            }
        }

        for (final BlockPosition position : toRemove) {
            this.removeItemFrame(position);
        }
    }

    public void tick() {
        for (Entity entity : this.entities.values()) {
            entity.tick();
        }
    }

    public void prepareForRespawn() {
        for (Entity entity : this.entities.values()) {
            if (entity instanceof PlayerEntity player) {
                player.deleteTeam();
            }
        }
    }

    public Entity getEntityByRid(final long runtimeId) {
        return this.entities.get(this.runtimeIdToUniqueId.get(runtimeId));
    }

    public Entity getEntityByUid(final long uniqueId) {
        return this.entities.get(uniqueId);
    }

    public ClientPlayerEntity getClientPlayer() {
        return this.clientPlayerEntity;
    }

    public boolean isEmpty() {
        return this.entities.isEmpty() || (this.entities.size() == 1 && this.entities.containsKey(this.clientPlayerEntity.uniqueId()));
    }

    public int getNextJavaEntityId() {
        return ID_COUNTER.getAndIncrement();
    }

}
