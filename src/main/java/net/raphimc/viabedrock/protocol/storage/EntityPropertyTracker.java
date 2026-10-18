/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectOpenHashMap;
import net.raphimc.viabedrock.api.model.entity.properties.EntityPropertyList;

public class EntityPropertyTracker extends StoredObject {

    Object2ObjectOpenHashMap<String, EntityPropertyList> entities = new Object2ObjectOpenHashMap<>();

    public EntityPropertyTracker(final UserConnection user) {
        super(user);
    }

    public void addEntity(final String entityId, final EntityPropertyList properties) {
        this.entities.put(entityId, properties);
    }

    public EntityPropertyList getEntityProperties(final String entityId) {
        return this.entities.get(entityId);
    }

}
