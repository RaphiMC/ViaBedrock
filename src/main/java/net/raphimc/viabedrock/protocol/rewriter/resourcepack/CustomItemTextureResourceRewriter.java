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
package net.raphimc.viabedrock.protocol.rewriter.resourcepack;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.model.resourcepack.TextureDefinitions;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

public class CustomItemTextureResourceRewriter {

    public static final String ITEM = "paper";

    public static void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent) {
        final Map<Integer, JsonObject> overridesMap = new TreeMap<>();

        for (Map.Entry<String, TextureDefinitions.ItemTextureDefinition> entry : resourcePacksStorage.getTextures().itemTextures().entrySet()) {
            for (ResourcePack pack : resourcePacksStorage.getPackStackTopToBottom()) {
                final ResourcePack.Content bedrockContent = pack.content();
                final BufferedImage texture = bedrockContent.getShortnameImage(entry.getValue().texturePath());
                if (texture == null) continue;

                final String javaTexturePath = StringUtil.makeIdentifierValueSafe(entry.getValue().texturePath().replace("textures/items/", ""));
                final String javaModelName = StringUtil.makeIdentifierValueSafe(entry.getKey());
                final int javaModelData = getCustomModelData(entry.getKey());
                javaContent.putImage("assets/viabedrock/textures/item/" + javaTexturePath + ".png", texture);

                final JsonObject itemModel = new JsonObject();
                itemModel.addProperty("parent", "minecraft:item/generated");
                final JsonObject layer0 = new JsonObject();
                layer0.addProperty("layer0", "viabedrock:item/" + javaTexturePath);
                itemModel.add("textures", layer0);
                javaContent.putJson("assets/viabedrock/models/" + javaModelName + ".json", itemModel);

                final JsonObject override = new JsonObject();
                override.addProperty("model", "viabedrock:" + javaModelName);
                final JsonObject predicate = new JsonObject();
                predicate.addProperty("custom_model_data", javaModelData);
                override.add("predicate", predicate);
                overridesMap.put(javaModelData, override);
                break;
            }
        }

        if (!overridesMap.isEmpty()) {
            final JsonArray overrides = new JsonArray();
            overridesMap.values().forEach(overrides::add);

            final JsonObject itemDefinition = new JsonObject();
            itemDefinition.addProperty("parent", "minecraft:item/generated");
            itemDefinition.add("overrides", overrides);
            final JsonObject layer0 = new JsonObject();
            layer0.addProperty("layer0", "minecraft:item/" + ITEM);
            itemDefinition.add("textures", layer0);
            javaContent.putJson("assets/minecraft/models/item/" + ITEM + ".json", itemDefinition);
        }
    }

    public static int getCustomModelData(final String iconName) {
        return Math.abs(iconName.hashCode() + 1); // 0 is used for the default model
    }

}
