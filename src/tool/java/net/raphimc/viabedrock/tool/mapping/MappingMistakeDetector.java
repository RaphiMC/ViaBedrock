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
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Looks for mappings which load without complaining but disagree with the rest of the data.
 * <p>
 * Nothing here is proof of a bug. Bedrock keeps legacy names which deliberately point somewhere else, and plenty of
 * mappings collapse detail java cannot represent. These checks narrow 22000 mappings down to the handful worth
 * reading.
 */
public class MappingMistakeDetector {

    private final MappingAssets assets;
    private final MappingAnalysis analysis;
    private final List<MappingMistake> mistakes = new ArrayList<>();

    public MappingMistakeDetector(final MappingAssets assets, final MappingAnalysis analysis) {
        this.assets = assets;
        this.analysis = analysis;
    }

    public List<MappingMistake> run() {
        this.mistakes.clear();

        this.checkIdentity(MappingAnalysis.ENTITIES, "custom/entity_mappings.json", this.assets.javaNamespaced("entities"));
        this.checkIdentity(MappingAnalysis.EFFECTS, "custom/effect_mappings.json", this.assets.javaEffects());
        this.checkIdentity(MappingAnalysis.PARTICLES, "custom/particle_mappings.json", this.assets.javaNamespaced("particles"));
        this.checkBlockStateIdentity();
        this.checkFamilyOutliers();
        this.checkIgnoredProperties();

        this.mistakes.sort(Comparator.comparing(MappingMistake::category).thenComparing(MappingMistake::check).thenComparing(MappingMistake::key));
        return List.copyOf(this.mistakes);
    }

    /**
     * Most of these categories map an identifier to the java identifier of the same name. When a mapping points
     * somewhere else although java has the matching name, either bedrock renamed something or the mapping predates
     * java gaining the block.
     */
    private void checkIdentity(final String category, final String mappingFile, final Set<String> javaIdentifiers) {
        for (Map.Entry<String, JsonElement> entry : this.assets.json(mappingFile).entrySet()) {
            final String identity = Key.namespaced(entry.getKey());
            if (!javaIdentifiers.contains(identity)) {
                continue;
            }
            if (entry.getValue().isJsonNull()) {
                this.mistakes.add(new MappingMistake(category, MappingMistake.UNMAPPED_BUT_PRESENT, entry.getKey(),
                        "left unmapped although java has " + identity));
                continue;
            }
            final String javaIdentifier = entry.getValue().isJsonObject()
                    ? Key.namespaced(entry.getValue().getAsJsonObject().get("particle").getAsString())
                    : Key.namespaced(entry.getValue().getAsString());
            if (!javaIdentifier.equals(identity)) {
                this.mistakes.add(new MappingMistake(category, MappingMistake.IDENTITY_AVAILABLE, entry.getKey(),
                        "maps to " + javaIdentifier + " although java has " + identity));
            }
        }
    }

    private void checkBlockStateIdentity() {
        final Map<String, Set<String>> javaIdentifiersByBlock = new LinkedHashMap<>();
        for (Map.Entry<BlockState, BlockState> entry : this.analysis.blockStateMappings().entrySet()) {
            javaIdentifiersByBlock
                    .computeIfAbsent(entry.getKey().namespacedIdentifier(), key -> new LinkedHashSet<>())
                    .add(entry.getValue().namespacedIdentifier());
        }

        for (Map.Entry<String, Set<String>> entry : javaIdentifiersByBlock.entrySet()) {
            if (!this.analysis.javaBlocks().contains(entry.getKey()) || entry.getValue().contains(entry.getKey())) {
                continue;
            }
            this.mistakes.add(new MappingMistake(MappingAnalysis.BLOCK_STATES, MappingMistake.IDENTITY_AVAILABLE, entry.getKey(),
                    "maps to " + String.join(", ", entry.getValue()) + " although java has " + entry.getKey()));
        }
    }

