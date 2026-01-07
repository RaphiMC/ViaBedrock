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
package net.raphimc.viabedrock.api.model.resourcepack;

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class FogDefinitions {

    private final Map<String, FogDefinition> fogs = new HashMap<>();

    public FogDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            for (String fogPath : pack.content().getFilesDeep("fogs/", ".json")) {
                try {
                    final JsonObject fog = pack.content().getJson(fogPath).getAsJsonObject("minecraft:fog_settings");
                    final String identifier = Key.namespaced(fog.getAsJsonObject("description").get("identifier").getAsString());
                    final Map<String, Integer> colors = new HashMap<>();
                    final JsonObject distance = fog.getAsJsonObject("distance");
                    for (Map.Entry<String, JsonElement> entry : distance.entrySet()) {
                        final JsonObject value = entry.getValue().getAsJsonObject();
                        if (value.has("fog_color")) {
                            colors.put(entry.getKey(), Integer.parseInt(value.get("fog_color").getAsString().substring(1), 16));
                        }
                    }
                    this.fogs.put(identifier, new FogDefinition(identifier, colors));
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse fog definition " + fogPath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public FogDefinition get(final String identifier) {
        return this.fogs.get(identifier);
    }

    public record FogDefinition(String identifier, Map<String, Integer> colors) {
    }

}
