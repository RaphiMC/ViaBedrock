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

import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Proposes java block states for bedrock block states which have no mapping yet.
 * <p>
 * Two strategies, both of which only ever emit a java block state that actually exists in the java block state list.
 * Anything that cannot be validated is reported as unresolved instead of being written.
 * <ul>
 *     <li>By analogy: find a bedrock block which has the same property keys and is already fully mapped, take its
 *     mapping for the same property values, and rewrite the java identifier the same way the bedrock identifier
 *     differs. This is what turns {@code oak_stairs} into {@code lime_wool_stairs}.</li>
 *     <li>By property name: match bedrock property names against the java properties of the target block, and fill
 *     the rest with the value that the rest of the mappings use for that property. This is what handles blocks which
 *     gained properties, like the panes and bars which grew {@code connection_*}.</li>
 * </ul>
 */
public class BlockStateProposer {

    private final MappingAnalysis analysis;

    private final Map<String, List<BedrockBlockState>> bedrockStatesByIdentifier = new LinkedHashMap<>();
    private final Map<String, Map<String, BlockState>> mappingsByIdentifier = new LinkedHashMap<>();
    private final Map<String, Set<String>> bedrockPropertyKeys = new LinkedHashMap<>();
    private final List<String> templateIdentifiers = new ArrayList<>();

    private final Map<String, List<String>> previousJavaIdentifiers = new LinkedHashMap<>();
    private final Map<String, Map<String, Set<String>>> javaPropertiesByIdentifier = new LinkedHashMap<>();
    private final Map<String, String> commonJavaPropertyValues = new LinkedHashMap<>();
    private final Map<String, List<String>> learnedPropertyAliases = new LinkedHashMap<>();
    private final Map<String, Integer> learnedPropertyAliasSupport = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> learnedPropertyValues = new LinkedHashMap<>();

    private final Set<String> excludedIdentifiers;

    public BlockStateProposer(final MappingAnalysis analysis) {
        this(analysis, Set.of());
    }

    /**
     * @param excludedIdentifiers Bedrock blocks whose existing mappings are ignored. Used by the self test to check
     *                            whether the proposer reproduces mappings which were written by hand.
     */
    public BlockStateProposer(final MappingAnalysis analysis, final Set<String> excludedIdentifiers) {
        this.analysis = analysis;
        this.excludedIdentifiers = excludedIdentifiers;
        this.indexBedrock();
        this.indexJava();
        this.learnPropertyAliases();
    }

    private void indexBedrock() {
        for (BedrockBlockState blockState : this.analysis.bedrockBlockStates()) {
            this.bedrockStatesByIdentifier.computeIfAbsent(blockState.namespacedIdentifier(), key -> new ArrayList<>()).add(blockState);
            this.bedrockPropertyKeys.putIfAbsent(blockState.namespacedIdentifier(), new LinkedHashSet<>(blockState.properties().keySet()));
        }

        final Map<String, Map<String, Integer>> javaIdentifierCounts = new LinkedHashMap<>();
        for (Map.Entry<BlockState, BlockState> entry : this.analysis.blockStateMappings().entrySet()) {
            if (this.excludedIdentifiers.contains(entry.getKey().namespacedIdentifier())) {
                continue;
            }
            this.mappingsByIdentifier
                    .computeIfAbsent(entry.getKey().namespacedIdentifier(), key -> new HashMap<>())
                    .put(propertyKey(entry.getKey()), entry.getValue());
            // Mappings for states bedrock has dropped still tell us which java block this bedrock block belongs to,
            // which is the only hint for blocks whose name differs, like trip_wire -> tripwire
            javaIdentifierCounts
                    .computeIfAbsent(entry.getKey().namespacedIdentifier(), key -> new LinkedHashMap<>())
                    .merge(entry.getValue().namespacedIdentifier(), 1, Integer::sum);
        }
        for (Map.Entry<String, Map<String, Integer>> entry : javaIdentifierCounts.entrySet()) {
            this.previousJavaIdentifiers.put(entry.getKey(), entry.getValue().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .toList());
        }

        // A template has to be complete, otherwise it could hand out a mapping for a state which is itself a guess
        for (Map.Entry<String, List<BedrockBlockState>> entry : this.bedrockStatesByIdentifier.entrySet()) {
            final Map<String, BlockState> mappings = this.mappingsByIdentifier.get(entry.getKey());
            if (mappings == null) {
                continue;
            }
            boolean complete = true;
            for (BedrockBlockState blockState : entry.getValue()) {
                if (!mappings.containsKey(propertyKey(blockState))) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                this.templateIdentifiers.add(entry.getKey());
            }
        }
    }

