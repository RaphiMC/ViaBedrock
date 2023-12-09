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
package net.raphimc.viabedrock.api.util.converter;

import com.google.gson.*;

import java.util.Map;

public class JsonConverter {

    public static com.viaversion.viaversion.libs.gson.JsonElement gsonToVia(final JsonElement element) {
        if (element == null) {
            return null;
        } else if (element.isJsonNull()) {
            return com.viaversion.viaversion.libs.gson.JsonNull.INSTANCE;
        } else if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return new com.viaversion.viaversion.libs.gson.JsonPrimitive(primitive.getAsBoolean());
            } else if (primitive.isNumber()) {
                return new com.viaversion.viaversion.libs.gson.JsonPrimitive(primitive.getAsNumber());
            } else if (primitive.isString()) {
                return new com.viaversion.viaversion.libs.gson.JsonPrimitive(primitive.getAsString());
            } else {
                throw new IllegalArgumentException("Unknown json primitive type: " + primitive);
            }
        } else if (element.isJsonArray()) {
            final com.viaversion.viaversion.libs.gson.JsonArray array = new com.viaversion.viaversion.libs.gson.JsonArray();
            for (JsonElement e : element.getAsJsonArray()) {
                array.add(gsonToVia(e));
            }
            return array;
        } else if (element.isJsonObject()) {
            final com.viaversion.viaversion.libs.gson.JsonObject object = new com.viaversion.viaversion.libs.gson.JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                object.add(entry.getKey(), gsonToVia(entry.getValue()));
            }
            return object;
        } else {
            throw new IllegalArgumentException("Unknown json element type: " + element.getClass().getName());
        }
    }

    public static JsonElement viaToGson(final com.viaversion.viaversion.libs.gson.JsonElement element) {
        if (element == null) {
            return null;
        } else if (element.isJsonNull()) {
            return JsonNull.INSTANCE;
        } else if (element.isJsonPrimitive()) {
            final com.viaversion.viaversion.libs.gson.JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return new JsonPrimitive(primitive.getAsBoolean());
            } else if (primitive.isNumber()) {
                return new JsonPrimitive(primitive.getAsNumber());
            } else if (primitive.isString()) {
                return new JsonPrimitive(primitive.getAsString());
            } else {
                throw new IllegalArgumentException("Unknown json primitive type: " + primitive);
            }
        } else if (element.isJsonArray()) {
            final JsonArray array = new JsonArray();
            for (com.viaversion.viaversion.libs.gson.JsonElement e : element.getAsJsonArray()) {
                array.add(viaToGson(e));
            }
            return array;
        } else if (element.isJsonObject()) {
            final JsonObject object = new JsonObject();
            for (Map.Entry<String, com.viaversion.viaversion.libs.gson.JsonElement> entry : element.getAsJsonObject().entrySet()) {
                object.add(entry.getKey(), viaToGson(entry.getValue()));
            }
            return object;
        } else {
            throw new IllegalArgumentException("Unknown json element type: " + element.getClass().getName());
        }
    }

}
