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
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.api.model.resourcepack.AttachableDefinitions;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import org.oryxel.cube.converter.FormatConverter;
import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.model.java.ItemModelData;
import org.oryxel.cube.parser.java.JavaModelSerializer;

import java.awt.image.BufferedImage;
import java.util.Map;

public class CustomAttachableResourceRewriter extends ItemModelResourceRewriter {

    public static final String ITEM = "leather";

    public CustomAttachableResourceRewriter() {
        super(ITEM, "attachable");
    }

    @Override
    protected void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent, final Map<Integer, JsonObject> overridesMap) {
        for (Map.Entry<String, AttachableDefinitions.AttachableDefinition> attachableEntry : resourcePacksStorage.getAttachables().attachables().entrySet()) {
            for (String bedrockPath : attachableEntry.getValue().attachableData().textures().values()) {
                for (ResourcePack pack : resourcePacksStorage.getPackStackTopToBottom()) {
                    final ResourcePack.Content bedrockContent = pack.content();
                    final BufferedImage texture = bedrockContent.getShortnameImage(bedrockPath);
                    if (texture != null) {
                        javaContent.putImage("assets/viabedrock/textures/" + this.getJavaTexturePath(bedrockPath) + ".png", texture);
                        break;
                    }
                }
            }

            final AttachableDefinitions.AttachableDefinition attachableDefinition = attachableEntry.getValue();
            for (Map.Entry<String, String> modelEntry : attachableDefinition.attachableData().geometries().entrySet()) {
                final BedrockGeometry bedrockGeometry = resourcePacksStorage.getModels().entityModels().get(modelEntry.getValue());
                if (bedrockGeometry == null) continue;
                if (!attachableDefinition.attachableData().textures().containsKey(modelEntry.getKey())) continue;

                final String javaTexturePath = this.getJavaTexturePath(attachableDefinition.attachableData().textures().get(modelEntry.getKey()));
                final ItemModelData itemModelData = FormatConverter.bedrockToJava("viabedrock:" + javaTexturePath, bedrockGeometry);
                final JsonObject itemModel = GsonUtil.getGson().fromJson(JavaModelSerializer.serialize(itemModelData).toString(), JsonObject.class);

                final JsonObject display = new JsonObject();
                final JsonArray scaling = new JsonArray();
                scaling.add(itemModelData.scale());
                scaling.add(itemModelData.scale());
                scaling.add(itemModelData.scale());

                final JsonObject value = new JsonObject();
                value.add("scale", scaling);

                display.add("firstperson_righthand", value);
                display.add("firstperson_lefthand", value);
                display.add("thirdperson_righthand", value);
                display.add("thirdperson_lefthand", value);
                display.add("head", value);
                display.add("gui", value);
                display.add("ground", value);
                display.add("fixed", value);

                itemModel.add("display", display);

                final String key = attachableEntry.getKey() + "_" + modelEntry.getKey();
                resourcePacksStorage.getConverterData().put("ca_" + key, true);
                javaContent.putJson("assets/viabedrock/models/" + this.getJavaModelName(key) + ".json", itemModel);
                this.addOverride(overridesMap, key);
            }
        }
    }

}
