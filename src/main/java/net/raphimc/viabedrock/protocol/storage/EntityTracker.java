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
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_3Types;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class EntityTracker extends StoredObject {

    private final AtomicInteger ID_COUNTER = new AtomicInteger(1);

    private ClientPlayerEntity clientPlayerEntity = null;
    private final Map<Long, Long> runtimeIdToUniqueId = new HashMap<>();
    private final Map<Long, Entity> entities = new HashMap<>();

    public EntityTracker(final UserConnection user) {
        super(user);
    }

    public ClientPlayerEntity addClientPlayer(final long uniqueId, final long runtimeId) throws Exception {
        this.addEntity(new ClientPlayerEntity(this.getUser(), uniqueId, runtimeId, 0, this.getUser().getProtocolInfo().getUuid()));
        return this.clientPlayerEntity;
    }

    public Entity addEntity(final long uniqueId, final long runtimeId, final UUID uuid, final Entity1_19_3Types type) throws Exception {
        return this.addEntity(new Entity(this.getUser(), uniqueId, runtimeId, ID_COUNTER.getAndIncrement(), uuid != null ? uuid : UUID.randomUUID(), type));
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
            final PacketWrapper removeEntities = PacketWrapper.create(ClientboundPackets1_19_3.REMOVE_ENTITIES, this.getUser());
            removeEntities.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{prevEntity.javaId()}); // entity ids
            removeEntities.send(BedrockProtocol.class);

            prevEntity.deleteTeam();
        }

        entity.createTeam();

        return entity;
    }

    public void removeEntity(final Entity entity) throws Exception {
        if (entity instanceof ClientPlayerEntity) {
            throw new IllegalArgumentException("Cannot remove client player entity");
        }

        this.runtimeIdToUniqueId.remove(entity.runtimeId());
        this.entities.remove(entity.uniqueId());

        entity.deleteTeam();
    }

    public void tick() throws Exception {
        for (Entity entity : this.entities.values()) {
            entity.tick();
        }
    }

    public void prepareForRespawn() throws Exception {
        for (Entity entity : this.entities.values()) {
            entity.deleteTeam();
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
