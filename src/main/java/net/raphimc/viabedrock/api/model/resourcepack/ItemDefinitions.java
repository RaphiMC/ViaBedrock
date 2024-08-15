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

import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ItemDefinitions {

    private final Map<String, ItemDefinition> items = new HashMap<>();

    public ItemDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            for (String itemPath : pack.content().getFilesDeep("items/", ".json")) {
                try {
                    final JsonObject item = pack.content().getJson(itemPath).getAsJsonObject("minecraft:item");
                    final String identifier = Key.namespaced(item.getAsJsonObject("description").get("identifier").getAsString());
                    final ItemDefinition itemDefinition = new ItemDefinition(identifier);
                    if (item.has("components")) {
                        final JsonObject components = item.getAsJsonObject("components");
                        if (components.has("minecraft:icon")) {
                            itemDefinition.iconComponent = components.get("minecraft:icon").getAsString();
                        }
                    }
                    this.items.put(identifier, itemDefinition);
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse item definition " + itemPath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public ItemDefinition get(final String identifier) {
        return this.items.get(identifier);
    }

    public static class ItemDefinition {

        private final String identifier;
        private String iconComponent;

        public ItemDefinition(final String identifier) {
            this.identifier = identifier;
        }

        public String identifier() {
            return this.identifier;
        }

        public String iconComponent() {
            return this.iconComponent;
        }

    }

}