    /**
     * Blocks which share both their bedrock and their java properties are almost always translated the same way. When
     * one block out of a large group does something different, it is usually a slip made while copying the group.
     */
    private void checkFamilyOutliers() {
        final Map<String, List<BedrockBlockState>> statesByBlock = new TreeMap<>();
        for (BedrockBlockState bedrockBlockState : this.analysis.bedrockBlockStates()) {
            statesByBlock.computeIfAbsent(bedrockBlockState.namespacedIdentifier(), key -> new ArrayList<>()).add(bedrockBlockState);
        }

        final Map<String, Map<String, List<String>>> groups = new LinkedHashMap<>();
        for (Map.Entry<String, List<BedrockBlockState>> block : statesByBlock.entrySet()) {
            final Map<String, String> signature = new TreeMap<>();
            final Set<String> javaIdentifiers = new LinkedHashSet<>();
            final Set<String> javaPropertyKeys = new LinkedHashSet<>();
            boolean complete = true;
            for (BedrockBlockState bedrockBlockState : block.getValue()) {
                final BlockState javaBlockState = this.analysis.blockStateMappings().get(bedrockBlockState);
                if (javaBlockState == null) {
                    complete = false;
                    break;
                }
                javaIdentifiers.add(javaBlockState.namespacedIdentifier());
                javaPropertyKeys.addAll(javaBlockState.properties().keySet());
                signature.put(new TreeMap<>(bedrockBlockState.properties()).toString(),
                        javaIdentifiers.stream().toList().indexOf(javaBlockState.namespacedIdentifier()) + new TreeMap<>(javaBlockState.properties()).toString());
            }
            if (!complete || javaPropertyKeys.isEmpty() || block.getValue().get(0).properties().isEmpty()) {
                continue; // Without properties on both sides there is no translation to compare
            }

            // Both sides have to describe the same thing before two blocks can be called comparable
            final String groupKey = signature.keySet() + " -> " + javaPropertyKeys + " in " + javaIdentifiers.size() + " identifiers";
            groups.computeIfAbsent(groupKey, key -> new LinkedHashMap<>())
                    .computeIfAbsent(signature.toString(), key -> new ArrayList<>())
                    .add(block.getKey());
        }

        for (Map.Entry<String, Map<String, List<String>>> group : groups.entrySet()) {
            if (group.getValue().size() < 2) {
                continue;
            }
            final List<Map.Entry<String, List<String>>> variants = new ArrayList<>(group.getValue().entrySet());
            variants.sort(Comparator.comparingInt((Map.Entry<String, List<String>> variant) -> variant.getValue().size()).reversed());

            final Map.Entry<String, List<String>> majority = variants.get(0);
            if (majority.getValue().size() < 5) {
                continue; // Too small a group to call anything an outlier
            }
            for (int i = 1; i < variants.size(); i++) {
                final Map.Entry<String, List<String>> variant = variants.get(i);
                if (variant.getValue().size() > 2) {
                    continue; // Two ways of doing it, both common, so probably both deliberate
                }
                for (String block : variant.getValue()) {
                    this.mistakes.add(new MappingMistake(MappingAnalysis.BLOCK_STATES, MappingMistake.FAMILY_OUTLIER, block,
                            "translates its properties differently from " + majority.getValue().size() + " similar blocks, for example "
                                    + majority.getValue().get(0)));
                }
            }
        }
    }

    /**
     * A bedrock property which changes nothing on the java side is usually intentional, but not when java has a
     * property of the same name sitting right there.
     */
    private void checkIgnoredProperties() {
        final Map<String, List<BedrockBlockState>> statesByBlock = new TreeMap<>();
        for (BedrockBlockState bedrockBlockState : this.analysis.bedrockBlockStates()) {
            statesByBlock.computeIfAbsent(bedrockBlockState.namespacedIdentifier(), key -> new ArrayList<>()).add(bedrockBlockState);
        }

        for (Map.Entry<String, List<BedrockBlockState>> block : statesByBlock.entrySet()) {
            final Map<String, Set<String>> javaValuesPerBedrockProperty = new LinkedHashMap<>();
            final Set<String> javaPropertyKeys = new LinkedHashSet<>();
            boolean complete = true;

            for (BedrockBlockState bedrockBlockState : block.getValue()) {
                final BlockState javaBlockState = this.analysis.blockStateMappings().get(bedrockBlockState);
                if (javaBlockState == null) {
                    complete = false;
                    break;
                }
                javaPropertyKeys.addAll(javaBlockState.properties().keySet());
                for (String bedrockKey : bedrockBlockState.properties().keySet()) {
                    javaValuesPerBedrockProperty
                            .computeIfAbsent(bedrockKey, key -> new LinkedHashSet<>())
                            .add(javaBlockState.namespacedIdentifier() + new TreeMap<>(javaBlockState.properties()));
                }
            }
            if (!complete) {
                continue;
            }

            for (String bedrockKey : javaValuesPerBedrockProperty.keySet()) {
                final String javaKey = BlockStateProposer.normalizePropertyName(bedrockKey);
                if (!javaPropertyKeys.contains(javaKey)) {
                    continue;
                }
                if (this.propertyChangesNothing(block.getValue(), bedrockKey, javaKey)) {
                    this.mistakes.add(new MappingMistake(MappingAnalysis.BLOCK_STATES, MappingMistake.IGNORED_PROPERTY, block.getKey(),
                            bedrockKey + " never changes java's " + javaKey));
                }
            }
        }
    }

    private boolean propertyChangesNothing(final List<BedrockBlockState> states, final String bedrockKey, final String javaKey) {
        final Map<String, Set<String>> javaValuesByRest = new LinkedHashMap<>();
        for (BedrockBlockState bedrockBlockState : states) {
            final BlockState javaBlockState = this.analysis.blockStateMappings().get(bedrockBlockState);
            final Map<String, String> rest = new TreeMap<>(bedrockBlockState.properties());
            rest.remove(bedrockKey);
            javaValuesByRest.computeIfAbsent(rest.toString(), key -> new LinkedHashSet<>()).add(javaBlockState.properties().get(javaKey));
        }
        for (Set<String> values : javaValuesByRest.values()) {
            if (values.size() > 1) {
                return false;
            }
        }
        return true;
    }

}
