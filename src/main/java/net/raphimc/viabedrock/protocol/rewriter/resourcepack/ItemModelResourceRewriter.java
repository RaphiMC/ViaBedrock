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

import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.rewriter.ResourcePackRewriter;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.HashSet;
import java.util.Set;

public abstract class ItemModelResourceRewriter implements ResourcePackRewriter.Rewriter {

    public static CustomModelData1_21_4 getCustomModelData(final String key) {
        return new CustomModelData1_21_4(new float[0], new boolean[0], new String[]{key}, new int[0]);
    }

    private final String name;
    private final String subFolder;

    public ItemModelResourceRewriter(final String name, final String subFolder) {
        this.name = name;
        this.subFolder = subFolder;
    }

    @Override
    public final void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent) {
        final Set<String> modelsList = new HashSet<>();
        this.apply(resourcePacksStorage, javaContent, modelsList);
        if (!modelsList.isEmpty()) {
            final JsonArray cases = new JsonArray();
            for (String modelKey : modelsList) {
                final JsonObject caseObj = new JsonObject();
                caseObj.addProperty("when", modelKey);

                final JsonObject model = new JsonObject();
                model.addProperty("type", "minecraft:model");
                model.addProperty("model", "viabedrock:" + this.getJavaModelName(modelKey));
                caseObj.add("model", model);

                cases.add(caseObj);
            }

            final JsonObject itemDefinition = new JsonObject();
            final JsonObject model = new JsonObject();
            model.addProperty("type", "minecraft:select");
            model.addProperty("property", "minecraft:custom_model_data");
            model.add("cases", cases);
            itemDefinition.add("model", model);
            javaContent.putJson("assets/viabedrock/items/" + this.name + ".json", itemDefinition);
        }
    }

    protected abstract void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent, final Set<String> modelsList);

    protected String getJavaModelName(final String bedrockName) {
        return this.subFolder + '/' + StringUtil.makeIdentifierValueSafe(bedrockName);
    }

    protected String getJavaTexturePath(final String bedrockPath) {
        return "item/" + this.subFolder + '/' + StringUtil.makeIdentifierValueSafe(bedrockPath.replace("textures/", ""));
    }

}
