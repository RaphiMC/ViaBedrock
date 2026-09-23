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
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.tool.ToolArgs;
import net.raphimc.viabedrock.tool.ToolPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        final boolean replace = toolArgs.flag("replace");
        final List<String> categories = toolArgs.list("category").isEmpty()
                ? List.of(MappingAnalysis.BLOCK_STATES, MappingAnalysis.ITEMS)
                : toolArgs.list("category");
        for (String category : categories) {
            if (!List.of(MappingAnalysis.BLOCK_STATES, MappingAnalysis.ITEMS, MappingAnalysis.ENTITIES,
                    MappingAnalysis.EFFECTS, MappingAnalysis.PARTICLES, MappingAnalysis.SOUNDS).contains(category)) {
                throw new IllegalArgumentException("Cannot apply proposals for category " + category);
            }
        }

        final ItemChanges itemChanges = categories.contains(MappingAnalysis.ITEMS) ? prepareItemFixes() : null;
        final BlockChanges blockChanges = categories.contains(MappingAnalysis.BLOCK_STATES) ? prepareBlockChanges(keepStale, replace) : null;
        final List<IdentifierChanges> identifierChanges = prepareIdentifierChanges(categories, replace);

        if (itemChanges != null) {
            System.out.println("item java_ids changed " + itemChanges.count());
        }
        if (blockChanges != null) {
            System.out.println("added    " + blockChanges.added());
            System.out.println("replaced " + blockChanges.replaced());
            System.out.println("removed  " + blockChanges.removed() + " stale mappings");
            System.out.println("entries  " + blockChanges.before() + " -> " + blockChanges.mappings().size());
        }
        for (IdentifierChanges changes : identifierChanges) {
            System.out.println(changes.category() + " mappings added " + changes.added() + ", replaced " + changes.replaced());
        }
        if (dryRun) {
            System.out.println("--dry-run given, nothing written");
            return;
        }
        if (blockChanges != null && blockChanges.changed()) {
            ToolPaths.writeJson(ToolPaths.CUSTOM_DATA.resolve("blockstate_mappings.json"), GsonUtil.sort(blockChanges.mappings()));
        }
        if (itemChanges != null && itemChanges.count() > 0) {
            ToolPaths.writeString(ToolPaths.CUSTOM_DATA.resolve("item_mappings.json"), itemChanges.content());
        }
        for (IdentifierChanges changes : identifierChanges) {
            if (changes.added() > 0 || changes.replaced() > 0) {
                ToolPaths.writeJson(changes.file(), GsonUtil.sort(changes.mappings()));
            }
        }
    }

    private static List<IdentifierChanges> prepareIdentifierChanges(final List<String> categories, final boolean replace) throws Exception {
        final List<String> selected = categories.stream().filter(category -> List.of(MappingAnalysis.ENTITIES,
                MappingAnalysis.EFFECTS, MappingAnalysis.PARTICLES, MappingAnalysis.SOUNDS).contains(category)).toList();
        if (selected.isEmpty()) {
            return List.of();
        }
        final MappingAssets assets = new MappingAssets();
        final JsonObject proposed = JsonParser.parseString(Files.readString(MappingProposer.OUTPUT_DIR.resolve("identifiers.json"))).getAsJsonObject();
        final List<IdentifierChanges> changes = new ArrayList<>();
        for (String category : selected) {
            final String fileName = switch (category) {
                case MappingAnalysis.ENTITIES -> "entity_mappings.json";
                case MappingAnalysis.EFFECTS -> "effect_mappings.json";
                case MappingAnalysis.PARTICLES -> "particle_mappings.json";
                case MappingAnalysis.SOUNDS -> "sound_mappings.json";
                default -> throw new IllegalArgumentException("Unknown category " + category);
            };
            final Set<String> bedrockIdentifiers = switch (category) {
                case MappingAnalysis.ENTITIES -> assets.bedrockEntities();
                case MappingAnalysis.EFFECTS -> assets.bedrockStrings("bedrock/effects.json");
                case MappingAnalysis.PARTICLES -> assets.bedrockStrings("bedrock/particles.json");
                case MappingAnalysis.SOUNDS -> assets.bedrockKeys("bedrock/sounds.json");
                default -> throw new IllegalArgumentException("Unknown category " + category);
            };
            final Set<String> javaIdentifiers = category.equals(MappingAnalysis.EFFECTS)
                    ? assets.javaEffects() : assets.javaNamespaced(category);
            final Path file = ToolPaths.CUSTOM_DATA.resolve(fileName);
            final JsonObject mappings = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            final JsonObject proposals = proposed.getAsJsonObject(category);
            int added = 0;
            int replaced = 0;
            for (Map.Entry<String, JsonElement> proposal : proposals.entrySet()) {
                final String source = proposal.getKey();
                final String target = Key.namespaced(proposal.getValue().getAsString());
                if (!bedrockIdentifiers.contains(source) || !javaIdentifiers.contains(target)) {
                    throw new IllegalStateException("Invalid " + category + " proposal: " + source + " -> " + target);
                }
                if (mappings.has(source)) {
                    final JsonElement current = mappings.get(source);
                    if (!current.isJsonPrimitive() || !Key.namespaced(current.getAsString()).equals(target)) {
                        if (!replace) {
                            throw new IllegalStateException("Proposal would replace the existing " + category + " mapping for " + source + ". Pass --replace after reviewing it.");
                        }
                        replaced++;
                    }
                } else {
                    added++;
                }
                mappings.addProperty(source, target);
            }
            changes.add(new IdentifierChanges(category, file, mappings, added, replaced));
        }
        return changes;
    }

    private static BlockChanges prepareBlockChanges(final boolean keepStale, final boolean replace) throws Exception {
        final Path mappingsFile = ToolPaths.CUSTOM_DATA.resolve("blockstate_mappings.json");
        final JsonObject mappings = JsonParser.parseString(Files.readString(mappingsFile)).getAsJsonObject();
        final int before = mappings.size();
        final MappingAssets assets = new MappingAssets();
        final Set<BlockState> bedrockStates = new HashSet<>(assets.bedrockBlockStates());
        final Set<BlockState> javaStates = new HashSet<>();
        assets.javaBlockStates().forEach(state -> javaStates.add(BlockState.fromString(state)));

        final Map<String, String> proposals = MappingProposer.readProposals(MappingProposer.OUTPUT_DIR.resolve("block_states.json"));
        int added = 0;
        int replaced = 0;
        for (Map.Entry<String, String> proposal : proposals.entrySet()) {
            if (!bedrockStates.contains(BlockState.fromString(proposal.getKey()))) {
                throw new IllegalStateException("Bedrock no longer has proposed state " + proposal.getKey());
            }
            if (!javaStates.contains(BlockState.fromString(proposal.getValue()))) {
                throw new IllegalStateException("Java has no proposed state " + proposal.getValue());
            }
            if (mappings.has(proposal.getKey())) {
                if (!mappings.get(proposal.getKey()).getAsString().equals(proposal.getValue())) {
                    if (!replace) {
                        throw new IllegalStateException("Proposal would replace the existing mapping for " + proposal.getKey() + ". Pass --replace after reviewing it.");
                    }
                    replaced++;
                }
            } else {
                added++;
            }
            mappings.addProperty(proposal.getKey(), proposal.getValue());
        }

        int removed = 0;
        if (!keepStale) {
            final JsonArray stale = JsonParser.parseString(Files.readString(MappingProposer.OUTPUT_DIR.resolve("block_states_stale.json"))).getAsJsonArray();
            for (JsonElement entry : stale) {
                final String key = entry.getAsString();
                if (bedrockStates.contains(BlockState.fromString(key))) {
                    throw new IllegalStateException("Stale proposal would remove a current Bedrock state: " + key);
                }
                if (mappings.remove(key) != null) {
                    removed++;
                }
            }
        }
        return new BlockChanges(mappings, before, added, replaced, removed);
    }

    /**
     * The item fixes are keyed by the bedrock item and the place the java_id sits.
     * <p>
     * They are applied to the text of the file rather than by reading and writing it back as json, because parts of
     * it are hand formatted on one line and a round trip would reflow them. This way the diff is the changed java_ids
     * and nothing else.
     */
    private static ItemChanges prepareItemFixes() throws Exception {
        final Path itemsFile = ToolPaths.CUSTOM_DATA.resolve("item_mappings.json");
        final Path fixesFile = MappingProposer.OUTPUT_DIR.resolve("item_fixes.json");
        if (!Files.exists(fixesFile)) {
            System.out.println("No item fixes at " + ToolPaths.describe(fixesFile) + ", run ./gradlew reportMappingMistakes first");
            return null;
        }

        String content = Files.readString(itemsFile);
        final Map<String, String> fixes = MappingProposer.readProposals(fixesFile);
        if (fixes.isEmpty()) {
            return new ItemChanges(content, 0);
        }
        if (!Files.exists(MappingProposer.OUTPUT_DIR.resolve("item_fixes_expected.json"))) {
            throw new IllegalStateException("Item proposals have no source snapshot. Run ./gradlew reportMappingMistakes again.");
        }
        final Map<String, String> expectedIds = MappingProposer.readProposals(MappingProposer.OUTPUT_DIR.resolve("item_fixes_expected.json"));
        final JsonObject mappings = JsonParser.parseString(content).getAsJsonObject();
        final Set<String> javaItems = new MappingAssets().javaNamespaced("items");

        int applied = 0;
        for (Map.Entry<String, String> fix : fixes.entrySet()) {
            if (!javaItems.contains(fix.getValue())) {
                throw new IllegalStateException("Java has no proposed item " + fix.getValue());
            }
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
            final String currentJavaId = mapping.get("java_id").getAsString();
            if (!Key.namespaced(currentJavaId).equals(expectedIds.get(fix.getKey()))) {
                throw new IllegalStateException("Item mapping changed since the proposal was made: " + fix.getKey());
            }
            content = replaceJavaId(content, parts[0], currentJavaId, fix.getValue());
            applied++;
        }

        return new ItemChanges(content, applied);
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

    private record ItemChanges(String content, int count) {
    }

    private record BlockChanges(JsonObject mappings, int before, int added, int replaced, int removed) {

        private boolean changed() {
            return this.added > 0 || this.replaced > 0 || this.removed > 0;
        }

    }

    private record IdentifierChanges(String category, Path file, JsonObject mappings, int added, int replaced) {
    }

}
