/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol.rewriter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.blockstate.BlockStateSanitizer;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.util.BlockStateHasher;
import net.raphimc.viabedrock.api.util.CombinationUtil;
import net.raphimc.viabedrock.api.util.HashedPaletteComparator;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.BlockProperties;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BlockStateRewriter implements StorableObject {

    public static final String TAG_WATER = "water";
    public static final String TAG_ITEM_FRAME = "item_frame";

    private final Int2IntMap blockStateIdMappings = new Int2IntOpenHashMap(); // Bedrock -> Java
    private final Int2IntMap legacyBlockStateIdMappings = new Int2IntOpenHashMap(); // Bedrock -> Bedrock
    private final BiMap<BlockState, Integer> blockStateMappings = HashBiMap.create(); // Bedrock -> Bedrock
    private final Int2ObjectMap<String> blockStateTags = new Int2ObjectOpenHashMap<>(); // Bedrock
    private final BlockStateSanitizer blockStateSanitizer;

    public BlockStateRewriter(final BlockProperties[] blockProperties, final boolean hashedRuntimeBlockIds) {
        this.blockStateIdMappings.defaultReturnValue(-1);
        this.legacyBlockStateIdMappings.defaultReturnValue(-1);

        final List<BedrockBlockState> bedrockBlockStates = new ArrayList<>(BedrockProtocol.MAPPINGS.getBedrockBlockStates());
        final List<BedrockBlockState> customBlockStates = new ArrayList<>();
        final Map<BlockState, Integer> javaBlockStates = BedrockProtocol.MAPPINGS.getJavaBlockStates();
        final Map<BlockState, BlockState> bedrockToJavaBlockStates = BedrockProtocol.MAPPINGS.getBedrockToJavaBlockStates();
        final Map<String, String> blockTags = BedrockProtocol.MAPPINGS.getBedrockBlockTags();

        for (BlockProperties blockProperty : blockProperties) {
            final Map<String, Set<Tag>> propertiesMap = new LinkedHashMap<>();
            if (blockProperty.properties().get("properties") instanceof ListTag) {
                final ListTag properties = blockProperty.properties().get("properties");
                if (CompoundTag.class.equals(properties.getElementType())) {
                    for (Tag propertyTag : properties) {
                        final CompoundTag property = (CompoundTag) propertyTag;
                        if (property.get("name") instanceof StringTag) {
                            final String name = property.<StringTag>get("name").getValue();
                            if (property.get("enum") instanceof ListTag) {
                                final ListTag enumTag = property.get("enum");
                                final Set<Tag> values = new LinkedHashSet<>();
                                for (Tag tag : enumTag) {
                                    values.add(tag);
                                }
                                propertiesMap.put(name, values);
                            }
                        }
                    }
                }
            }
            if (blockProperty.properties().get("traits") instanceof ListTag) { // https://wiki.bedrock.dev/blocks/block-traits.html
                final ListTag traits = blockProperty.properties().get("traits");
                if (CompoundTag.class.equals(traits.getElementType())) {
                    for (Tag traitTag : traits) {
                        final CompoundTag trait = (CompoundTag) traitTag;
                        if (trait.get("name") instanceof StringTag) {
                            final String name = Key.namespaced(trait.<StringTag>get("name").getValue());
                            final Map<String, Set<String>> traitStates = BedrockProtocol.MAPPINGS.getBedrockBlockTraits().get(name);
                            if (traitStates == null) {
                                throw new RuntimeException("Missing block trait states for " + name);
                            }

                            if (trait.get("enabled_states") instanceof CompoundTag) {
                                final CompoundTag enabledStatesTag = trait.get("enabled_states");
                                if (enabledStatesTag.size() != traitStates.size()) {
                                    throw new RuntimeException("Invalid enabled_states tag for trait " + name + " (size mismatch)");
                                }

                                for (Map.Entry<String, Tag> tag : enabledStatesTag) {
                                    final String key = Key.namespaced(tag.getKey());
                                    final boolean enabled = tag.getValue() instanceof ByteTag && ((ByteTag) tag.getValue()).asByte() != 0;
                                    if (enabled) {
                                        if (traitStates.containsKey(key)) {
                                            final Set<String> states = traitStates.get(key);
                                            final Set<Tag> values = new LinkedHashSet<>();
                                            for (String state : states) {
                                                values.add(new StringTag(state));
                                            }
                                            propertiesMap.put(key, values);
                                        } else {
                                            throw new RuntimeException("Missing block trait states for trait " + name + " and key " + key);
                                        }
                                    }
                                }
                            } else {
                                throw new RuntimeException("Missing enabled_states tag for trait " + name);
                            }
                        }
                    }
                }
            }

            final List<CompoundTag> combinations = CombinationUtil.generateCombinations(propertiesMap).stream()
                    .map(stringTagMap -> {
                        final CompoundTag combination = new CompoundTag();
                        for (Map.Entry<String, Tag> entry : stringTagMap.entrySet()) {
                            combination.put(entry.getKey(), entry.getValue().clone());
                        }
                        return combination;
                    }).collect(Collectors.toList());
            if (combinations.isEmpty()) {
                combinations.add(new CompoundTag());
            }

            for (CompoundTag combination : combinations) {
                final CompoundTag blockStateTag = new CompoundTag();
                blockStateTag.put("name", new StringTag(blockProperty.name()));
                blockStateTag.put("states", combination);
                blockStateTag.put("network_id", new IntTag(BlockStateHasher.hash(blockStateTag)));

                customBlockStates.add(BedrockBlockState.fromNbt(blockStateTag));
            }
        }

        bedrockBlockStates.addAll(customBlockStates);
        bedrockBlockStates.sort((a, b) -> HashedPaletteComparator.INSTANCE.compare(a.namespacedIdentifier(), b.namespacedIdentifier()));

        for (int i = 0; i < bedrockBlockStates.size(); i++) {
            final BedrockBlockState bedrockBlockState = bedrockBlockStates.get(i);
            final int bedrockId = hashedRuntimeBlockIds ? bedrockBlockState.blockStateTag().<IntTag>get("network_id").asInt() : i;

            this.blockStateMappings.put(bedrockBlockState, bedrockId);

            if (blockTags.containsKey(bedrockBlockState.namespacedIdentifier())) {
                this.blockStateTags.put(bedrockId, blockTags.get(bedrockBlockState.namespacedIdentifier()));
            }

            if (!bedrockToJavaBlockStates.containsKey(bedrockBlockState)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing bedrock -> java block state mapping: " + bedrockBlockState.toBlockStateString());
                continue;
            }

            final int javaId = javaBlockStates.get(bedrockToJavaBlockStates.get(bedrockBlockState));
            this.blockStateIdMappings.put(bedrockId, javaId);
        }

        for (BedrockBlockState customBlockState : customBlockStates) {
            final int bedrockId = this.blockStateMappings.get(customBlockState);
            final int javaId = javaBlockStates.get(bedrockToJavaBlockStates.get(BedrockBlockState.INFO_UPDATE));
            this.blockStateIdMappings.put(bedrockId, javaId);
        }

        for (Int2ObjectMap.Entry<BedrockBlockState> entry : BedrockProtocol.MAPPINGS.getBedrockLegacyBlockStates().int2ObjectEntrySet()) {
            final int legacyId = entry.getIntKey() >> 6;
            final int legacyData = entry.getIntKey() & 63;
            if (legacyData > 15) continue; // Dirty hack Mojang did in 1.12. Can be ignored safely as those values can't be used in chunk packets.

            this.legacyBlockStateIdMappings.put(legacyId << 4 | legacyData & 15, this.blockStateMappings.getOrDefault(entry.getValue(), -1).intValue());
        }

        this.blockStateSanitizer = new BlockStateSanitizer(bedrockBlockStates);
    }

    public int bedrockId(final CompoundTag bedrockBlockStateTag) {
        final CompoundTag bedrockBlockStateTagClone = bedrockBlockStateTag.clone();
        try {
            BedrockProtocol.MAPPINGS.getBedrockBlockStateUpgrader().upgradeToLatest(bedrockBlockStateTagClone);
            this.blockStateSanitizer.sanitize(bedrockBlockStateTagClone);

            return this.bedrockId(BedrockBlockState.fromNbt(bedrockBlockStateTagClone));
        } catch (Throwable e) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error while rewriting block state tag: " + bedrockBlockStateTag, e);
            return this.bedrockId(BedrockBlockState.AIR);
        }
    }

    public int bedrockId(final BlockState bedrockBlockState) {
        return this.blockStateMappings.getOrDefault(bedrockBlockState, -1);
    }

    public BlockState blockState(final int bedrockBlockStateId) {
        return this.blockStateMappings.inverse().get(bedrockBlockStateId);
    }

    public int bedrockId(final int legacyBlockStateId) {
        return this.legacyBlockStateIdMappings.get(legacyBlockStateId);
    }

    public int javaId(final int bedrockBlockStateId) {
        return this.blockStateIdMappings.get(bedrockBlockStateId);
    }

    public int waterlog(final int javaBlockStateId) {
        if (BedrockProtocol.MAPPINGS.getJavaPreWaterloggedBlockStates().contains(javaBlockStateId)) {
            return javaBlockStateId;
        }

        final BlockState waterlogged = BedrockProtocol.MAPPINGS.getJavaBlockStates().inverse().get(javaBlockStateId).withProperty("waterlogged", "true");
        return BedrockProtocol.MAPPINGS.getJavaBlockStates().getOrDefault(waterlogged, -1);
    }

    public String tag(final int bedrockBlockStateId) {
        return this.blockStateTags.get(bedrockBlockStateId);
    }

}
