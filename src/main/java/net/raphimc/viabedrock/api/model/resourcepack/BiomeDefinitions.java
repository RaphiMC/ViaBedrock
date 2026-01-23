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

import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BiomeDefinitions {

    private final Map<String, BiomeDefinition> biomes = new HashMap<>();

    public BiomeDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            for (String biomePath : pack.content().getFilesDeep("biomes/", ".json")) {
                try {
                    final JsonObject biome = pack.content().getJson(biomePath).getAsJsonObject("minecraft:client_biome");
                    final String name = biome.getAsJsonObject("description").get("identifier").getAsString();
                    final BiomeDefinition biomeDefinition = new BiomeDefinition(name);
                    if (biome.has("components")) {
                        final JsonObject components = biome.getAsJsonObject("components");
                        if (components.has("minecraft:sky_color")) {
                            biomeDefinition.skyColor = Integer.parseInt(components.getAsJsonObject("minecraft:sky_color").get("sky_color").getAsString().substring(1, 7), 16);
                        }
                        if (components.has("minecraft:water_appearance")) {
                            biomeDefinition.waterSurfaceColor = Integer.parseInt(components.getAsJsonObject("minecraft:water_appearance").get("surface_color").getAsString().substring(1, 7), 16);
                        }
                        if (components.has("minecraft:fog_appearance")) {
                            biomeDefinition.fog = Key.namespaced(components.getAsJsonObject("minecraft:fog_appearance").get("fog_identifier").getAsString());
                        }
                    }
                    this.biomes.put(name, biomeDefinition);
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse biome definition " + biomePath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public BiomeDefinition get(final String name) {
        return this.biomes.get(name);
    }

    public Map<String, BiomeDefinition> biomes() {
        return Collections.unmodifiableMap(this.biomes);
    }

    public static class BiomeDefinition {

        private final String name;
        private Integer skyColor;
        private Integer waterSurfaceColor;
        private String fog;

        public BiomeDefinition(final String name) {
            this.name = name;
        }

        public String name() {
            return this.name;
        }

        public Integer skyColor() {
            return this.skyColor;
        }

        public Integer waterSurfaceColor() {
            return this.waterSurfaceColor;
        }

        public String fog() {
            return this.fog;
        }

    }

}
