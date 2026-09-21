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
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Compares the bedrock data assets against the curated mapping files and collects everything that is wrong.
 * <p>
 * The checks mirror the ones {@link net.raphimc.viabedrock.protocol.data.BedrockMappingData} performs while loading,
 * with one difference: loading stops at the first problem, this collects all of them.
 */
public class MappingAnalysis {

    public static final String BLOCK_STATES = "block_states";
    public static final String ITEMS = "items";
    public static final String ENTITIES = "entities";
    public static final String EFFECTS = "effects";
    public static final String PARTICLES = "particles";
    public static final String SOUNDS = "sounds";

    public static final List<String> CATEGORIES = List.of(BLOCK_STATES, ITEMS, ENTITIES, EFFECTS, PARTICLES, SOUNDS);

    private final MappingAssets assets;
    private final List<MappingGap> gaps = new ArrayList<>();

    private final Map<BlockState, BlockState> blockStateMappings = new LinkedHashMap<>();
    private final Set<BlockState> javaBlockStates = new LinkedHashSet<>();
    private final Set<String> javaBlocks;
    private final List<BedrockBlockState> bedrockBlockStates;

    public MappingAnalysis(final MappingAssets assets) {
        this.assets = assets;
        this.javaBlocks = assets.javaNamespaced("blocks");
        this.bedrockBlockStates = assets.bedrockBlockStates();
        for (String javaBlockState : assets.javaBlockStates()) {
            this.javaBlockStates.add(BlockState.fromString(javaBlockState));
        }
    }

    public List<MappingGap> run(final List<String> categories) {
        this.gaps.clear();
        if (categories.contains(BLOCK_STATES)) {
            this.checkBlockStates();
        }
        if (categories.contains(ITEMS)) {
            this.checkItems();
        }
        if (categories.contains(ENTITIES)) {
            this.checkIdentifierMapping(ENTITIES, this.assets.bedrockEntities(), "custom/entity_mappings.json", this.assets.javaNamespaced("entities"), true);
        }
        if (categories.contains(EFFECTS)) {
            this.checkIdentifierMapping(EFFECTS, this.assets.bedrockStrings("bedrock/effects.json"), "custom/effect_mappings.json", this.assets.javaEffects(), false);
        }
        if (categories.contains(PARTICLES)) {
            this.checkParticles();
        }
        if (categories.contains(SOUNDS)) {
            this.checkIdentifierMapping(SOUNDS, this.assets.bedrockKeys("bedrock/sounds.json"), "custom/sound_mappings.json", this.assets.javaNamespaced("sounds"), false);
        }
        return List.copyOf(this.gaps);
    }

    /**
     * The block states are the only mapping where both sides carry properties, which is also where updates leave the most holes.
     */
    private void checkBlockStates() {
        final Set<BlockState> bedrockBlockStates = new LinkedHashSet<>(this.bedrockBlockStates);
        final JsonObject mappings = this.assets.json("custom/blockstate_mappings.json");

        for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
            final BlockState bedrockBlockState = BlockState.fromString(entry.getKey());
            final BlockState javaBlockState = BlockState.fromString(entry.getValue().getAsString());
            if (!bedrockBlockStates.contains(bedrockBlockState)) {
                this.gaps.add(new MappingGap(BLOCK_STATES, MappingGap.Kind.STALE, entry.getKey(), "bedrock no longer has this block state"));
            }
            if (!this.javaBlockStates.contains(javaBlockState)) {
                this.gaps.add(new MappingGap(BLOCK_STATES, MappingGap.Kind.BROKEN, entry.getKey(), "java has no block state " + entry.getValue().getAsString()));
            }
            if (this.blockStateMappings.put(bedrockBlockState, javaBlockState) != null) {
                this.gaps.add(new MappingGap(BLOCK_STATES, MappingGap.Kind.BROKEN, entry.getKey(), "duplicate mapping"));
            }
        }

