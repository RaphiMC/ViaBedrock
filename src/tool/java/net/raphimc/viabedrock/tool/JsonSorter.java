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
package net.raphimc.viabedrock.tool;

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.util.GsonUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Sorts data asset json files in place, so hand edited files keep the same key order as generated ones.
 * <p>
 * Example: {@code --file=data/custom/item_mappings.json --deep}
 */
public final class JsonSorter {

    public static void main(final String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final List<String> files = toolArgs.list("file");
        if (files.isEmpty()) {
            throw new IllegalArgumentException("Missing required argument --file. Pass one or more comma separated files, either absolute or relative to " + ToolPaths.ASSETS);
        }
        final boolean deep = toolArgs.flag("deep");

        for (String file : files) {
            final Path path = resolve(file);
            JsonElement json = JsonParser.parseString(Files.readString(path));
            json = deep ? GsonUtil.sort(json) : sortTopLevel(path, json);
            ToolPaths.writeJson(path, json, true);
        }
    }

    private static JsonElement sortTopLevel(final Path path, final JsonElement json) {
        if (json instanceof JsonObject jsonObject) {
            final Map<String, JsonElement> sortedJson = new TreeMap<>();
            jsonObject.entrySet().forEach(entry -> sortedJson.put(entry.getKey(), entry.getValue()));
            jsonObject.entrySet().clear();
            sortedJson.forEach(jsonObject::add);
            return jsonObject;
        }
        // Arrays like vanilla_resource_packs.json store a load order, sorting them would change their meaning
        throw new IllegalArgumentException(ToolPaths.describe(path) + " is not a json object. Only the key order of objects is sorted.");
    }

    private static Path resolve(final String file) {
        final Path path = Path.of(file);
        if (Files.isRegularFile(path)) {
            return path;
        }
        final Path assetPath = ToolPaths.ASSETS.resolve(file);
        if (Files.isRegularFile(assetPath)) {
            return assetPath;
        }
        throw new IllegalArgumentException("Could not find '" + file + "'. It is neither a file nor a path inside " + ToolPaths.ASSETS);
    }

    private JsonSorter() {
    }

}
