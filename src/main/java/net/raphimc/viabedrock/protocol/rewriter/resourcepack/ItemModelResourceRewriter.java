/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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

import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.api.resourcepack.content.InMemoryContent;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.rewriter.ResourcePackRewriter;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemModelResourceRewriter implements ResourcePackRewriter.Rewriter {

    public static CustomModelData1_21_4 getCustomModelData(final String key) {
        return new CustomModelData1_21_4(new float[0], new boolean[0], new String[]{key}, new int[0]);
    }

    private final String subFolder;

    public ItemModelResourceRewriter(final String subFolder) {
        this.subFolder = subFolder;
    }

    protected String getJavaTexturePath(final String bedrockPath) {
        return "item/" + this.subFolder + '/' + StringUtil.makeIdentifierValueSafe(bedrockPath.replace("textures/", ""));
    }

    protected class ItemModelContent extends InMemoryContent {

        private final Map<String, String> modelDefinitions = new HashMap<>();
        private final String itemPath;

        protected ItemModelContent(final String itemName) {
            this.itemPath = ItemModelResourceRewriter.this.subFolder + '/' + StringUtil.makeIdentifierValueSafe(itemName);
        }

        protected void putModel(final String name, final JsonObject json) {
            final String modelPath = this.itemPath + '/' + StringUtil.makeIdentifierValueSafe(name);
            this.modelDefinitions.put(name, modelPath);
            this.putJson("assets/viabedrock/models/" + modelPath + ".json", json);
        }

        protected void generateItemDefinition() {
            final JsonArray modelCases = new JsonArray();
            for (Map.Entry<String, String> modelDefinition : this.modelDefinitions.entrySet()) {
                final JsonObject model = new JsonObject();
                model.addProperty("type", "minecraft:model");
                model.addProperty("model", "viabedrock:" + modelDefinition.getValue());

                final JsonObject caseObj = new JsonObject();
                caseObj.addProperty("when", modelDefinition.getKey());
                caseObj.add("model", model);
                modelCases.add(caseObj);
            }

            final JsonObject model = new JsonObject();
            model.addProperty("type", "minecraft:select");
            model.addProperty("property", "minecraft:custom_model_data");
            model.add("cases", modelCases);
            final JsonObject itemDefinitionObj = new JsonObject();
            itemDefinitionObj.add("model", model);
            this.putJson("assets/viabedrock/items/" + this.itemPath + ".json", itemDefinitionObj);
        }

    }

}
