/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
import net.raphimc.viabedrock.protocol.model.ResourcePack;

public class ResourcePackRewriter {

    public static ResourcePack.Content bedrockToJava(final ResourcePack.Content bedrockContent) {
        final ResourcePack.Content javaContent = new ResourcePack.Content();

        javaContent.putJson("pack.mcmeta", createPackManifest());

        return javaContent;
    }

    private static JsonObject createPackManifest() {
        final JsonObject root = new JsonObject();
        final JsonObject pack = new JsonObject();
        root.add("pack", pack);
        pack.addProperty("pack_format", 13);
        pack.addProperty("description", "ViaBedrock Resource Pack");
        return root;
    }

}
