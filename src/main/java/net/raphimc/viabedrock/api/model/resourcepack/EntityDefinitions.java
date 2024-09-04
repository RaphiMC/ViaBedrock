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
package net.raphimc.viabedrock.api.model.resourcepack;

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

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
                    final JsonObject description = pack.content().getJson(entityPath).getAsJsonObject("minecraft:client_entity").getAsJsonObject("description");
                    final String identifier = Key.namespaced(description.get("identifier").getAsString());
                    final EntityDefinition entityDefinition = new EntityDefinition(identifier, pack.content().getString(entityPath));
                    if (description.has("geometry")) {
                        final JsonObject geometry = description.getAsJsonObject("geometry");
                        for (Map.Entry<String, JsonElement> entry : geometry.entrySet()) {
                            entityDefinition.models.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                    if (description.has("textures")) {
                        final JsonObject textures = description.getAsJsonObject("textures");
                        for (Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                            entityDefinition.textures.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                    this.entities.put(identifier, entityDefinition);
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

    public static class EntityDefinition {

        private final String identifier;
        private final Map<String, String> models = new HashMap<>();
        private final Map<String, String> textures = new HashMap<>();
        private final String jsonForCubeConverter;

        public EntityDefinition(final String identifier, final String jsonForCubeConverter) {
            this.identifier = identifier;
            this.jsonForCubeConverter = jsonForCubeConverter;
        }

        public String identifier() {
            return this.identifier;
        }

        public Map<String, String> models() {
            return Collections.unmodifiableMap(this.models);
        }

        public Map<String, String> textures() {
            return Collections.unmodifiableMap(this.textures);
        }

        public String jsonForCubeConverter() {
            return this.jsonForCubeConverter;
        }

    }

}
