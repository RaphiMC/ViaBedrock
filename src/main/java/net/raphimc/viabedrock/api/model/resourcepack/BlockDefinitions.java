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

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

// https://wiki.bedrock.dev/blocks/blocks-intro.html
public class BlockDefinitions {

    private final Map<String, BlockDefinition> blocks = new HashMap<>();

    public BlockDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            if (pack.content().contains("blocks.json")) {
                try {
                    final JsonObject blocks = pack.content().getJson("blocks.json");
                    for (Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                        if (entry.getKey().equals("format_version")) continue;
                        final JsonObject block = entry.getValue().getAsJsonObject();
                        final String sound = block.has("sound") ? block.get("sound").getAsString() : null;
                        this.blocks.put(Key.namespaced(entry.getKey()), new BlockDefinition(Key.namespaced(entry.getKey()), sound));
                    }
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse blocks.json in pack " + pack.packId(), e);
                }
            }
        }
    }

    public BlockDefinition get(final String identifier) {
        return this.blocks.get(identifier);
    }

    public Map<String, BlockDefinition> blocks() {
        return Collections.unmodifiableMap(this.blocks);
    }

    public record BlockDefinition(String identifier, String sound) {
    }

}
