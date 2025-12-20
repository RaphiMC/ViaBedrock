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
package net.raphimc.viabedrock.tool;

import com.viaversion.viaversion.libs.gson.Gson;
import net.raphimc.viabedrock.api.model.resourcepack.ParticleDefinitions;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.util.Util;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BedrockParticleListGenerator {

    public static void main(String[] args) throws Throwable {
        final ResourcePacksStorage resourcePacksStorage = Util.getClientResourcePacks(new File("C:\\XboxGames\\Minecraft for Windows\\Content\\data"));

        final List<String> particleList = new ArrayList<>();
        for (Map.Entry<String, ParticleDefinitions.ParticleDefinition> entry : resourcePacksStorage.getParticles().particles().entrySet()) {
            particleList.add(entry.getKey());
        }
        particleList.sort(String::compareTo);

        final String json = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(particleList);
        Files.writeString(new File("particles.json").toPath(), json);
    }

}