    private void indexJava() {
        for (BlockState blockState : this.analysis.javaBlockStates()) {
            final Map<String, Set<String>> properties = this.javaPropertiesByIdentifier.computeIfAbsent(blockState.namespacedIdentifier(), key -> new LinkedHashMap<>());
            for (Map.Entry<String, String> property : blockState.properties().entrySet()) {
                properties.computeIfAbsent(property.getKey(), key -> new LinkedHashSet<>()).add(property.getValue());
            }
        }

        // Which value does the rest of the file use for a property the bedrock state says nothing about? For
        // "waterlogged" that is "false", and guessing it from the data beats hardcoding a list of property names.
        final Map<String, Map<String, Integer>> counts = new LinkedHashMap<>();
        for (BlockState javaBlockState : this.analysis.blockStateMappings().values()) {
            for (Map.Entry<String, String> property : javaBlockState.properties().entrySet()) {
                counts.computeIfAbsent(property.getKey(), key -> new LinkedHashMap<>()).merge(property.getValue(), 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Map<String, Integer>> entry : counts.entrySet()) {
            entry.getValue().entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(best -> this.commonJavaPropertyValues.put(entry.getKey(), best.getKey()));
        }
    }

    /**
     * Learns which bedrock property belongs to which java property from the blocks which are already mapped.
     * <p>
     * Bedrock renames properties freely, so {@code minecraft:cardinal_direction} is java's {@code facing} and
     * {@code growth} is java's {@code age}. A pair counts as evidence when the bedrock value decides the java value
     * within a block and the java property actually varies, and the pair with the most supporting blocks wins.
     */
    private void learnPropertyAliases() {
        final Map<String, Map<String, Integer>> support = new LinkedHashMap<>();
        final Map<String, Map<String, Map<String, Integer>>> valueCounts = new LinkedHashMap<>();

        for (Map.Entry<String, List<BedrockBlockState>> block : this.bedrockStatesByIdentifier.entrySet()) {
            final Map<String, BlockState> mappings = this.mappingsByIdentifier.get(block.getKey());
            if (mappings == null) {
                continue;
            }

            final Map<String, Map<String, Set<String>>> observed = new LinkedHashMap<>();
            final Map<String, Set<String>> javaValues = new LinkedHashMap<>();
            for (BedrockBlockState bedrockBlockState : block.getValue()) {
                final BlockState javaBlockState = mappings.get(propertyKey(bedrockBlockState));
                if (javaBlockState == null) {
                    continue;
                }
                for (Map.Entry<String, String> javaProperty : javaBlockState.properties().entrySet()) {
                    javaValues.computeIfAbsent(javaProperty.getKey(), key -> new LinkedHashSet<>()).add(javaProperty.getValue());
                    for (Map.Entry<String, String> bedrockProperty : bedrockBlockState.properties().entrySet()) {
                        observed.computeIfAbsent(bedrockProperty.getKey() + "\u0000" + javaProperty.getKey(), key -> new LinkedHashMap<>())
                                .computeIfAbsent(bedrockProperty.getValue(), key -> new LinkedHashSet<>())
                                .add(javaProperty.getValue());
                    }
                }
            }

            for (Map.Entry<String, Map<String, Set<String>>> entry : observed.entrySet()) {
                final String[] keys = entry.getKey().split("\u0000", 2);
                if (javaValues.get(keys[1]).size() < 2) {
                    continue; // The java property never changes in this block, so it says nothing about the bedrock one
                }
                boolean functional = true;
                for (Set<String> values : entry.getValue().values()) {
                    if (values.size() != 1) {
                        functional = false;
                        break;
                    }
                }
                if (functional) {
                    support.computeIfAbsent(keys[0], key -> new LinkedHashMap<>()).merge(keys[1], 1, Integer::sum);
                    for (Map.Entry<String, Set<String>> value : entry.getValue().entrySet()) {
                        valueCounts.computeIfAbsent(entry.getKey(), key -> new LinkedHashMap<>())
                                .computeIfAbsent(value.getKey(), key -> new LinkedHashMap<>())
                                .merge(value.getValue().iterator().next(), 1, Integer::sum);
                    }
                }
            }
        }

        for (Map.Entry<String, Map<String, Integer>> entry : support.entrySet()) {
            final List<String> javaKeys = entry.getValue().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .toList();
            this.learnedPropertyAliases.put(entry.getKey(), javaKeys);
            for (Map.Entry<String, Integer> javaKey : entry.getValue().entrySet()) {
                this.learnedPropertyAliasSupport.put(entry.getKey() + " -> " + javaKey.getKey(), javaKey.getValue());
            }
        }

        // Bedrock also renames the values, not just the property: a bed's head_piece_bit=1 is java's part=head
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : valueCounts.entrySet()) {
            final Map<String, String> valueMap = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Integer>> value : entry.getValue().entrySet()) {
                value.getValue().entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .ifPresent(best -> valueMap.put(value.getKey(), best.getKey()));
            }
            this.learnedPropertyValues.put(entry.getKey(), valueMap);
        }
    }

