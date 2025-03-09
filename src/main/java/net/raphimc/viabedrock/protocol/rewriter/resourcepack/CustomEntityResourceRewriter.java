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

import com.google.common.collect.Lists;
import net.raphimc.viabedrock.api.model.resourcepack.EntityDefinitions;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import org.cube.converter.converter.enums.RotationFixMode;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.model.impl.java.JavaItemModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomEntityResourceRewriter extends ItemModelResourceRewriter {

    public CustomEntityResourceRewriter() {
        super("entity", "entity");
    }

    @Override
    protected void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent, final Set<String> modelsList) {
        for (Map.Entry<String, EntityDefinitions.EntityDefinition> entityEntry : resourcePacksStorage.getEntities().entities().entrySet()) {
            for (String bedrockPath : entityEntry.getValue().entityData().getTextures().values()) {
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
            for (Map.Entry<String, String> modelEntry : entityDefinition.entityData().getGeometries().entrySet()) {
                final BedrockGeometryModel bedrockGeometry = resourcePacksStorage.getModels().entityModels().get(modelEntry.getValue());
                if (bedrockGeometry == null) continue;

                final String key = entityEntry.getKey() + "_" + modelEntry.getKey() + "_";

                if (!entityDefinition.entityData().getTextures().containsKey(modelEntry.getKey())) {
                    for (Map.Entry<String, String> textureEntry : entityDefinition.entityData().getTextures().entrySet()) {
                        this.putModelToPack(resourcePacksStorage, javaContent, modelsList, key + textureEntry.getKey(), bedrockGeometry, textureEntry.getValue());
                    }
                    continue;
                }

                this.putModelToPack(resourcePacksStorage, javaContent, modelsList, key + modelEntry.getKey(), bedrockGeometry, entityDefinition.entityData().getTextures().get(modelEntry.getKey()));
            }
        }
    }

    private void putModelToPack(final ResourcePacksStorage storage, final ResourcePack.Content content, final Set<String> modelsList, final String key, final BedrockGeometryModel geometry, final String texture) {
        final List<JavaItemModel> models = Lists.newArrayList(geometry.toJavaItemModel("viabedrock:" + this.getJavaTexturePath(texture), RotationFixMode.HACKY));
        storage.getConverterData().put("ce_" + key, models.size());
        for (int i = 0; i < models.size(); i++) {
            final JavaItemModel cubeConverterItemModel = models.get(i);
            storage.getConverterData().put("ce_" + key + "_" + i + "_scale", (float) cubeConverterItemModel.getScale());

            content.putString("assets/viabedrock/models/" + this.getJavaModelName(key + "_" + i) + ".json", cubeConverterItemModel.compile().toString());
            modelsList.add(key + "_" + i);
        }
    }
}