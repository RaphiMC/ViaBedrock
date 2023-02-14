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
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.protocol.model.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class EntityTracker extends StoredObject {

    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private ClientPlayerEntity clientPlayerEntity = null;
    private final Map<Long, Entity> entities = new HashMap<>();

    public EntityTracker(final UserConnection user) {
        super(user);
    }

    public ClientPlayerEntity addClientPlayer(final long uniqueId, final long runtimeId) {
        this.clientPlayerEntity = new ClientPlayerEntity(uniqueId, runtimeId, ID_COUNTER.getAndIncrement());
        this.entities.put(uniqueId, this.clientPlayerEntity);

        return this.clientPlayerEntity;
    }

    // TODO: Behavior if entity is already present
    public Entity addEntity(final long uniqueId, final long runtimeId, final Entity1_19_3Types type) {
        final Entity entity = new Entity(uniqueId, runtimeId, ID_COUNTER.getAndIncrement(), type);
        this.entities.put(entity.uniqueId(), entity);

        return entity;
    }

    public void tick() {
        for (Entity entity : this.entities.values()) {
            try {
                entity.tick(this);
            } catch (Throwable e) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error while ticking entity " + entity.uniqueId(), e);
            }
        }
    }

    // TODO: Clear on dimension change
    public void clear() {
        this.entities.clear();
        this.entities.put(this.clientPlayerEntity.uniqueId(), this.clientPlayerEntity);
    }

    public Entity getEntity(final long uniqueId) {
        return this.entities.get(uniqueId);
    }

    public ClientPlayerEntity getClientPlayer() {
        return this.clientPlayerEntity;
    }

}
