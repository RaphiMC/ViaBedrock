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

import com.viaversion.viaversion.api.minecraft.item.data.ItemModel;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.api.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.resourcepack.content.Content;
import net.raphimc.viabedrock.api.resourcepack.definition.TextureDefinitions;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.storage.ResourcePackStorage;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomItemTextureResourceRewriter extends ItemModelResourceRewriter {

    private static final String SUB_FOLDER = "item_textures";

    public static ItemModel getItemModel(final String itemTextureName) {
        return new ItemModel(Key.of("viabedrock", SUB_FOLDER + '/' + StringUtil.makeIdentifierValueSafe(itemTextureName)));
    }

    public CustomItemTextureResourceRewriter() {
        super(SUB_FOLDER);
    }

    @Override
    protected void apply(final ResourcePackStorage resourcePackStorage, final Content javaContent, final Set<ItemDefinition> javaItemDefinitions) {
        for (Map.Entry<String, List<TextureDefinitions.ItemTextureDefinition>> itemEntry : resourcePackStorage.getTextures().itemTextures().entrySet()) {
            final ItemDefinition javaItemDefinition = new ItemDefinition(itemEntry.getKey());
            for (int i = 0; i < itemEntry.getValue().size(); i++) {
                final TextureDefinitions.ItemTextureDefinition itemTextureDefinition = itemEntry.getValue().get(i);
                for (ResourcePack pack : resourcePackStorage.getPackStackTopToBottom()) {
                    final Content.LazyImage texture = pack.content().getShortnameImage(itemTextureDefinition.texturePath());
                    if (texture != null) {
                        javaContent.putPngImage("assets/viabedrock/textures/" + this.getJavaTexturePath(itemTextureDefinition.texturePath()) + ".png", texture);
                        break;
                    }
                }
                final JsonObject itemModel = new JsonObject();
                itemModel.addProperty("parent", "minecraft:item/generated");
                final JsonObject layer0 = new JsonObject();
                layer0.addProperty("layer0", "viabedrock:" + this.getJavaTexturePath(itemTextureDefinition.texturePath()));
                itemModel.add("textures", layer0);
                javaItemDefinition.modelDefinitions().put(String.valueOf(i), itemModel);
            }
            javaItemDefinitions.add(javaItemDefinition);
        }
    }

}
