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
import net.raphimc.viabedrock.protocol.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.protocol.model.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityTracker extends StoredObject {

    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private ClientPlayerEntity clientPlayerEntity = null;
    private final Map<Long, Entity> entities = new HashMap<>();

    public EntityTracker(final UserConnection user) {
        super(user);
    }

    public ClientPlayerEntity addClientPlayer(final long uniqueId, final long runtimeId) {
        this.clientPlayerEntity = new ClientPlayerEntity(this.getUser(), uniqueId, runtimeId, ID_COUNTER.getAndIncrement());
        this.entities.put(runtimeId, this.clientPlayerEntity);

        return this.clientPlayerEntity;
    }

    // TODO: Behavior if entity is already present
    public Entity addEntity(final long uniqueId, final long runtimeId, final Entity1_19_3Types type) {
        final Entity entity = new Entity(this.getUser(), uniqueId, runtimeId, ID_COUNTER.getAndIncrement(), type);
        this.entities.put(runtimeId, entity);

        return entity;
    }

    public void tick() throws Exception {
        for (Entity entity : this.entities.values()) {
            entity.tick();
        }
    }

    // TODO: Clear on dimension change
    public void clear() {
        this.entities.clear();
        this.entities.put(this.clientPlayerEntity.runtimeId(), this.clientPlayerEntity);
    }

    public Entity getEntity(final long runtimeId) {
        return this.entities.get(runtimeId);
    }

    public ClientPlayerEntity getClientPlayer() {
        return this.clientPlayerEntity;
    }

}
