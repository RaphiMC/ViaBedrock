/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
import org.cube.converter.converter.enums.RotationType;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.model.impl.java.JavaItemModel;

import java.util.Map;
import java.util.Set;

public class CustomAttachableResourceRewriter extends ItemModelResourceRewriter {

    public CustomAttachableResourceRewriter() {
        super("attachable", "attachable");
    }

    @Override
    protected void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent, final Set<String> modelsList) {
        for (Map.Entry<String, AttachableDefinitions.AttachableDefinition> attachableEntry : resourcePacksStorage.getAttachables().attachables().entrySet()) {
            for (String bedrockPath : attachableEntry.getValue().attachableData().getTextures().values()) {
                for (ResourcePack pack : resourcePacksStorage.getPackStackTopToBottom()) {
                    final ResourcePack.Content bedrockContent = pack.content();
                    final ResourcePack.Content.LazyImage texture = bedrockContent.getShortnameImage(bedrockPath);
                    if (texture != null) {
                        javaContent.putPngImage("assets/viabedrock/textures/" + this.getJavaTexturePath(bedrockPath) + ".png", texture);
                        break;
                    }
                }
            }

            final AttachableDefinitions.AttachableDefinition attachableDefinition = attachableEntry.getValue();
            for (Map.Entry<String, String> modelEntry : attachableDefinition.attachableData().getGeometries().entrySet()) {
                final BedrockGeometryModel bedrockGeometry = resourcePacksStorage.getModels().entityModels().get(modelEntry.getValue());
                if (bedrockGeometry == null) continue;
                if (!attachableDefinition.attachableData().getTextures().containsKey(modelEntry.getKey())) continue;

                final String javaTexturePath = this.getJavaTexturePath(attachableDefinition.attachableData().getTextures().get(modelEntry.getKey()));
                final JavaItemModel itemModelData = bedrockGeometry.toJavaItemModel("viabedrock:" + javaTexturePath, RotationType.POST_1_21_6);
                final JsonObject itemModel = GsonUtil.getGson().fromJson(itemModelData.compile().toString(), JsonObject.class);

                final JsonObject display = new JsonObject();
                final JsonArray scaling = new JsonArray();
                scaling.add(itemModelData.getScale());
                scaling.add(itemModelData.getScale());
                scaling.add(itemModelData.getScale());

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
                modelsList.add(key);
            }
        }
    }

}
