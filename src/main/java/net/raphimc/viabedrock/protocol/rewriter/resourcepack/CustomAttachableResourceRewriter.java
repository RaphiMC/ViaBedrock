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
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.api.model.resourcepack.AttachableDefinitions;
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

public class CustomAttachableResourceRewriter {

    public static final String ITEM = "leather";

    public static void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent) {
        final Map<Integer, JsonObject> overridesMap = new TreeMap<>();

        for (Map.Entry<String, AttachableDefinitions.AttachableDefinition> entry : resourcePacksStorage.getAttachable().attachableDefinitions().entrySet()) {
            for (String bedrockPath : entry.getValue().attachableData().textures().values()) {
                final String javaPath = "attachable_texture/" + StringUtil.makeIdentifierValueSafe(bedrockPath.replace("textures/", ""));
                for (ResourcePack pack : resourcePacksStorage.getPackStackTopToBottom()) {
                    final ResourcePack.Content bedrockContent = pack.content();
                    final BufferedImage texture = bedrockContent.getShortnameImage(bedrockPath);
                    if (texture == null) continue;

                    javaContent.putImage("assets/viabedrock/textures/item/" + javaPath + ".png", texture);
                    break;
                }
            }

            final AttachableDefinitions.AttachableDefinition attachableDefinition = entry.getValue();
            for (Map.Entry<String, String> modelEntry : attachableDefinition.attachableData().geometries().entrySet()) {
                final BedrockGeometry bedrockGeometry = resourcePacksStorage.getModels().entityModels().get(modelEntry.getValue());
                if (bedrockGeometry == null) continue;
                if (!attachableDefinition.attachableData().textures().containsKey(modelEntry.getKey())) continue;

                final String javaTexturePath = "attachable_texture/" + StringUtil.makeIdentifierValueSafe(
                        attachableDefinition.attachableData().textures().get(modelEntry.getKey()).replace("textures/", ""));

                final List<ItemModelData> cubeConverterItemModels = Lists.newArrayList(FormatConverter.bedrockToJava("viabedrock:item/" +
                        javaTexturePath, bedrockGeometry));

                // It doesn't matter even if there is multiple models, it's item so only 1 model is supported.
                if (cubeConverterItemModels.size() < 1) continue;
                ItemModelData model = cubeConverterItemModels.get(0);
                if (model == null) continue;

                String json = JavaModelSerializer.serialize(model).toString();
                if (json == null || json.isEmpty()) continue;
                JsonObject object = GsonUtil.getGson().fromJson(json.trim(), JsonObject.class);

                // Scaling up the model...
                JsonObject display = new JsonObject();
                JsonArray scaling = new JsonArray();
                scaling.add(model.scale());
                scaling.add(model.scale());
                scaling.add(model.scale());

                JsonObject value = new JsonObject();
                value.add("scale", scaling);

                display.add("firstperson_righthand", value);
                display.add("firstperson_lefthand", value);
                display.add("thirdperson_righthand", value);
                display.add("thirdperson_lefthand", value);
                display.add("head", value);
                display.add("gui", value);
                display.add("ground", value);
                display.add("fixed", value);

                object.add("display", display);

                final String key = "attachable_" + entry.getKey() + "_" + modelEntry.getKey();
                final String javaModelName = StringUtil.makeIdentifierValueSafe(key);

                javaContent.putString("assets/viabedrock/models/attachable/" + javaModelName + ".json", object.toString());

                final int javaModelData = getCustomModelData(key);
                final JsonObject override = new JsonObject();
                override.addProperty("model", "viabedrock:attachable/" + javaModelName);
                final JsonObject predicate = new JsonObject();
                predicate.addProperty("custom_model_data", javaModelData);
                override.add("predicate", predicate);

                if (overridesMap.put(javaModelData, override) != null) {
                    throw new IllegalStateException("Duplicate custom model data: " + override);
                }
            }
        }

        if (!overridesMap.isEmpty()) {
            final JsonArray overrides = new JsonArray();
            overridesMap.values().forEach(overrides::add);

            final JsonObject attachableDefinition = new JsonObject();
            attachableDefinition.addProperty("parent", "minecraft:item/generated");
            attachableDefinition.add("overrides", overrides);
            final JsonObject layer0 = new JsonObject();
            layer0.addProperty("layer0", "minecraft:item/" + ITEM);
            attachableDefinition.add("textures", layer0);
            javaContent.putJson("assets/minecraft/models/item/" + ITEM + ".json", attachableDefinition);
        }
    }

    public static int getCustomModelData(final String key) {
        return Math.abs(key.hashCode() + 1); // 0 is used for the default model
    }

}
