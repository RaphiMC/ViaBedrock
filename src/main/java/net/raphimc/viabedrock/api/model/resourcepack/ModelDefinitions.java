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
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ModelDefinitions {

    private final Map<String, ModelDefinition> entityModels = new HashMap<>();

    public ModelDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            for (String modelPath : pack.content().getFilesDeep("models/", ".json")) {
                try {
                    for (JsonElement geometryElement : pack.content().getJson(modelPath).getAsJsonArray("minecraft:geometry")) {
                        final JsonObject description = geometryElement.getAsJsonObject().getAsJsonObject("description");
                        final String name = description.get("identifier").getAsString();
                        final ModelDefinition modelDefinition = new ModelDefinition(name, pack.content().getString(modelPath));
                        if (modelPath.startsWith("models/entity/")) {
                            this.entityModels.put(name, modelDefinition);
                        }
                    }
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse model definition " + modelPath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public ModelDefinition getEntityModel(final String name) {
        return this.entityModels.get(name);
    }

    public Map<String, ModelDefinition> entityModels() {
        return Collections.unmodifiableMap(this.entityModels);
    }

    public static class ModelDefinition {

        private final String name;
        private final String jsonForCubeConverter;

        public ModelDefinition(final String name, final String jsonForCubeConverter) {
            this.name = name;
            this.jsonForCubeConverter = jsonForCubeConverter;
        }

        public String name() {
            return this.name;
        }

        public String jsonForCubeConverter() {
            return this.jsonForCubeConverter;
        }

    }

}
