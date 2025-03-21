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

import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import org.cube.converter.data.bedrock.controller.BedrockRenderController;
import org.cube.converter.parser.bedrock.controller.BedrockControllerParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

// https://wiki.bedrock.dev/entities/render-controllers
public class RenderControllerDefinitions {

    private final Map<String, BedrockRenderController> renderControllers = new HashMap<>();

    public RenderControllerDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            for (String controllerPath : pack.content().getFilesDeep("render_controllers/", ".json")) {
                try {
                    for (BedrockRenderController bedrockRenderController : BedrockControllerParser.parse(pack.content().getString(controllerPath))) {
                        this.renderControllers.put(bedrockRenderController.identifier(), bedrockRenderController);
                    }
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse render controller " + controllerPath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public BedrockRenderController get(final String name) {
        return this.renderControllers.get(name);
    }

    public Map<String, BedrockRenderController> renderControllers() {
        return Collections.unmodifiableMap(this.renderControllers);
    }

}
