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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

// https://wiki.bedrock.dev/items/item-components.html
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
                        if (components.has("minecraft:display_name")) {
                            itemDefinition.displayNameComponent = components.get("minecraft:display_name").getAsString();
                        }
                    }
                    this.items.put(identifier, itemDefinition);
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse item definition " + itemPath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public void addFromNetworkTag(final String identifier, final CompoundTag tag) {
        final ItemDefinition itemDefinition = new ItemDefinition(identifier);
        if (tag.get("components") instanceof CompoundTag components) {
            if (components.get("item_properties") instanceof CompoundTag itemProperties) {
                if (itemProperties.get("minecraft:icon") instanceof CompoundTag icon) {
                    if (icon.get("textures") instanceof CompoundTag texture) {
                        if (texture.get("default") instanceof StringTag defaultTexture) {
                            itemDefinition.iconComponent = defaultTexture.getValue();
                        }
                    }
                }
            }
            if (components.get("minecraft:display_name") instanceof CompoundTag displayName) {
                if (displayName.get("value") instanceof StringTag value) {
                    itemDefinition.displayNameComponent = value.getValue();
                }
            }
        }
        this.items.put(identifier, itemDefinition);
    }

    public ItemDefinition get(final String identifier) {
        return this.items.get(identifier);
    }

    public void remove(final String identifier) {
        this.items.remove(identifier);
    }

    public static class ItemDefinition {

        private final String identifier;
        private String iconComponent;
        private String displayNameComponent;

        public ItemDefinition(final String identifier) {
            this.identifier = identifier;
        }

        public String identifier() {
            return this.identifier;
        }

        public String iconComponent() {
            return this.iconComponent;
        }

        public String displayNameComponent() {
            return this.displayNameComponent;
        }

    }

}
