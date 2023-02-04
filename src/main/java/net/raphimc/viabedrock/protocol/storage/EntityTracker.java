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
import net.raphimc.viabedrock.protocol.model.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityTracker extends StoredObject {

    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private long clientPlayerUniqueId = -1;
    private final Map<Long, Entity> entities = new HashMap<>();

    public EntityTracker(final UserConnection user) {
        super(user);
    }

    public void setClientPlayerUniqueId(final long uniqueId) {
        this.clientPlayerUniqueId = uniqueId;
    }

    // TODO: Behavior if entity is already present
    public Entity addEntity(final long uniqueId, final long runtimeId, final Entity1_19_3Types type) {
        final Entity entity = new Entity(uniqueId, runtimeId, ID_COUNTER.getAndIncrement(), type);
        this.entities.put(entity.uniqueId(), entity);

        return entity;
    }

    public Entity getEntity(final long uniqueId) {
        return this.entities.get(uniqueId);
    }

    public Entity getClientPlayer() {
        return this.entities.get(this.clientPlayerUniqueId);
    }

}
