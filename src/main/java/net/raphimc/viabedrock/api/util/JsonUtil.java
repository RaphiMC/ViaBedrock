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
package net.raphimc.viabedrock.api.util;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JsonUtil {

    public static <T extends JsonElement> T sort(final T element, final Comparator<String> comparator) {
        if (element == null) {
            return null;
        } else if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) array.set(i, sort(array.get(i), comparator));
            return (T) array;
        } else if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            final JsonObject sorted = new JsonObject();
            final List<String> keys = new ArrayList<>(object.keySet());
            keys.sort(comparator);
            for (String key : keys) sorted.add(key, sort(object.get(key), comparator));
            return (T) sorted;
        } else {
            return element;
        }
    }

}
