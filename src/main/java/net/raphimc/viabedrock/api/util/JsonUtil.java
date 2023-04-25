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
package net.raphimc.viabedrock.api.util;

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonParser;
import net.lenni0451.mcstructs.core.TextFormatting;
import net.lenni0451.mcstructs.text.serializer.LegacyStringDeserializer;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;
import net.lenni0451.mcstructs_bedrock.text.BedrockTextFormatting;

import java.util.Optional;

public class JsonUtil {

    public static String textToJson(final String text) {
        return TextComponentSerializer.V1_19_4.serialize(LegacyStringDeserializer.parse(text, TextFormatting.COLOR_CHAR, c -> Optional.ofNullable(BedrockTextFormatting.getByCode(c)).map(f -> {
            if (f.isColor()) {
                return new TextFormatting(f.getRgbValue());
            } else if (f.equals(BedrockTextFormatting.OBFUSCATED)) {
                return TextFormatting.OBFUSCATED;
            } else if (f.equals(BedrockTextFormatting.BOLD)) {
                return TextFormatting.BOLD;
            } else if (f.equals(BedrockTextFormatting.ITALIC)) {
                return TextFormatting.ITALIC;
            } else if (f.equals(BedrockTextFormatting.RESET)) {
                return TextFormatting.RESET;
            } else {
                throw new IllegalArgumentException("Unknown formatting: " + f);
            }
        }).orElse(null)));
    }

    public static JsonElement textToComponent(final String text) {
        return JsonParser.parseString(textToJson(text));
    }

}
