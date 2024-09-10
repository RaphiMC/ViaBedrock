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
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.api.model.resourcepack.EntityDefinitions;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import org.oryxel.cube.converter.FormatConverter;
import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.model.java.ItemModelData;
import org.oryxel.cube.parser.java.JavaModelSerializer;

import java.util.List;
import java.util.Map;

public class CustomEntityResourceRewriter extends ItemModelResourceRewriter {

    public static final String ITEM = "armor_stand";

    public CustomEntityResourceRewriter() {
        super(ITEM, "entity");
    }

    @Override
    protected void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent, final Map<Integer, JsonObject> overridesMap) {
        for (Map.Entry<String, EntityDefinitions.EntityDefinition> entityEntry : resourcePacksStorage.getEntities().entities().entrySet()) {
            for (String bedrockPath : entityEntry.getValue().entityData().textures().values()) {
                for (ResourcePack pack : resourcePacksStorage.getPackStackTopToBottom()) {
                    final ResourcePack.Content bedrockContent = pack.content();
                    final ResourcePack.Content.LazyImage texture = bedrockContent.getShortnameImage(bedrockPath);
                    if (texture != null) {
                        javaContent.putPngImage("assets/viabedrock/textures/" + this.getJavaTexturePath(bedrockPath) + ".png", texture);
                        break;
                    }
                }
            }

            final EntityDefinitions.EntityDefinition entityDefinition = entityEntry.getValue();
            for (Map.Entry<String, String> modelEntry : entityDefinition.entityData().geometries().entrySet()) {
                final BedrockGeometry bedrockGeometry = resourcePacksStorage.getModels().entityModels().get(modelEntry.getValue());
                if (bedrockGeometry == null) continue;
                if (!entityDefinition.entityData().textures().containsKey(modelEntry.getKey())) continue;

                final String javaTexturePath = this.getJavaTexturePath(entityDefinition.entityData().textures().get(modelEntry.getKey()));
                final List<ItemModelData> itemModels = Lists.newArrayList(FormatConverter.bedrockToJava("viabedrock:" + javaTexturePath, bedrockGeometry));
                final String key = entityEntry.getKey() + "_" + modelEntry.getKey();
                resourcePacksStorage.getConverterData().put("ce_" + key, itemModels.size());
                for (int i = 0; i < itemModels.size(); i++) {
                    final ItemModelData cubeConverterItemModel = itemModels.get(i);
                    resourcePacksStorage.getConverterData().put("ce_" + key + "_" + i + "_scale", (float) cubeConverterItemModel.scale());

                    javaContent.putString("assets/viabedrock/models/" + this.getJavaModelName(key + "_" + i) + ".json", JavaModelSerializer.serialize(cubeConverterItemModel).toString());
                    this.addOverride(overridesMap, key + "_" + i);
                }
            }
        }
    }

}
