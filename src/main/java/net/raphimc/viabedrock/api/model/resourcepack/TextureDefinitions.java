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

public class TextureDefinitions {

    private final Map<String, ItemTextureDefinition> itemTextures = new HashMap<>();

    public TextureDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            if (!pack.content().contains("textures/item_texture.json")) continue;
            try {
                final JsonObject itemTexture = pack.content().getJson("textures/item_texture.json");
                final String textureName = itemTexture.has("texture_name") ? itemTexture.get("texture_name").getAsString() : "atlas.items";
                if (textureName.equals("atlas.items")) {
                    final JsonObject textureData = itemTexture.getAsJsonObject("texture_data");
                    for (Map.Entry<String, JsonElement> entry : textureData.entrySet()) {
                        final String name = entry.getKey();
                        final String texturePath = entry.getValue().getAsJsonObject().get("textures").getAsString();
                        final ItemTextureDefinition itemTextureDefinition = new ItemTextureDefinition(name, texturePath);
                        this.itemTextures.put(name, itemTextureDefinition);
                    }
                }
            } catch (Throwable e) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse item texture definition in pack " + pack.packId(), e);
            }
        }
    }

    public Map<String, ItemTextureDefinition> itemTextures() {
        return Collections.unmodifiableMap(this.itemTextures);
    }

    public record ItemTextureDefinition(String name, String texturePath) {
    }

}
