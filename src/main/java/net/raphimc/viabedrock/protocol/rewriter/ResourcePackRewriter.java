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
package net.raphimc.viabedrock.protocol.rewriter;

import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.api.resourcepack.content.Content;
import net.raphimc.viabedrock.api.resourcepack.content.InMemoryContent;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.CustomAttachableResourceRewriter;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.CustomEntityResourceRewriter;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.CustomItemTextureResourceRewriter;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.GlyphSheetResourceRewriter;
import net.raphimc.viabedrock.protocol.storage.ResourcePackStorage;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackRewriter {

    private static final List<Rewriter> REWRITERS = new ArrayList<>();

    static {
        REWRITERS.add(new GlyphSheetResourceRewriter());
        REWRITERS.add(new CustomItemTextureResourceRewriter());
        REWRITERS.add(new CustomAttachableResourceRewriter());
        REWRITERS.add(new CustomEntityResourceRewriter());
    }

    public static Content bedrockToJava(final ResourcePackStorage resourcePackStorage) {
        final Content javaContent = new InMemoryContent();
        for (Rewriter rewriter : REWRITERS) {
            rewriter.apply(resourcePackStorage, javaContent);
        }
        javaContent.putJson("pack.mcmeta", createPackManifest());
        return javaContent;
    }

    private static JsonObject createPackManifest() {
        final JsonObject pack = new JsonObject();
        pack.addProperty("description", "ViaBedrock Resource Pack");
        pack.addProperty("min_format", ProtocolConstants.JAVA_PACK_VERSION);
        pack.addProperty("max_format", ProtocolConstants.JAVA_PACK_VERSION);
        final JsonObject root = new JsonObject();
        root.add("pack", pack);
        return root;
    }

    public interface Rewriter {

        void apply(final ResourcePackStorage resourcePackStorage, final Content javaContent);

    }

}
