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

import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.parser.bedrock.BedrockGeometrySerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ModelDefinitions {

    private final Map<String, BedrockGeometry> models = new HashMap<>();

    public ModelDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            for (String modelPath : pack.content().getFilesDeep("models/", ".json")) {
                try {
                    for (BedrockGeometry bedrockGeometry : BedrockGeometrySerializer.deserialize(pack.content().getString(modelPath))) {
                        // Attachable can be in models/entity/ for some reason, even tho it has it own folder for attachable definitions
                        // Just check for models that is not blocks.
                        if (!modelPath.startsWith("models/blocks/")) {
                            this.models.put(bedrockGeometry.identifier(), bedrockGeometry);
                        }
                    }
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse model definition " + modelPath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public BedrockGeometry getModel(final String name) {
        return this.models.get(name);
    }

    public Map<String, BedrockGeometry> models() {
        return Collections.unmodifiableMap(this.models);
    }

}
