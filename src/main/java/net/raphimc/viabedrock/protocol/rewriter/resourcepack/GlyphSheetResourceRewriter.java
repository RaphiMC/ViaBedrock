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
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.rewriter.ResourcePackRewriter;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.Locale;

// https://wiki.bedrock.dev/concepts/emojis
public class GlyphSheetResourceRewriter implements ResourcePackRewriter.Rewriter {

    private static final int GLYPHS_PER_ROW = 16;
    private static final int GLYPHS_PER_COLUMN = 16;

    @Override
    public void apply(final ResourcePacksStorage resourcePacksStorage, final ResourcePack.Content javaContent) {
        final JsonArray providers = new JsonArray();

        for (int i = 0; i < 0xFF; i++) {
            final String pageName = "glyph_" + String.format("%1$02X", i) + ".png";
            final String bedrockPath = "font/" + pageName;
            final String javaPath = "font/" + pageName.toLowerCase(Locale.ROOT);

            for (ResourcePack pack : resourcePacksStorage.getPackStackTopToBottom()) {
                final ResourcePack.Content bedrockContent = pack.content();
                if (!bedrockContent.contains(bedrockPath)) continue;

                javaContent.copyFrom(bedrockContent, bedrockPath, "assets/viabedrock/textures/" + javaPath);
                final ResourcePack.Content.LazyImage image = bedrockContent.getImage(bedrockPath);
                final int glyphHeight = image.getImage().getHeight() / GLYPHS_PER_COLUMN;

                final JsonObject glyphPage = new JsonObject();
                providers.add(glyphPage);
                glyphPage.addProperty("type", "bitmap");
                glyphPage.addProperty("file", "viabedrock:" + javaPath);
                glyphPage.addProperty("ascent", glyphHeight / 2 + 5);
                glyphPage.addProperty("height", glyphHeight);
                final JsonArray chars = new JsonArray();
                glyphPage.add("chars", chars);
                for (int c = 0; c < GLYPHS_PER_COLUMN; c++) {
                    final StringBuilder row = new StringBuilder();
                    for (int r = 0; r < GLYPHS_PER_ROW; r++) {
                        final int idx = c * GLYPHS_PER_COLUMN + r;
                        row.append((char) (i << 8 | idx));
                    }
                    chars.add(row.toString());
                }
                break;
            }
        }

        if (!providers.isEmpty()) {
            final JsonObject defaultJson = new JsonObject();
            defaultJson.add("providers", providers);
            javaContent.putJson("assets/minecraft/font/default.json", defaultJson);
        }
    }

}
