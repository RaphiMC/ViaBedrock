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
package net.raphimc.viabedrock.api.model.resourcepack;

import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// https://wiki.bedrock.dev/concepts/text-and-translations.html
public class TextDefinitions {

    private final Map<String, String> translations;

    public TextDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        this.translations = new HashMap<>();
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            if (pack.content().contains("texts/en_US.lang")) {
                this.translations.putAll(pack.content().getLang("texts/en_US.lang"));
            }
        }
    }

    public String translate(final String text) {
        return BedrockTranslator.translate(text, this.lookup(), new Object[0]);
    }

    public String get(final String key) {
        return this.translations.getOrDefault(key, key);
    }

    public Function<String, String> lookup() {
        return this::get;
    }

}
