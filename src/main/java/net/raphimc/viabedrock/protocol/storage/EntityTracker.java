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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
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
    private final Map<Position, Integer> itemFrames = new HashMap<>();

    public EntityTracker(final UserConnection user) {
        super(user);
    }

    public Entity addEntity(final long uniqueId, final long runtimeId, final UUID uuid, final Entity1_19_4Types type) throws Exception {
        switch (type) {
            case PLAYER:
                return this.addEntity(new PlayerEntity(this.getUser(), uniqueId, runtimeId, ID_COUNTER.getAndIncrement(), uuid != null ? uuid : UUID.randomUUID()));
            default:
                return this.addEntity(new Entity(this.getUser(), uniqueId, runtimeId, ID_COUNTER.getAndIncrement(), uuid != null ? uuid : UUID.randomUUID(), type));
        }
    }

    public Entity addEntity(final Entity entity) throws Exception {
        if (entity instanceof ClientPlayerEntity) {
            this.clientPlayerEntity = (ClientPlayerEntity) entity;
        }

        if (this.runtimeIdToUniqueId.putIfAbsent(entity.runtimeId(), entity.uniqueId()) != null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Duplicate runtime entity ID: " + entity.runtimeId());
        }
        final Entity prevEntity = this.entities.put(entity.uniqueId(), entity);
        if (prevEntity != null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Duplicate unique entity ID: " + entity.uniqueId());
            final PacketWrapper removeEntities = PacketWrapper.create(ClientboundPackets1_19_4.REMOVE_ENTITIES, this.getUser());
            removeEntities.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{prevEntity.javaId()}); // entity ids
            removeEntities.send(BedrockProtocol.class);

            if (prevEntity instanceof PlayerEntity) {
                ((PlayerEntity) prevEntity).deleteTeam();
            }
        }

        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).createTeam();
        }

        return entity;
    }

    public void removeEntity(final Entity entity) throws Exception {
        if (entity instanceof ClientPlayerEntity) {
            throw new IllegalArgumentException("Cannot remove client player entity");
        }

        this.runtimeIdToUniqueId.remove(entity.runtimeId());
        this.entities.remove(entity.uniqueId());

        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).deleteTeam();
        }
    }

    public void spawnItemFrame(final Position position, final BlockState blockState) throws Exception {
        this.removeItemFrame(position);

        if (!blockState.identifier().equals("frame") && !blockState.identifier().equals("glow_frame")) {
            throw new IllegalArgumentException("Block state must be a frame or glow_frame");
        }

        final int javaId = ID_COUNTER.getAndIncrement();
        this.itemFrames.put(position, javaId);

        final PacketWrapper spawnEntity = PacketWrapper.create(ClientboundPackets1_19_4.SPAWN_ENTITY, this.getUser());
        spawnEntity.write(Type.VAR_INT, javaId); // entity id
        spawnEntity.write(Type.UUID, UUID.randomUUID()); // uuid
        spawnEntity.write(Type.VAR_INT, blockState.identifier().equals("frame") ? Entity1_19_4Types.ITEM_FRAME.getId() : Entity1_19_4Types.GLOW_ITEM_FRAME.getId()); // type id
        spawnEntity.write(Type.DOUBLE, (double) position.x()); // x
        spawnEntity.write(Type.DOUBLE, (double) position.y()); // y
        spawnEntity.write(Type.DOUBLE, (double) position.z()); // z
        spawnEntity.write(Type.BYTE, (byte) 0); // pitch
        spawnEntity.write(Type.BYTE, (byte) 0); // yaw
        spawnEntity.write(Type.BYTE, (byte) 0); // head yaw
        spawnEntity.write(Type.VAR_INT, Integer.valueOf(blockState.properties().get("facing_direction"))); // data
        spawnEntity.write(Type.SHORT, (short) 0); // velocity x
        spawnEntity.write(Type.SHORT, (short) 0); // velocity y
        spawnEntity.write(Type.SHORT, (short) 0); // velocity z
        spawnEntity.send(BedrockProtocol.class);
    }

    public int getItemFrameId(final Position position) {
        return this.itemFrames.getOrDefault(position, -1);
    }

    public void removeItemFrame(final Position position) throws Exception {
        final Integer javaId = this.itemFrames.remove(position);
        if (javaId == null) {
            return;
        }

        final PacketWrapper removeEntities = PacketWrapper.create(ClientboundPackets1_19_4.REMOVE_ENTITIES, this.getUser());
        removeEntities.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{javaId}); // entity ids
        removeEntities.send(BedrockProtocol.class);
    }

    public void removeItemFrame(final int chunkX, final int chunkZ) throws Exception {
        final List<Position> toRemove = new ArrayList<>();
        for (final Map.Entry<Position, Integer> entry : this.itemFrames.entrySet()) {
            final Position position = entry.getKey();
            if (position.x() >> 4 == chunkX && position.z() >> 4 == chunkZ) {
                toRemove.add(position);
            }
        }

        for (final Position position : toRemove) {
            this.removeItemFrame(position);
        }
    }

    public void tick() throws Exception {
        for (Entity entity : this.entities.values()) {
            entity.tick();
        }
    }

    public void prepareForRespawn() throws Exception {
        for (Entity entity : this.entities.values()) {
            if (entity instanceof PlayerEntity) {
                ((PlayerEntity) entity).deleteTeam();
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

}
