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

import com.viaversion.viaversion.libs.gson.Gson;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;

public class JsonSorter {

    public static void main(String[] args) throws Throwable {
        final byte[] data = JsonSorter.class.getResourceAsStream("/assets/viabedrock/data/custom/particle_mappings.json").readAllBytes();
        JsonObject obj = JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();

        // Sort top level keys
        final Map<String, JsonElement> sortedJson = new TreeMap<>();
        obj.entrySet().forEach(entry -> sortedJson.put(entry.getKey(), entry.getValue()));
        obj.entrySet().clear();
        sortedJson.forEach(obj::add);

        // Deep sort
        // obj = (JsonObject) GsonUtil.sort(obj);

        final String json = new Gson().newBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create().toJson(obj);
        Files.writeString(new File("sorted_json.json").toPath(), json);
    }

}