    public List<BlockStateProposal> proposeAll() {
        final List<BlockStateProposal> proposals = new ArrayList<>();
        for (BedrockBlockState bedrockBlockState : this.analysis.bedrockBlockStates()) {
            if (this.analysis.blockStateMappings().containsKey(bedrockBlockState) && !this.excludedIdentifiers.contains(bedrockBlockState.namespacedIdentifier())) {
                continue;
            }
            proposals.add(this.propose(bedrockBlockState));
        }
        return proposals;
    }

    public BlockStateProposal propose(final BedrockBlockState bedrockBlockState) {
        final BlockStateProposal byAnalogy = this.proposeByAnalogy(bedrockBlockState);
        if (byAnalogy != null) {
            return byAnalogy;
        }
        final BlockStateProposal byPropertyName = this.proposeByPropertyName(bedrockBlockState, null);
        if (byPropertyName != null) {
            return byPropertyName;
        }
        // The block kept its bedrock name but java calls it something else, or java has no equivalent and the old
        // mapping already picked a stand in. Either way the previous mapping of this block names the java block.
        for (String previousJavaIdentifier : this.previousJavaIdentifiers.getOrDefault(bedrockBlockState.namespacedIdentifier(), List.of())) {
            final BlockStateProposal byPreviousIdentifier = this.proposeByPropertyName(bedrockBlockState, previousJavaIdentifier);
            if (byPreviousIdentifier != null) {
                return new BlockStateProposal(bedrockBlockState, byPreviousIdentifier.javaBlockState(), "previous-mapping", null,
                        byPreviousIdentifier.score() - 0.05D, byPreviousIdentifier.note());
            }
        }
        return new BlockStateProposal(bedrockBlockState, null, "unresolved", null, 0D, "no java block state could be validated");
    }

