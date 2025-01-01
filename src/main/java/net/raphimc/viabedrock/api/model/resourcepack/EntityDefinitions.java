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
package net.raphimc.viabedrock.api.model.resourcepack;

import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import org.oryxel.cube.model.bedrock.data.BedrockEntityData;
import org.oryxel.cube.parser.bedrock.data.BedrockEntitySerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

// https://wiki.bedrock.dev/entities/entity-intro-rp.html
public class EntityDefinitions {

    private final Map<String, EntityDefinition> entities = new HashMap<>();

    public EntityDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            for (String entityPath : pack.content().getFilesDeep("entity/", ".json")) {
                try {
                    final BedrockEntityData entityData = BedrockEntitySerializer.deserialize(pack.content().getString(entityPath));
                    final String identifier = Key.namespaced(entityData.identifier());
                    this.entities.put(identifier, new EntityDefinition(identifier, entityData));
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse entity definition " + entityPath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public EntityDefinition get(final String identifier) {
        return this.entities.get(identifier);
    }

    public Map<String, EntityDefinition> entities() {
        return Collections.unmodifiableMap(this.entities);
    }

    public record EntityDefinition(String identifier, BedrockEntityData entityData) {
    }

}
