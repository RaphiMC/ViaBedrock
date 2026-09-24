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

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.tool.ToolPaths;

import java.io.IOException;
import java.util.*;

/**
 * Adds entries which can be derived from the current Bedrock palette and Java registry. Conflicts need a manual review.
 */
public final class DerivedMappingDataUpdater {

    private DerivedMappingDataUpdater() {
    }

    public static void main(final String[] args) throws IOException {
        final MappingAssets assets = new MappingAssets();
        final JsonObject blockTags = assets.json("custom/block_tags.json");
        final JsonObject pottedStates = assets.json("custom/potted_blockstates.json");

        final int addedTags = addBlockTags(blockTags, DerivedMappingRules.blockTags(assets));
        final int addedPotted = addStringMappings(pottedStates, DerivedMappingRules.pottedBlockStates(assets));

        if (addedTags > 0) {
            ToolPaths.writeJson(ToolPaths.CUSTOM_DATA.resolve("block_tags.json"), GsonUtil.sort(blockTags));
        }
        if (addedPotted > 0) {
            ToolPaths.writeJson(ToolPaths.CUSTOM_DATA.resolve("potted_blockstates.json"), GsonUtil.sort(pottedStates));
        }
        System.out.println("Added " + addedTags + " block tags and " + addedPotted + " potted states");
    }

    private static int addBlockTags(final JsonObject blockTags, final Map<String, String> expected) {
        final Map<String, Set<String>> members = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : blockTags.entrySet()) {
            final Set<String> values = new LinkedHashSet<>();
            for (JsonElement member : entry.getValue().getAsJsonArray()) {
                values.add(member.getAsString());
            }
            members.put(entry.getKey(), values);
        }

        int added = 0;
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            for (Map.Entry<String, Set<String>> group : members.entrySet()) {
                if (!group.getKey().equals(entry.getValue()) && group.getValue().contains(entry.getKey())) {
                    throw new IllegalStateException(entry.getKey() + " is tagged as " + group.getKey() + ", expected " + entry.getValue());
                }
            }
            if (members.computeIfAbsent(entry.getValue(), key -> new LinkedHashSet<>()).add(entry.getKey())) {
                added++;
            }
        }

        if (added > 0) {
            for (String tag : List.of("hanging_sign", "shelf", "sign")) {
                final List<String> sorted = new ArrayList<>(members.get(tag));
                sorted.sort(String::compareTo);
                final JsonArray values = new JsonArray();
                sorted.forEach(values::add);
                blockTags.add(tag, values);
            }
        }
        return added;
    }

    private static int addStringMappings(final JsonObject mappings, final Map<String, String> expected) {
        int added = 0;
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            if (mappings.has(entry.getKey())) {
                if (!mappings.get(entry.getKey()).getAsString().equals(entry.getValue())) {
                    throw new IllegalStateException(entry.getKey() + " maps to " + mappings.get(entry.getKey()) + ", expected " + entry.getValue());
                }
            } else {
                mappings.addProperty(entry.getKey(), entry.getValue());
                added++;
            }
        }
        return added;
    }

}