    private BlockStateProposal proposeByAnalogy(final BedrockBlockState bedrockBlockState) {
        final String identifier = bedrockBlockState.namespacedIdentifier();
        final Set<String> propertyKeys = bedrockBlockState.properties().keySet();

        for (String template : this.rankTemplates(identifier, propertyKeys)) {
            final BlockState templateJavaState = this.mappingsByIdentifier.get(template).get(propertyKey(bedrockBlockState));
            if (templateJavaState == null) {
                continue;
            }

            final String javaIdentifier = template.equals(identifier)
                    ? templateJavaState.namespacedIdentifier()
                    : Similarity.rewriteIdentifier(template, identifier, templateJavaState.namespacedIdentifier());
            if (javaIdentifier == null || !this.javaPropertiesByIdentifier.containsKey(javaIdentifier)) {
                continue;
            }

            final BlockState candidate = templateJavaState.withNamespacedIdentifier(javaIdentifier);
            if (this.analysis.javaBlockStates().contains(candidate)) {
                final int suffix = Similarity.commonSuffixLength(template, identifier);
                double score = template.equals(identifier) ? 1D : (suffix >= 4 ? 0.95D : 0.8D);

                // Bedrock keeps legacy names which point at a different java block: bedrock's stone_stairs is java's
                // cobblestone_stairs, and java has a stone_stairs of its own, so the rewrite looks valid but is wrong.
                // If this block was mapped before, that mapping knows better than the name does.
                final List<String> previous = this.previousJavaIdentifiers.getOrDefault(identifier, List.of());
                String note = null;
                if (!previous.isEmpty() && !previous.contains(candidate.namespacedIdentifier())) {
                    note = "name says " + candidate.namespacedIdentifier() + " but this block used to map to " + String.join(", ", previous);
                    score = 0.4D;
                }
                return new BlockStateProposal(bedrockBlockState, candidate, "analogy", template, score, note);
            }

            // Same family but the java block carries different properties, so fall back to matching them up by name
            final BlockStateProposal byPropertyName = this.proposeByPropertyName(bedrockBlockState, javaIdentifier);
            if (byPropertyName != null) {
                return new BlockStateProposal(bedrockBlockState, byPropertyName.javaBlockState(), "analogy+properties", template, 0.7D, byPropertyName.note());
            }
        }
        return null;
    }

    /**
     * Templates are ranked by how closely they are related to the target block: the block itself first, then the ones
     * sharing the longest trailing word, then the closest name overall.
     */
    private List<String> rankTemplates(final String identifier, final Set<String> propertyKeys) {
        final List<String> candidates = new ArrayList<>();
        for (String template : this.templateIdentifiers) {
            if (this.bedrockPropertyKeys.get(template).equals(propertyKeys)) {
                candidates.add(template);
            }
        }
        candidates.sort(Comparator
                .comparing((String template) -> !template.equals(identifier))
                .thenComparing(template -> -Similarity.commonSuffixLength(template, identifier))
                .thenComparing(template -> Similarity.levenshtein(template, identifier, Integer.MAX_VALUE - 1))
                .thenComparing(template -> template));
        return candidates;
    }