        for (BedrockBlockState bedrockBlockState : this.bedrockBlockStates) {
            if (!this.blockStateMappings.containsKey(bedrockBlockState)) {
                this.gaps.add(new MappingGap(BLOCK_STATES, MappingGap.Kind.MISSING, bedrockBlockState.toBlockStateString(true), null));
            }
        }
    }

    /**
     * Items are split into block items and meta items by their runtime id, and the two use different mapping shapes.
     */
    private void checkItems() {
        final Set<String> bedrockItems = this.assets.bedrockItems();
        final Set<String> bedrockBlockItems = this.assets.bedrockBlockItems(ProtocolConstants.LAST_BLOCK_ITEM_ID);
        final Set<String> javaItems = this.assets.javaNamespaced("items");
        final JsonObject mappings = this.assets.json("custom/item_mappings.json");

        for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
            final String bedrockIdentifier = entry.getKey();
            if (!bedrockItems.contains(bedrockIdentifier)) {
                this.gaps.add(new MappingGap(ITEMS, MappingGap.Kind.STALE, bedrockIdentifier, "bedrock no longer has this item"));
                continue;
            }
            final JsonObject definition = entry.getValue().getAsJsonObject();
            final boolean isBlockItem = bedrockBlockItems.contains(bedrockIdentifier);
            if (definition.has("block") != isBlockItem) {
                this.gaps.add(new MappingGap(ITEMS, MappingGap.Kind.BROKEN, bedrockIdentifier,
                        isBlockItem ? "is a block item but is mapped as a meta item" : "is a meta item but is mapped as a block item"));
                continue;
            }
            if (isBlockItem) {
                for (Map.Entry<String, JsonElement> blockMapping : definition.getAsJsonObject("block").entrySet()) {
                    this.checkJavaItem(bedrockIdentifier + " " + blockMapping.getKey(), blockMapping.getValue(), javaItems);
                }
            } else {
                final JsonObject meta = definition.getAsJsonObject("meta");
                if (!meta.has("")) {
                    this.gaps.add(new MappingGap(ITEMS, MappingGap.Kind.BROKEN, bedrockIdentifier, "has no default meta mapping"));
                }
                for (Map.Entry<String, JsonElement> metaMapping : meta.entrySet()) {
                    this.checkJavaItem(bedrockIdentifier + ":" + metaMapping.getKey(), metaMapping.getValue(), javaItems);
                }
            }
        }

        for (String bedrockIdentifier : bedrockItems) {
            if (!mappings.has(bedrockIdentifier)) {
                this.gaps.add(new MappingGap(ITEMS, MappingGap.Kind.MISSING, bedrockIdentifier,
                        bedrockBlockItems.contains(bedrockIdentifier) ? "block item" : "meta item"));
            }
        }
    }

    private void checkJavaItem(final String key, final JsonElement value, final Set<String> javaItems) {
        if (!value.isJsonObject() || !value.getAsJsonObject().has("java_id")) {
            this.gaps.add(new MappingGap(ITEMS, MappingGap.Kind.BROKEN, key, "mapping has no java_id"));
            return;
        }
        final String javaIdentifier = Key.namespaced(value.getAsJsonObject().get("java_id").getAsString());
        if (!javaItems.contains(javaIdentifier)) {
            this.gaps.add(new MappingGap(ITEMS, MappingGap.Kind.BROKEN, key, "java has no item " + javaIdentifier));
        }
    }

    /**
     * Particles map either to a plain identifier or to an object with the particle and its arguments.
     */
    private void checkParticles() {
        final Set<String> bedrockParticles = this.assets.bedrockStrings("bedrock/particles.json");
        final Set<String> javaParticles = this.assets.javaNamespaced("particles");
        final JsonObject mappings = this.assets.json("custom/particle_mappings.json");

        for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
            if (!bedrockParticles.contains(entry.getKey())) {
                this.gaps.add(new MappingGap(PARTICLES, MappingGap.Kind.STALE, entry.getKey(), "bedrock no longer has this particle"));
                continue;
            }
            if (entry.getValue().isJsonNull()) {
                continue;
            }
            final String javaIdentifier = entry.getValue().isJsonObject()
                    ? entry.getValue().getAsJsonObject().get("particle").getAsString()
                    : entry.getValue().getAsString();
            if (!javaParticles.contains(Key.namespaced(javaIdentifier))) {
                this.gaps.add(new MappingGap(PARTICLES, MappingGap.Kind.BROKEN, entry.getKey(), "java has no particle " + javaIdentifier));
            }
        }

        for (String bedrockIdentifier : bedrockParticles) {
            if (!mappings.has(bedrockIdentifier)) {
                this.gaps.add(new MappingGap(PARTICLES, MappingGap.Kind.MISSING, bedrockIdentifier, null));
            }
        }
    }

    /**
     * The plain {@code bedrock identifier -> java identifier} categories, which only differ in whether null means "deliberately unmapped".
     */
    private void checkIdentifierMapping(final String category, final Set<String> bedrockIdentifiers, final String mappingFile, final Set<String> javaIdentifiers, final boolean nullAllowed) {
        final JsonObject mappings = this.assets.json(mappingFile);

        for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
            if (!bedrockIdentifiers.contains(entry.getKey())) {
                this.gaps.add(new MappingGap(category, MappingGap.Kind.STALE, entry.getKey(), "bedrock no longer has this identifier"));
                continue;
            }
            if (entry.getValue().isJsonNull()) {
                if (!nullAllowed) {
                    this.gaps.add(new MappingGap(category, MappingGap.Kind.BROKEN, entry.getKey(), "null is not allowed in this category"));
                }
                continue;
            }
            final String javaIdentifier = entry.getValue().getAsString();
            if (!javaIdentifiers.contains(Key.namespaced(javaIdentifier))) {
                this.gaps.add(new MappingGap(category, MappingGap.Kind.BROKEN, entry.getKey(), "java has no " + javaIdentifier));
            }
        }

        for (String bedrockIdentifier : bedrockIdentifiers) {
            if (!mappings.has(bedrockIdentifier)) {
                this.gaps.add(new MappingGap(category, MappingGap.Kind.MISSING, bedrockIdentifier, null));
            }
        }
    }

    public Map<BlockState, BlockState> blockStateMappings() {
        return this.blockStateMappings;
    }

    public Set<BlockState> javaBlockStates() {
        return this.javaBlockStates;
    }

    public Set<String> javaBlocks() {
        return this.javaBlocks;
    }

    public List<BedrockBlockState> bedrockBlockStates() {
        return this.bedrockBlockStates;
    }

    public static Map<String, Map<MappingGap.Kind, Integer>> summarize(final List<MappingGap> gaps) {
        final Map<String, Map<MappingGap.Kind, Integer>> summary = new TreeMap<>();
        for (MappingGap gap : gaps) {
            summary.computeIfAbsent(gap.category(), key -> new TreeMap<>()).merge(gap.kind(), 1, Integer::sum);
        }
        return summary;
    }

}
