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
package net.raphimc.viabedrock.util;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonParser;
import net.raphimc.viabedrock.api.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.resourcepack.content.DirectoryContent;
import net.raphimc.viabedrock.protocol.storage.ResourcePackStorage;
import net.raphimc.viabedrock.tool.JsonSorter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {

    public static ResourcePackStorage getClientResourcePacks(final File clientDataDir) throws IOException {
        final File resourcePacksDir = new File(clientDataDir, "resource_packs");
        final long start = System.currentTimeMillis();

        final byte[] data = JsonSorter.class.getResourceAsStream("/assets/viabedrock/data/custom/vanilla_resource_packs.json").readAllBytes();
        JsonArray obj = JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonArray();
        final List<String> orderedKeys = obj.asList().stream().map(JsonElement::getAsString).toList();

        final List<ResourcePack> resourcePacks = new ArrayList<>();
        for (File packDir : resourcePacksDir.listFiles()) {
            if (!new File(packDir, "manifest.json").exists()) {
                continue;
            }
            resourcePacks.add(new ResourcePack(new DirectoryContent(packDir.toPath())));
        }

        resourcePacks.removeIf(pack -> !orderedKeys.contains(pack.key().toString()));
        resourcePacks.sort((a, b) -> {
            final int indexA = orderedKeys.indexOf(a.key().toString());
            final int indexB = orderedKeys.indexOf(b.key().toString());
            return Integer.compare(indexA, indexB);
        });
        Collections.reverse(resourcePacks);

        final ResourcePackStorage resourcePackStorage = new ResourcePackStorage(resourcePacks);
        System.out.println("Preparation took " + (System.currentTimeMillis() - start) + "ms");
        return resourcePackStorage;
    }

}
