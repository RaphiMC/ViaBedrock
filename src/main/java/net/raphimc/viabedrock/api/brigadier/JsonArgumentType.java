/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.api.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.viaversion.viaversion.libs.gson.stream.JsonReader;
import com.viaversion.viaversion.util.GsonUtil;

import java.lang.reflect.Field;

public class JsonArgumentType implements ArgumentType<Object> {

    private static final Field JSON_READER_POS;
    private static final Field JSON_READER_LINE_START;
    private static final SimpleCommandExceptionType INVALID_JSON_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Invalid json"));

    static {
        try {
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            JSON_READER_POS = field;
        } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var1);
        }
        try {
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            JSON_READER_LINE_START = field;
        } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var1);
        }
    }

    public static JsonArgumentType json() {
        return new JsonArgumentType();
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        try (JsonReader r = new JsonReader(new java.io.StringReader(reader.getRemaining()))) {
            GsonUtil.getGson().fromJson(r, JsonObject.class);
            reader.setCursor(reader.getCursor() + this.getPosition(r));
            return null;
        } catch (Throwable t) {
            throw INVALID_JSON_EXCEPTION.createWithContext(reader);
        }
    }

    private int getPosition(JsonReader reader) {
        try {
            return JSON_READER_POS.getInt(reader) - JSON_READER_LINE_START.getInt(reader);
        } catch (IllegalAccessException var2) {
            throw new IllegalStateException("Couldn't read position of JsonReader", var2);
        }
    }

}
