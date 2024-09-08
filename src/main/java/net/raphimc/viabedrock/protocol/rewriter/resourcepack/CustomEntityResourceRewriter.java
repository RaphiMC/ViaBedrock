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

import com.google.common.collect.Lists;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.api.model.resourcepack.EntityDefinitions;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import org.oryxel.cube.converter.FormatConverter;
import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.model.java.ItemModelData;
import org.oryxel.cube.parser.java.JavaModelSerializer;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CustomEntityResourceRewriter {

    public static final String ITEM = "armor_stand";

    public static void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent) {
        final Map<Integer, JsonObject> overridesMap = new TreeMap<>();

        for (Map.Entry<String, EntityDefinitions.EntityDefinition> entityEntry : resourcePacksStorage.getEntities().entities().entrySet()) {
            for (String bedrockPath : entityEntry.getValue().entityData().textures().values()) {
                final String javaPath = "entity_texture/" + StringUtil.makeIdentifierValueSafe(bedrockPath.replace("textures/", ""));
                for (ResourcePack pack : resourcePacksStorage.getPackStackTopToBottom()) {
                    final ResourcePack.Content bedrockContent = pack.content();
                    final BufferedImage texture = bedrockContent.getShortnameImage(bedrockPath);
                    if (texture == null) continue;

                    javaContent.putImage("assets/viabedrock/textures/item/" + javaPath + ".png", texture);
                    break;
                }
            }

            final EntityDefinitions.EntityDefinition entityDefinition = entityEntry.getValue();
            for (Map.Entry<String, String> modelEntry : entityDefinition.entityData().geometries().entrySet()) {
                final BedrockGeometry bedrockGeometry = resourcePacksStorage.getModels().models().get(modelEntry.getValue());
                if (bedrockGeometry == null) continue;
                if (!entityDefinition.entityData().textures().containsKey(modelEntry.getKey())) continue;

                final String javaTexturePath = "entity_texture/" + StringUtil.makeIdentifierValueSafe(entityDefinition.entityData().textures().get(modelEntry.getKey()).replace("textures/", ""));

                final List<ItemModelData> cubeConverterItemModels = Lists.newArrayList(FormatConverter.bedrockToJava("viabedrock:item/" + javaTexturePath, bedrockGeometry));
                final String key = entityEntry.getKey() + "_" + modelEntry.getKey();
                resourcePacksStorage.getConverterData().put("ce_" + key, cubeConverterItemModels.size());
                for (int i = 0; i < cubeConverterItemModels.size(); i++) {
                    final ItemModelData cubeConverterItemModel = cubeConverterItemModels.get(i);
                    final String javaModelName = StringUtil.makeIdentifierValueSafe(key + "_" + i);
                    final int javaModelData = getCustomModelData(key + "_" + i);
                    resourcePacksStorage.getConverterData().put("ce_" + key + "_" + i + "_scale", (float) cubeConverterItemModel.scale());

                    javaContent.putString("assets/viabedrock/models/" + javaModelName + ".json", JavaModelSerializer.serialize(cubeConverterItemModel).toString());

                    final JsonObject override = new JsonObject();
                    override.addProperty("model", "viabedrock:" + javaModelName);
                    final JsonObject predicate = new JsonObject();
                    predicate.addProperty("custom_model_data", javaModelData);
                    override.add("predicate", predicate);
                    if (overridesMap.put(javaModelData, override) != null) {
                        throw new IllegalStateException("Duplicate custom model data: " + override);
                    }
                }
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

    public static int getCustomModelData(final String name) {
        return Math.abs(name.hashCode() + 1); // 0 is used for the default model
    }

}