    private BlockStateProposal proposeByPropertyName(final BedrockBlockState bedrockBlockState, final String forcedJavaIdentifier) {
        final String javaIdentifier = forcedJavaIdentifier != null ? forcedJavaIdentifier : bedrockBlockState.namespacedIdentifier();
        final Map<String, Set<String>> javaProperties = this.javaPropertiesByIdentifier.get(javaIdentifier);
        if (javaProperties == null) {
            return null;
        }

        final Map<String, String> bedrockByNormalizedName = new LinkedHashMap<>();
        for (Map.Entry<String, String> property : bedrockBlockState.properties().entrySet()) {
            bedrockByNormalizedName.put(normalizePropertyName(property.getKey()), property.getValue());
        }

        final Map<String, String> javaValues = new TreeMap<>();
        final List<String> defaulted = new ArrayList<>();
        final List<String> viaAlias = new ArrayList<>();
        for (Map.Entry<String, Set<String>> javaProperty : javaProperties.entrySet()) {
            final String javaKey = javaProperty.getKey();
            final Set<String> allowedValues = javaProperty.getValue();

            String value = translateValue(bedrockByNormalizedName.get(javaKey), allowedValues);
            if (value == null) {
                // No property of that name, so fall back to what the already mapped blocks call it
                for (Map.Entry<String, String> bedrockProperty : bedrockBlockState.properties().entrySet()) {
                    if (!this.learnedPropertyAliases.getOrDefault(bedrockProperty.getKey(), List.of()).contains(javaKey)) {
                        continue;
                    }
                    String aliasValue = translateValue(bedrockProperty.getValue(), allowedValues);
                    if (aliasValue == null) {
                        final String learnedValue = this.learnedPropertyValues
                                .getOrDefault(bedrockProperty.getKey() + "\u0000" + javaKey, Map.of())
                                .get(bedrockProperty.getValue());
                        if (learnedValue != null && allowedValues.contains(learnedValue)) {
                            aliasValue = learnedValue;
                        }
                    }
                    if (aliasValue != null) {
                        value = aliasValue;
                        viaAlias.add(bedrockProperty.getKey() + "->" + javaKey
                                + " (" + this.learnedPropertyAliasSupport.getOrDefault(bedrockProperty.getKey() + " -> " + javaKey, 0) + " blocks)");
                        break;
                    }
                }
            }
            if (value == null) {
                value = this.commonJavaPropertyValues.get(javaKey);
                if (value == null || !allowedValues.contains(value)) {
                    value = allowedValues.iterator().next();
                }
                defaulted.add(javaKey + "=" + value);
            }
            javaValues.put(javaKey, value);
        }

        final BlockState candidate = new BlockState(javaIdentifier.split(":", 2)[0], javaIdentifier.split(":", 2)[1], javaValues);
        if (!this.analysis.javaBlockStates().contains(candidate)) {
            return null;
        }

        // A defaulted property is only safe when the bedrock state has nothing left to say. If bedrock properties
        // went unused while a java property had to be guessed, the two sides disagree and a human should look at it.
        final int usedBedrockProperties = bedrockBlockState.properties().size() - unusedBedrockProperties(bedrockBlockState, javaProperties.keySet(), viaAlias);
        final boolean guessing = !defaulted.isEmpty() && usedBedrockProperties < bedrockBlockState.properties().size();

        final double score = defaulted.isEmpty() ? (viaAlias.isEmpty() ? 0.7D : 0.65D) : (guessing ? 0.45D : 0.55D);
        final List<String> notes = new ArrayList<>();
        if (!viaAlias.isEmpty()) {
            notes.add("learned " + String.join(", ", viaAlias));
        }
        if (!defaulted.isEmpty()) {
            notes.add("defaulted " + String.join(", ", defaulted));
        }
        return new BlockStateProposal(bedrockBlockState, candidate, "properties", null, score, notes.isEmpty() ? null : String.join("; ", notes));
    }

    private int unusedBedrockProperties(final BedrockBlockState bedrockBlockState, final Set<String> javaKeys, final List<String> viaAlias) {
        int unused = 0;
        for (String bedrockKey : bedrockBlockState.properties().keySet()) {
            final boolean byName = javaKeys.contains(normalizePropertyName(bedrockKey));
            final boolean byAlias = viaAlias.stream().anyMatch(alias -> alias.startsWith(bedrockKey + "->"));
            if (!byName && !byAlias) {
                unused++;
            }
        }
        return unused;
    }

    /**
     * Bedrock decorates its property names, java doesn't: {@code minecraft:connection_east} is java's {@code east},
     * {@code upside_down_bit} is java's {@code upside_down}.
     */
    static String normalizePropertyName(final String name) {
        String normalized = name;
        final int namespace = normalized.indexOf(':');
        if (namespace != -1) {
            normalized = normalized.substring(namespace + 1);
        }
        if (normalized.endsWith("_bit")) {
            normalized = normalized.substring(0, normalized.length() - "_bit".length());
        }
        if (normalized.startsWith("connection_")) {
            normalized = normalized.substring("connection_".length());
        }
        return normalized;
    }

    static String translateValue(final String bedrockValue, final Set<String> allowedValues) {
        if (bedrockValue == null) {
            return null;
        }
        if (allowedValues.contains(bedrockValue)) {
            return bedrockValue;
        }
        if (allowedValues.size() == 2 && allowedValues.contains("true") && allowedValues.contains("false")) {
            if (bedrockValue.equals("0") || bedrockValue.equals("false")) {
                return "false";
            }
            if (bedrockValue.equals("1") || bedrockValue.equals("true")) {
                return "true";
            }
        }
        return null;
    }

    static String propertyKey(final BlockState blockState) {
        return new TreeMap<>(blockState.properties()).toString();
    }

}
