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
import com.viaversion.viaversion.libs.gson.JsonPrimitive;

import java.util.*;

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

    public static void merge(final JsonObject target, final JsonObject source) {
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            final JsonElement targetElement = target.get(entry.getKey());
            if (targetElement == null) {
                target.add(entry.getKey(), entry.getValue().deepCopy());
            } else if (targetElement.isJsonObject() && entry.getValue().isJsonObject()) {
                merge(targetElement.getAsJsonObject(), entry.getValue().getAsJsonObject());
            }
        }
    }

    public static Object getValue(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isNumber()) {
                return primitive.getAsNumber();
            } else {
                return primitive.getAsString();
            }
        } else if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final List<Object> list = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) list.add(getValue(array.get(i)));
            return list;
        } else if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            final Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) map.put(entry.getKey(), getValue(entry.getValue()));
            return map;
        } else {
            return null;
        }
    }

}
