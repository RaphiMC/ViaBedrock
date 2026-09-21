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

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.tool.ToolArgs;
import net.raphimc.viabedrock.tool.ToolPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Merges the reviewed proposals back into the data assets.
 * <p>
 * Proposals are read from {@code run/mapping-proposals}, so anything edited or deleted there is respected. Mappings
 * for block states bedrock no longer has are dropped unless {@code --keep-stale} is passed. Use {@code --category}
 * to apply only the block states or only the item fixes.
 */
public class MappingProposalApplier {

    public static void main(String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final boolean dryRun = toolArgs.flag("dry-run");
        final boolean keepStale = toolArgs.flag("keep-stale");
        final List<String> categories = toolArgs.list("category").isEmpty()
                ? List.of(MappingAnalysis.BLOCK_STATES, MappingAnalysis.ITEMS)
                : toolArgs.list("category");

        if (categories.contains(MappingAnalysis.ITEMS)) {
            applyItemFixes(dryRun);
        }
        if (!categories.contains(MappingAnalysis.BLOCK_STATES)) {
            return;
        }

        final Path mappingsFile = ToolPaths.CUSTOM_DATA.resolve("blockstate_mappings.json");
        final JsonObject mappings = JsonParser.parseString(Files.readString(mappingsFile)).getAsJsonObject();
        final int before = mappings.size();

        final Map<String, String> proposals = MappingProposer.readProposals(MappingProposer.OUTPUT_DIR.resolve("block_states.json"));
        int added = 0;
        int replaced = 0;
        for (Map.Entry<String, String> proposal : proposals.entrySet()) {
            if (mappings.has(proposal.getKey())) {
                if (!mappings.get(proposal.getKey()).getAsString().equals(proposal.getValue())) {
                    replaced++;
                }
            } else {
                added++;
            }
            mappings.addProperty(proposal.getKey(), proposal.getValue());
        }

        int removed = 0;
        if (!keepStale) {
            final List<String> stale = GsonUtil.getGson().fromJson(Files.readString(MappingProposer.OUTPUT_DIR.resolve("block_states_stale.json")), List.class);
            for (Object key : stale) {
                if (mappings.remove((String) key) != null) {
                    removed++;
                }
            }
        }

        System.out.println();
        System.out.println("added    " + added);
        System.out.println("replaced " + replaced);
        System.out.println("removed  " + removed + " stale mappings");
        System.out.println("entries  " + before + " -> " + mappings.size());

        if (dryRun) {
            System.out.println();
            System.out.println("--dry-run given, nothing written");
            return;
        }

        final JsonElement sorted = GsonUtil.sort(mappings);
        ToolPaths.writeJson(mappingsFile, sorted);
    }

    /**
     * The item fixes are keyed by the bedrock item and the place the java_id sits.
     * <p>
     * They are applied to the text of the file rather than by reading and writing it back as json, because parts of
     * it are hand formatted on one line and a round trip would reflow them. This way the diff is the changed java_ids
     * and nothing else.
     */
    private static void applyItemFixes(final boolean dryRun) throws Exception {
        final Path itemsFile = ToolPaths.CUSTOM_DATA.resolve("item_mappings.json");
        final Path fixesFile = MappingProposer.OUTPUT_DIR.resolve("item_fixes.json");
        if (!Files.exists(fixesFile)) {
            System.out.println("No item fixes at " + ToolPaths.describe(fixesFile) + ", run ./gradlew reportMappingMistakes first");
            return;
        }

        String content = Files.readString(itemsFile);
        final Map<String, String> fixes = MappingProposer.readProposals(fixesFile);
        final JsonObject mappings = JsonParser.parseString(content).getAsJsonObject();

        int applied = 0;
        for (Map.Entry<String, String> fix : fixes.entrySet()) {
            final String[] parts = fix.getKey().split(" ", 2);
            final String[] path = parts[1].split("/", 2);
            final JsonObject definition = mappings.getAsJsonObject(parts[0]);
            if (definition == null) {
                throw new IllegalStateException("Unknown bedrock item: " + parts[0]);
            }
            final JsonObject mapping = definition.getAsJsonObject(path[0]).getAsJsonObject(path[1]);
            if (mapping == null) {
                throw new IllegalStateException("Unknown item mapping: " + fix.getKey());
            }
            content = replaceJavaId(content, parts[0], mapping.get("java_id").getAsString(), fix.getValue());
            applied++;
        }

        System.out.println();
        System.out.println("item java_ids changed " + applied);
        if (!dryRun) {
            ToolPaths.writeString(itemsFile, content);
        }
    }

    private static String replaceJavaId(final String content, final String bedrockIdentifier, final String oldJavaId, final String newJavaId) {
        final int start = content.indexOf("\n  \"" + bedrockIdentifier + "\": {");
        if (start == -1) {
            throw new IllegalStateException("Could not find " + bedrockIdentifier + " in the item mappings");
        }
        final int end = content.indexOf("\n  },", start + 1);
        final String entry = content.substring(start, end == -1 ? content.length() : end);

        final String oldValue = "\"java_id\": \"" + oldJavaId + "\"";
        if (entry.indexOf(oldValue) != entry.lastIndexOf(oldValue)) {
            throw new IllegalStateException(bedrockIdentifier + " has more than one mapping to " + oldJavaId + ", refusing to guess which one to change");
        }
        if (!entry.contains(oldValue)) {
            throw new IllegalStateException("Could not find " + oldValue + " in " + bedrockIdentifier);
        }
        return content.substring(0, start) + entry.replace(oldValue, "\"java_id\": \"" + newJavaId + "\"") + content.substring(start + entry.length());
    }

}
