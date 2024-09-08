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
package net.raphimc.viabedrock.protocol.rewriter;

import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.CustomAttachableResourceRewriter;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.CustomEntityResourceRewriter;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.CustomItemTextureResourceRewriter;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.GlyphSheetResourceRewriter;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ResourcePackRewriter {

    public static ResourcePack.Content bedrockToJava(final ResourcePacksStorage resourcePacksStorage) {
        final ResourcePack.Content javaContent = new ResourcePack.Content();

        GlyphSheetResourceRewriter.apply(resourcePacksStorage, javaContent);
        CustomItemTextureResourceRewriter.apply(resourcePacksStorage, javaContent);
        CustomAttachableResourceRewriter.apply(resourcePacksStorage, javaContent);
        CustomEntityResourceRewriter.apply(resourcePacksStorage, javaContent);

        javaContent.putJson("pack.mcmeta", createPackManifest());

        return javaContent;
    }

    private static JsonObject createPackManifest() {
        final JsonObject root = new JsonObject();
        final JsonObject pack = new JsonObject();
        root.add("pack", pack);
        pack.addProperty("pack_format", ProtocolConstants.JAVA_PACK_VERSION);
        pack.addProperty("description", "ViaBedrock Resource Pack");
        return root;
    }

}
