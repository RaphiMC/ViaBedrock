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

import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.rewriter.ResourcePackRewriter;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.Map;
import java.util.TreeMap;

public abstract class ItemModelResourceRewriter implements ResourcePackRewriter.Rewriter {

    public static CustomModelData1_21_4 getCustomModelData(final String key) {
        final int value = Math.abs(key.hashCode() + 1); // 0 is used for the default model
        return new CustomModelData1_21_4(new float[]{value}, new boolean[0], new String[0], new int[0]);
    }

    private final String item;
    private final String subFolder;

    public ItemModelResourceRewriter(final String item, final String subFolder) {
        this.item = item;
        this.subFolder = subFolder;
    }

    @Override
    public final void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent) {
        final Map<Integer, JsonObject> overridesMap = new TreeMap<>();
        this.apply(resourcePacksStorage, javaContent, overridesMap);
        if (!overridesMap.isEmpty()) {
            final JsonArray overrides = new JsonArray();
            overridesMap.values().forEach(overrides::add);

            final JsonObject itemDefinition = new JsonObject();
            itemDefinition.addProperty("parent", "minecraft:item/generated");
            itemDefinition.add("overrides", overrides);
            final JsonObject layer0 = new JsonObject();
            layer0.addProperty("layer0", "minecraft:item/" + this.item);
            itemDefinition.add("textures", layer0);
            javaContent.putJson("assets/minecraft/models/item/" + this.item + ".json", itemDefinition);
        }
    }

    protected abstract void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent, final Map<Integer, JsonObject> overridesMap);

    protected void addOverride(final Map<Integer, JsonObject> overridesMap, final String javaModelKey) {
        final String javaModelName = getJavaModelName(javaModelKey);
        //final int javaModelData = getCustomModelData(javaModelKey);
        // TODO: Update: Fix this (Broken in 1.21.4 update)
        final int javaModelData = 0;

        final JsonObject override = new JsonObject();
        override.addProperty("model", "viabedrock:" + javaModelName);
        final JsonObject predicate = new JsonObject();
        predicate.addProperty("custom_model_data", javaModelData);
        override.add("predicate", predicate);
        if (overridesMap.put(javaModelData, override) != null) {
            throw new IllegalStateException("Duplicate custom model data: " + override);
        }
    }

    protected String getJavaModelName(final String bedrockName) {
        return this.subFolder + '/' + StringUtil.makeIdentifierValueSafe(bedrockName);
    }

    protected String getJavaTexturePath(final String bedrockPath) {
        return "item/" + this.subFolder + '/' + StringUtil.makeIdentifierValueSafe(bedrockPath.replace("textures/", ""));
    }

}
