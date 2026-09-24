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
package net.raphimc.viabedrock.tool.mapping;

import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.tool.ToolPaths;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Reads the data assets straight from {@code src/main/resources}, so the tools see edits without a Gradle resource sync.
 * <p>
 * This mirrors what {@link net.raphimc.viabedrock.protocol.data.BedrockMappingData} loads, but it never throws on a
 * problem. The whole point of these tools is to list the problems instead of stopping at the first one.
 */
public class MappingAssets {

    private final Map<String, JsonElement> jsonCache = new LinkedHashMap<>();
    private final Map<String, CompoundTag> nbtCache = new LinkedHashMap<>();

    public JsonObject json(final String file) {
        return this.jsonElement(file).getAsJsonObject();
    }

    public JsonArray jsonArray(final String file) {
        return this.jsonElement(file).getAsJsonArray();
    }

    public JsonElement jsonElement(final String file) {
        return this.jsonCache.computeIfAbsent(file, key -> {
            try {
                return JsonParser.parseString(Files.readString(this.path(key)));
            } catch (final IOException e) {
                throw new RuntimeException("Failed to read " + key, e);
            }
        });
    }

    public CompoundTag nbt(final String file) {
        return this.nbtCache.computeIfAbsent(file, key -> {
            try (InputStream inputStream = Files.newInputStream(this.path(key))) {
                return NBTIO.readTag(new DataInputStream(new GZIPInputStream(inputStream)), TagLimiter.noop(), true, CompoundTag.class);
            } catch (final IOException e) {
                throw new RuntimeException("Failed to read " + key, e);
            }
        });
    }

    public Path path(final String file) {
        return ToolPaths.ASSETS.resolve("data").resolve(file);
    }

    // Java side

    public JsonObject javaMappings() {
        return this.json("java/via_mappings.json");
    }

    public Set<String> javaNamespaced(final String key) {
        final Set<String> values = new LinkedHashSet<>();
        for (JsonElement element : this.javaMappings().getAsJsonArray(key)) {
            values.add(Key.namespaced(element.getAsString()));
        }
        return values;
    }

    public List<String> javaBlockStates() {
        final List<String> blockStates = new ArrayList<>();
        for (JsonElement element : this.javaMappings().getAsJsonArray("blockstates")) {
            blockStates.add(element.getAsString());
        }
        return blockStates;
    }

    public Set<String> javaEffects() {
        final Set<String> effects = new LinkedHashSet<>();
        for (JsonElement element : this.jsonArray("java/effects.json")) {
            effects.add(Key.namespaced(element.getAsString()));
        }
        return effects;
    }

    // Bedrock side

    public List<BedrockBlockState> bedrockBlockStates() {
        final ListTag<CompoundTag> tag = this.nbt("bedrock/block_palette.nbt").getListTag("blocks", CompoundTag.class);
        final List<BedrockBlockState> blockStates = new ArrayList<>(tag.size());
        for (CompoundTag blockStateTag : tag) {
            blockStates.add(BedrockBlockState.fromNbt(blockStateTag));
        }
        return blockStates;
    }

    public Set<String> bedrockEntities() {
        final Set<String> entities = new LinkedHashSet<>();
        for (CompoundTag tag : this.nbt("bedrock/entity_identifiers.nbt").getListTag("idlist", CompoundTag.class)) {
            entities.add(tag.getString("id"));
        }
        return entities;
    }

    public Set<String> bedrockItems() {
        final Set<String> items = new LinkedHashSet<>();
        for (JsonElement element : this.jsonArray("bedrock/runtime_item_states.json")) {
            items.add(element.getAsJsonObject().get("name").getAsString());
        }
        return items;
    }

    /**
     * Bedrock splits items by runtime id: everything up to {@code LAST_BLOCK_ITEM_ID} is a block item, the rest are meta items.
     */
    public Set<String> bedrockBlockItems(final int lastBlockItemId) {
        final Set<String> items = new LinkedHashSet<>();
        for (JsonElement element : this.jsonArray("bedrock/runtime_item_states.json")) {
            final JsonObject item = element.getAsJsonObject();
            if (item.get("id").getAsInt() <= lastBlockItemId) {
                items.add(item.get("name").getAsString());
            }
        }
        return items;
    }

    public Set<String> bedrockStrings(final String file) {
        final Set<String> values = new LinkedHashSet<>();
        for (JsonElement element : this.jsonArray(file)) {
            values.add(element.getAsString());
        }
        return values;
    }

    public Set<String> bedrockKeys(final String file) {
        return new LinkedHashSet<>(this.json(file).keySet());
    }

}
