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
package net.raphimc.viabedrock.tool.generator.client;

import com.viaversion.viaversion.libs.gson.JsonArray;
import net.raphimc.viabedrock.api.resourcepack.definition.ParticleDefinitions;
import net.raphimc.viabedrock.protocol.storage.ResourcePackStorage;
import net.raphimc.viabedrock.tool.ToolArgs;
import net.raphimc.viabedrock.tool.ToolPaths;
import net.raphimc.viabedrock.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BedrockParticleListGenerator {

    public static void main(final String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final ResourcePackStorage resourcePackStorage = Util.getClientResourcePacks(ToolPaths.clientDataDir(toolArgs));

        final List<String> particleList = new ArrayList<>();
        for (Map.Entry<String, ParticleDefinitions.ParticleDefinition> entry : resourcePackStorage.getParticles().particles().entrySet()) {
            particleList.add(entry.getKey());
        }
        particleList.sort(String::compareTo);

        final JsonArray particles = new JsonArray();
        particleList.forEach(particles::add);
        ToolPaths.writeJson(ToolPaths.BEDROCK_DATA.resolve("particles.json"), particles);
    }

    private BedrockParticleListGenerator() {
    }

}
