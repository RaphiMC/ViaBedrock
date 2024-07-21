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
package net.raphimc.viabedrock.protocol.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteStreams;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.libs.fastutil.ints.*;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.api.chunk.blockstate.BlockStateUpgrader;
import net.raphimc.viabedrock.api.item.ItemUpgrader;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.model.ResourcePack;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.protocol.data.enums.MenuType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PackType;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class BedrockMappingData extends MappingDataBase {

    // Bedrock misc
    private ResourcePack bedrockVanillaResourcePack;
    private ResourcePack bedrockVanillaSkinPack;
    private Map<String, Object> bedrockGameRules;

    // Java misc
    private CompoundTag javaRegistries;
    private CompoundTag javaTags;
    private BiMap<String, Integer> javaCommandArgumentTypes;

    // Block states
    private BlockStateUpgrader bedrockBlockStateUpgrader;
    private BiMap<BlockState, Integer> javaBlockStates;
    private List<BedrockBlockState> bedrockBlockStates;
    private Map<BlockState, BlockState> bedrockToJavaBlockStates;
    private Map<String, String> bedrockBlockTags;
    private Map<String, Map<String, Set<String>>> bedrockBlockTraits;
    private BiMap<String, Integer> bedrockLegacyBlocks;
    private Int2ObjectMap<BedrockBlockState> bedrockLegacyBlockStates;
    private IntSet javaPreWaterloggedBlockStates;
    private Int2IntMap javaPottedBlockStates;
    private Map<String, IntSet> javaHeightMapBlockStates;

    // Biomes
    private CompoundTag bedrockBiomeDefinitions;
    private BiMap<String, Integer> bedrockBiomes;
    private BiMap<String, Integer> javaBiomes;
    private Map<String, Map<String, Object>> bedrockToJavaBiomeExtraData;

    // Items
    private ItemUpgrader bedrockItemUpgrader;
    private BiMap<String, Integer> javaItems;
    private BiMap<String, Integer> bedrockItems;
    private Map<String, String> bedrockItemTags;
    private Map<String, Map<BlockState, ItemRewriter.Rewriter>> bedrockToJavaBlockItems;
    private Map<String, Map<Integer, ItemRewriter.Rewriter>> bedrockToJavaMetaItems;
    private BiMap<String, Integer> javaMenus;

    // Entities
    private BiMap<String, Integer> bedrockEntities;
    private Map<String, EntityTypes1_20_5> bedrockToJavaEntities;
    private BiMap<String, Integer> javaBlockEntities;
    private BiMap<String, Integer> javaAttributes;

    // Effects
    private BiMap<String, Integer> javaEffects;
    private BiMap<String, Integer> bedrockEffects;
    private Map<String, String> bedrockToJavaEffects;

    // Other stuff
    private BiMap<String, String> bedrockToJavaExperimentalFeatures;
    private BiMap<String, String> bedrockToJavaBannerPatterns;
    private BiMap<String, String> bedrockToJavaPaintings;

    public BedrockMappingData() {
        super(BedrockProtocolVersion.bedrockLatest.getName(), ProtocolConstants.JAVA_VERSION.getName());
    }

    @Override
    public void load() {
        if (Via.getManager().isDebug()) {
            this.getLogger().info("Loading " + this.unmappedVersion + " -> " + this.mappedVersion + " mappings...");
        }

        final JsonObject javaViaMappingJson = this.readJson("java/via_mappings.json");

        { // Bedrock misc
            this.bedrockVanillaResourcePack = this.readResourcePack("bedrock/vanilla_resource_pack.mcpack", UUID.fromString("0575c61f-a5da-4b7f-9961-ffda2908861e"), "0.0.1");
            this.bedrockVanillaSkinPack = this.readResourcePack("bedrock/vanilla_skin_pack.mcpack", UUID.fromString("c18e65aa-7b21-4637-9b63-8ad63622ef01"), "1.0.0");
            this.bedrockVanillaSkinPack.setType(PackType.Skins);

            final JsonObject bedrockGameRulesJson = this.readJson("bedrock/game_rules.json");
            this.bedrockGameRules = new HashMap<>(bedrockGameRulesJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockGameRulesJson.entrySet()) {
                this.bedrockGameRules.put(entry.getKey().toLowerCase(Locale.ROOT), JsonUtil.getValue(entry.getValue()));
            }
        }

        { // Java misc
            this.javaRegistries = this.readNBT("java/registries.nbt");
            this.javaTags = this.readNBT("java/tags.nbt");

            final JsonArray javaCommandArgumentTypesJson = javaViaMappingJson.getAsJsonArray("argumenttypes");
            this.javaCommandArgumentTypes = HashBiMap.create(javaCommandArgumentTypesJson.size());
            for (int i = 0; i < javaCommandArgumentTypesJson.size(); i++) {
                this.javaCommandArgumentTypes.put(Key.namespaced(javaCommandArgumentTypesJson.get(i).getAsString()), i);
            }
            ArgumentTypeRegistry.init();
        }

        { // Block states
            this.bedrockBlockStateUpgrader = new BlockStateUpgrader();

            final JsonArray javaBlockStatesJson = javaViaMappingJson.getAsJsonArray("blockstates");
            this.javaBlockStates = HashBiMap.create(javaBlockStatesJson.size());
            for (int i = 0; i < javaBlockStatesJson.size(); i++) {
                final BlockState blockState = BlockState.fromString(javaBlockStatesJson.get(i).getAsString());
                this.javaBlockStates.put(blockState, i);
            }

            final ListTag<CompoundTag> bedrockBlockStatesTag = this.readNBT("bedrock/block_palette.nbt").getListTag("blocks", CompoundTag.class);
            this.bedrockBlockStates = new ArrayList<>(bedrockBlockStatesTag.size());
            for (CompoundTag tag : bedrockBlockStatesTag) {
                this.bedrockBlockStates.add(BedrockBlockState.fromNbt(tag));
            }

            final JsonObject bedrockToJavaBlockStateMappingsJson = this.readJson("custom/blockstate_mappings.json");
            this.bedrockToJavaBlockStates = new HashMap<>(bedrockToJavaBlockStateMappingsJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockToJavaBlockStateMappingsJson.entrySet()) {
                final BlockState bedrockBlockState = BlockState.fromString(entry.getKey());
                if (!this.bedrockBlockStates.contains(bedrockBlockState)) {
                    throw new RuntimeException("Unknown bedrock block state: " + bedrockBlockState.toBlockStateString());
                }
                final BlockState javaBlockState = BlockState.fromString(entry.getValue().getAsString());
                if (!this.javaBlockStates.containsKey(javaBlockState)) {
                    throw new RuntimeException("Unknown java block state: " + javaBlockState.toBlockStateString());
                }
                if (this.bedrockToJavaBlockStates.put(bedrockBlockState, javaBlockState) != null) {
                    throw new RuntimeException("Duplicate bedrock -> java block state mapping for " + bedrockBlockState.toBlockStateString());
                }
            }

            final JsonObject bedrockBlockTagsJson = this.readJson("custom/block_tags.json");
            this.bedrockBlockTags = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : bedrockBlockTagsJson.entrySet()) {
                final String tagName = entry.getKey();
                for (JsonElement tagValueJson : entry.getValue().getAsJsonArray()) {
                    final String bedrockIdentifier = tagValueJson.getAsString();
                    boolean contains = false;
                    for (BedrockBlockState bedrockBlockState : this.bedrockBlockStates) {
                        if (bedrockBlockState.namespacedIdentifier().equals(bedrockIdentifier)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        throw new RuntimeException("Unknown bedrock block: " + bedrockIdentifier);
                    }
                    if (this.bedrockBlockTags.put(bedrockIdentifier, tagName) != null) {
                        throw new RuntimeException("Duplicate bedrock block tag for " + bedrockIdentifier);
                    }
                }
            }

            final JsonObject bedrockBlockTraitsJson = this.readJson("bedrock/block_traits.json");
            this.bedrockBlockTraits = new HashMap<>(bedrockBlockTraitsJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockBlockTraitsJson.entrySet()) {
                final String traitName = entry.getKey();
                final JsonObject traitStatesJson = entry.getValue().getAsJsonObject();
                final Map<String, Set<String>> traitStates = new HashMap<>(traitStatesJson.size());
                for (Map.Entry<String, JsonElement> traitStatesEntry : traitStatesJson.entrySet()) {
                    final JsonArray statesJson = traitStatesEntry.getValue().getAsJsonArray();
                    final Set<String> states = new LinkedHashSet<>(statesJson.size());
                    for (JsonElement stateJson : statesJson) {
                        if (!states.add(stateJson.getAsString())) {
                            throw new RuntimeException("Duplicate bedrock block trait state for " + traitName + ": " + stateJson.getAsString());
                        }
                    }
                    traitStates.put(traitStatesEntry.getKey(), states);
                }
                if (this.bedrockBlockTraits.put(traitName, traitStates) != null) {
                    throw new RuntimeException("Duplicate bedrock block trait for " + traitName);
                }
            }

            final JsonObject bedrockLegacyBlocksJson = this.readJson("bedrock/block_legacy_id_map.json");
            this.bedrockLegacyBlocks = HashBiMap.create(bedrockLegacyBlocksJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockLegacyBlocksJson.entrySet()) {
                this.bedrockLegacyBlocks.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().getAsInt());
            }

            this.buildLegacyBlockStateMappings();

            final JsonArray javaPreWaterloggedBlockStatesJson = this.readJson("custom/pre_waterlogged_blockstates.json").getAsJsonArray("blockstates");
            this.javaPreWaterloggedBlockStates = new IntOpenHashSet(javaPreWaterloggedBlockStatesJson.size());
            for (JsonElement entry : javaPreWaterloggedBlockStatesJson) {
                final BlockState javaBlockState = BlockState.fromString(entry.getAsString());
                if (!this.javaBlockStates.containsKey(javaBlockState)) {
                    throw new RuntimeException("Unknown java block state: " + javaBlockState.toBlockStateString());
                }

                this.javaPreWaterloggedBlockStates.add(this.javaBlockStates.get(javaBlockState).intValue());
            }

            final JsonObject javaPottedBlockStatesJson = this.readJson("custom/potted_blockstates.json");
            this.javaPottedBlockStates = new Int2IntOpenHashMap(javaPottedBlockStatesJson.size());
            for (Map.Entry<String, JsonElement> entry : javaPottedBlockStatesJson.entrySet()) {
                final BlockState javaBlockState = BlockState.fromString(entry.getKey());
                if (!this.javaBlockStates.containsKey(javaBlockState)) {
                    throw new RuntimeException("Unknown java block state: " + javaBlockState.toBlockStateString());
                }
                final BlockState javaPottedBlockState = BlockState.fromString(entry.getValue().getAsString());
                if (!this.javaBlockStates.containsKey(javaPottedBlockState)) {
                    throw new RuntimeException("Unknown java block state: " + javaPottedBlockState.toBlockStateString());
                }

                this.javaPottedBlockStates.put(this.javaBlockStates.get(javaBlockState).intValue(), this.javaBlockStates.get(javaPottedBlockState).intValue());
            }

            final CompoundTag javaHeightMapBlockStatesTag = this.readNBT("java/heightmap_blockstates.nbt");
            this.javaHeightMapBlockStates = new HashMap<>(javaHeightMapBlockStatesTag.size());
            for (Map.Entry<String, Tag> entry : javaHeightMapBlockStatesTag.getValue().entrySet()) {
                final IntSet blockStates = new IntOpenHashSet();
                final IntArrayTag blockStatesArrayTag = (IntArrayTag) entry.getValue();
                for (int blockState : blockStatesArrayTag.getValue()) {
                    blockStates.add(blockState);
                }
                this.javaHeightMapBlockStates.put(entry.getKey(), blockStates);
            }
        }

        { // Biomes
            this.bedrockBiomeDefinitions = this.readNBT("bedrock/biome_definitions.nbt");

            final JsonObject bedrockBiomesJson = this.readJson("bedrock/biomes.json", JsonObject.class);
            this.bedrockBiomes = HashBiMap.create(bedrockBiomesJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockBiomesJson.entrySet()) {
                final String bedrockBiomeName = entry.getKey();
                if (!this.bedrockBiomeDefinitions.contains(bedrockBiomeName)) {
                    throw new RuntimeException("Unknown bedrock biome: " + bedrockBiomeName);
                }

                this.bedrockBiomes.put(bedrockBiomeName, entry.getValue().getAsInt());
            }

            for (String bedrockBiomeName : this.bedrockBiomeDefinitions.keySet()) {
                if (!this.bedrockBiomes.containsKey(bedrockBiomeName)) {
                    throw new RuntimeException("Missing bedrock biome id mapping: " + bedrockBiomeName);
                }
            }

            this.javaBiomes = HashBiMap.create(this.bedrockBiomes.size());
            this.javaBiomes.put("the_void", 0);
            for (String bedrockBiomeName : this.bedrockBiomes.keySet()) {
                this.javaBiomes.put(bedrockBiomeName, this.javaBiomes.size());
            }

            final JsonObject bedrockToJavaBiomeExtraDataJson = this.readJson("custom/biome_extra_data.json");
            this.bedrockToJavaBiomeExtraData = new HashMap<>(bedrockToJavaBiomeExtraDataJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockToJavaBiomeExtraDataJson.entrySet()) {
                final String dataName = entry.getKey();
                final JsonObject extraDataJson = entry.getValue().getAsJsonObject();
                final Map<String, Object> extraData = new HashMap<>(extraDataJson.size());
                for (Map.Entry<String, JsonElement> extraDataEntry : extraDataJson.entrySet()) {
                    final JsonPrimitive primitive = extraDataEntry.getValue().getAsJsonPrimitive();
                    if (primitive.isString()) {
                        extraData.put(extraDataEntry.getKey(), primitive.getAsString());
                    } else if (primitive.isNumber()) {
                        extraData.put(extraDataEntry.getKey(), primitive.getAsNumber().intValue());
                    } else if (primitive.isBoolean()) {
                        extraData.put(extraDataEntry.getKey(), primitive.getAsBoolean());
                    } else {
                        throw new IllegalArgumentException("Unknown extra data type: " + extraDataEntry.getValue().getClass().getName());
                    }
                }
                this.bedrockToJavaBiomeExtraData.put(dataName, extraData);
            }
        }

        { // Items
            this.bedrockItemUpgrader = new ItemUpgrader();

            final JsonArray javaItemsJson = javaViaMappingJson.get("items").getAsJsonArray();
            this.javaItems = HashBiMap.create(javaItemsJson.size());
            for (int i = 0; i < javaItemsJson.size(); i++) {
                this.javaItems.put(Key.namespaced(javaItemsJson.get(i).getAsString()), i);
            }

            final JsonArray bedrockItemsJson = this.readJson("bedrock/runtime_item_states.json", JsonArray.class);
            this.bedrockItems = HashBiMap.create(bedrockItemsJson.size());
            for (JsonElement entry : bedrockItemsJson) {
                final JsonObject itemEntry = entry.getAsJsonObject();
                final String identifier = itemEntry.get("name").getAsString();
                final int id = itemEntry.get("id").getAsInt();
                this.bedrockItems.put(identifier, id);
            }

            final JsonObject bedrockItemTagsJson = this.readJson("custom/item_tags.json");
            this.bedrockItemTags = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : bedrockItemTagsJson.entrySet()) {
                final String tagName = entry.getKey();
                for (JsonElement tagValueJson : entry.getValue().getAsJsonArray()) {
                    final String bedrockIdentifier = tagValueJson.getAsString();
                    if (!this.bedrockItems.containsKey(bedrockIdentifier)) {
                        throw new RuntimeException("Unknown bedrock item: " + bedrockIdentifier);
                    }
                    if (this.bedrockItemTags.put(bedrockIdentifier, tagName) != null) {
                        throw new RuntimeException("Duplicate bedrock item tag for " + bedrockIdentifier);
                    }
                }
            }

            final JsonObject bedrockItemMappingsJson = this.readJson("custom/item_mappings.json");
            this.bedrockToJavaBlockItems = new HashMap<>(bedrockItemMappingsJson.size());
            this.bedrockToJavaMetaItems = new HashMap<>(bedrockItemMappingsJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockItemMappingsJson.entrySet()) {
                final String bedrockIdentifier = entry.getKey();
                if (!this.bedrockItems.containsKey(bedrockIdentifier)) {
                    throw new RuntimeException("Unknown bedrock item: " + bedrockIdentifier);
                }
                final JsonObject definition = entry.getValue().getAsJsonObject();
                if (definition.has("block")) {
                    if (this.bedrockItems.get(bedrockIdentifier) > 255) {
                        throw new RuntimeException("Tried to register meta item as block item: " + bedrockIdentifier);
                    }

                    final JsonObject blockDefinition = definition.get("block").getAsJsonObject();
                    final Map<BlockState, ItemRewriter.Rewriter> blockItems = new LinkedHashMap<>(blockDefinition.size());
                    this.bedrockToJavaBlockItems.put(bedrockIdentifier, blockItems);
                    final List<BlockState> allPossibleStates = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> blockMapping : blockDefinition.entrySet()) {
                        final BlockState blockState = BlockState.fromString(blockMapping.getKey());
                        final String blockStateIdentifier = blockState.namespacedIdentifier();
                        final List<BlockState> blockStates = new ArrayList<>();
                        for (BedrockBlockState bedrockBlockState : this.bedrockBlockStates) {
                            if (bedrockBlockState.namespacedIdentifier().equals(blockStateIdentifier)) {
                                if (!bedrockBlockState.properties().keySet().containsAll(blockState.properties().keySet())) {
                                    throw new RuntimeException("Unknown bedrock block state property: " + blockState.properties().keySet() + " for " + blockStateIdentifier);
                                }
                                if (bedrockBlockState.properties().entrySet().containsAll(blockState.properties().entrySet())) {
                                    blockStates.add(bedrockBlockState);
                                }
                                allPossibleStates.add(bedrockBlockState);
                            }
                        }
                        if (blockStates.isEmpty()) {
                            throw new RuntimeException("Unknown bedrock block state: " + blockState.toBlockStateString());
                        }

                        for (BlockState state : blockStates) {
                            if (blockItems.put(state, ItemRewriter.Rewriter.fromJson(bedrockIdentifier, blockMapping.getValue().getAsJsonObject())) != null) {
                                throw new RuntimeException("Duplicate bedrock -> java item mapping for " + bedrockIdentifier);
                            }
                        }
                    }

                    /*for (BlockState state : allPossibleStates) {
                        if (!blockItems.containsKey(state)) {
                            throw new RuntimeException("Missing bedrock -> java item mapping for " + state.toBlockStateString());
                        }
                    }*/
                } else if (definition.has("meta")) {
                    if (this.bedrockItems.get(bedrockIdentifier) < 256) {
                        throw new RuntimeException("Tried to register block item as meta item: " + bedrockIdentifier);
                    }

                    final JsonObject metaDefinition = definition.get("meta").getAsJsonObject();
                    final Map<Integer, ItemRewriter.Rewriter> metaItems = new HashMap<>(metaDefinition.size());
                    this.bedrockToJavaMetaItems.put(bedrockIdentifier, metaItems);
                    for (Map.Entry<String, JsonElement> metaMapping : metaDefinition.entrySet()) {
                        Integer meta;
                        try {
                            meta = Integer.parseInt(metaMapping.getKey());
                        } catch (NumberFormatException e) {
                            meta = null;
                        }

                        if (metaItems.put(meta, ItemRewriter.Rewriter.fromJson(bedrockIdentifier, metaMapping.getValue().getAsJsonObject())) != null) {
                            throw new RuntimeException("Duplicate bedrock -> java item mapping for " + bedrockIdentifier + ":" + meta);
                        }
                    }

                    if (!metaItems.containsKey(null)) {
                        throw new RuntimeException("Missing bedrock -> java item mapping for " + bedrockIdentifier + ":null");
                    }
                    if (metaItems.size() > 1 && !metaItems.containsKey(0)) {
                        throw new RuntimeException("Missing bedrock -> java item mapping for " + bedrockIdentifier + ":0");
                    }
                } else {
                    throw new RuntimeException("Unknown item mapping definition: " + definition);
                }
            }

            for (Map.Entry<String, Map<Integer, ItemRewriter.Rewriter>> entry : this.bedrockToJavaMetaItems.entrySet()) {
                final String bedrockIdentifier = entry.getKey();
                for (Map.Entry<Integer, ItemRewriter.Rewriter> metaEntry : entry.getValue().entrySet()) {
                    final Integer meta = metaEntry.getKey();
                    if (meta != null) {
                        final String newBedrockIdentifier = this.bedrockItemUpgrader.upgradeMetaItem(bedrockIdentifier, meta);
                        if (newBedrockIdentifier != null) {
                            if (newBedrockIdentifier.equals(metaEntry.getValue().identifier())) {
                                throw new RuntimeException("Redundant bedrock -> java item mapping for " + bedrockIdentifier + ":" + meta);
                            } else {
                                throw new RuntimeException("Upgraded " + bedrockIdentifier + ":" + meta + " to " + newBedrockIdentifier + " but it was mapped to " + metaEntry.getValue().identifier());
                            }
                        }
                    }
                }
            }

            for (String bedrockIdentifier : this.bedrockItems.keySet()) {
                if (!this.bedrockToJavaBlockItems.containsKey(bedrockIdentifier) && !this.bedrockToJavaMetaItems.containsKey(bedrockIdentifier)) {
                    throw new RuntimeException("Missing bedrock -> java item mapping for " + bedrockIdentifier);
                }
            }

            final JsonArray javaMenusJson = javaViaMappingJson.get("menus").getAsJsonArray();
            this.javaMenus = HashBiMap.create(javaMenusJson.size());
            for (int i = 0; i < javaMenusJson.size(); i++) {
                this.javaMenus.put(Key.namespaced(javaMenusJson.get(i).getAsString()), i);
            }
            // noinspection ResultOfMethodCallIgnored
            MenuType.values(); // Initialize the enum
        }

        { // Entities
            final CompoundTag entityIdentifiersTag = this.readNBT("bedrock/entity_identifiers.nbt");
            final ListTag<CompoundTag> entityIdentifiersListTag = entityIdentifiersTag.getListTag("idlist", CompoundTag.class);
            this.bedrockEntities = HashBiMap.create(entityIdentifiersListTag.size());
            for (CompoundTag entry : entityIdentifiersListTag) {
                this.bedrockEntities.put(entry.getStringTag("id").getValue(), entry.getIntTag("rid").asInt());
            }

            final JsonObject bedrockToJavaEntityMappingsJson = this.readJson("custom/entity_mappings.json");
            this.bedrockToJavaEntities = new HashMap<>(bedrockToJavaEntityMappingsJson.size());
            final Set<String> unmappedIdentifiers = new HashSet<>();
            for (Map.Entry<String, JsonElement> entry : bedrockToJavaEntityMappingsJson.entrySet()) {
                final String bedrockIdentifier = entry.getKey();
                if (!this.bedrockEntities.containsKey(bedrockIdentifier)) {
                    throw new RuntimeException("Unknown bedrock entity identifier: " + bedrockIdentifier);
                }

                final String javaIdentifier = entry.getValue().getAsString();
                if (javaIdentifier.isEmpty()) {
                    unmappedIdentifiers.add(bedrockIdentifier);
                    continue;
                }
                EntityTypes1_20_5 javaEntityType = null;
                for (EntityTypes1_20_5 type : EntityTypes1_20_5.values()) {
                    if (!type.isAbstractType() && type.identifier().equals(javaIdentifier)) {
                        javaEntityType = type;
                        break;
                    }
                }
                if (javaEntityType == null) {
                    throw new RuntimeException("Unknown java entity identifier: " + javaIdentifier);
                }

                this.bedrockToJavaEntities.put(bedrockIdentifier, javaEntityType);
            }

            for (String bedrockIdentifier : this.bedrockEntities.keySet()) {
                if (!this.bedrockToJavaEntities.containsKey(bedrockIdentifier) && !unmappedIdentifiers.contains(bedrockIdentifier)) {
                    throw new RuntimeException("Missing bedrock -> java entity mapping for " + bedrockIdentifier);
                }
            }

            final JsonArray javaBlockEntitiesJson = javaViaMappingJson.get("blockentities").getAsJsonArray();
            this.javaBlockEntities = HashBiMap.create(javaBlockEntitiesJson.size());
            for (int i = 0; i < javaBlockEntitiesJson.size(); i++) {
                this.javaBlockEntities.put(javaBlockEntitiesJson.get(i).getAsString(), i);
            }

            final JsonArray javaAttributesJson = javaViaMappingJson.get("attributes").getAsJsonArray();
            this.javaAttributes = HashBiMap.create(javaAttributesJson.size());
            for (int i = 0; i < javaAttributesJson.size(); i++) {
                this.javaAttributes.put(Key.namespaced(javaAttributesJson.get(i).getAsString()), i);
            }
        }

        { // Effects
            final JsonArray javaEffectsJson = this.readJson("java/effects.json", JsonArray.class);
            this.javaEffects = HashBiMap.create(javaEffectsJson.size());
            for (int i = 0; i < javaEffectsJson.size(); i++) {
                this.javaEffects.put(javaEffectsJson.get(i).getAsString(), i + 1);
            }

            final JsonArray bedrockEffectsJson = this.readJson("bedrock/effects.json", JsonArray.class);
            this.bedrockEffects = HashBiMap.create(bedrockEffectsJson.size());
            for (int i = 0; i < bedrockEffectsJson.size(); i++) {
                this.bedrockEffects.put(bedrockEffectsJson.get(i).getAsString(), i + 1);
            }

            final JsonObject bedrockToJavaEffectMappingsJson = this.readJson("custom/effect_mappings.json");
            this.bedrockToJavaEffects = new HashMap<>(bedrockToJavaEffectMappingsJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockToJavaEffectMappingsJson.entrySet()) {
                final String bedrockIdentifier = entry.getKey();
                if (!this.bedrockEffects.containsKey(bedrockIdentifier)) {
                    throw new IllegalStateException("Unknown bedrock effect: " + bedrockIdentifier);
                }
                final String javaIdentifier = entry.getValue().getAsString();
                if (!this.javaEffects.containsKey(javaIdentifier)) {
                    throw new IllegalStateException("Unknown java effect: " + javaIdentifier);
                }
                this.bedrockToJavaEffects.put(bedrockIdentifier, javaIdentifier);
            }

            for (String bedrockIdentifier : this.bedrockEffects.keySet()) {
                if (!this.bedrockToJavaEffects.containsKey(bedrockIdentifier)) {
                    throw new IllegalStateException("Missing bedrock -> java effect mapping for " + bedrockIdentifier);
                }
            }
        }

        { // Other stuff
            final JsonObject bedrockToJavaExperimentalFeatureMappingsJson = this.readJson("custom/experimental_feature_mappings.json");
            this.bedrockToJavaExperimentalFeatures = HashBiMap.create(bedrockToJavaExperimentalFeatureMappingsJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockToJavaExperimentalFeatureMappingsJson.entrySet()) {
                this.bedrockToJavaExperimentalFeatures.put(entry.getKey(), entry.getValue().getAsString());
            }

            final JsonObject bedrockToJavaBannerPatternMappingsJson = this.readJson("custom/banner_pattern_mappings.json");
            this.bedrockToJavaBannerPatterns = HashBiMap.create(bedrockToJavaBannerPatternMappingsJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockToJavaBannerPatternMappingsJson.entrySet()) {
                final String javaIdentifier = entry.getValue().getAsString();
                if (!this.javaRegistries.getCompoundTag("minecraft:banner_pattern").contains(javaIdentifier)) {
                    throw new RuntimeException("Unknown java banner pattern: " + javaIdentifier);
                }
                this.bedrockToJavaBannerPatterns.put(entry.getKey(), javaIdentifier);
            }

            final JsonObject bedrockToJavaPaintingMappingsJson = this.readJson("custom/painting_mappings.json");
            this.bedrockToJavaPaintings = HashBiMap.create(bedrockToJavaPaintingMappingsJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockToJavaPaintingMappingsJson.entrySet()) {
                final String javaIdentifier = entry.getValue().getAsString();
                if (!this.javaRegistries.getCompoundTag("minecraft:painting_variant").contains(javaIdentifier)) {
                    throw new RuntimeException("Unknown java painting: " + javaIdentifier);
                }
                this.bedrockToJavaPaintings.put(entry.getKey(), javaIdentifier);
            }
        }
    }

    public ResourcePack getBedrockVanillaResourcePack() {
        return this.bedrockVanillaResourcePack;
    }

    public ResourcePack getBedrockVanillaSkinPack() {
        return this.bedrockVanillaSkinPack;
    }

    public Map<String, Object> getBedrockGameRules() {
        return this.bedrockGameRules;
    }

    public CompoundTag getJavaRegistries() {
        return this.javaRegistries;
    }

    public CompoundTag getJavaTags() {
        return this.javaTags;
    }

    public BiMap<String, Integer> getJavaCommandArgumentTypes() {
        return this.javaCommandArgumentTypes;
    }

    public BlockStateUpgrader getBedrockBlockStateUpgrader() {
        return this.bedrockBlockStateUpgrader;
    }

    public BiMap<BlockState, Integer> getJavaBlockStates() {
        return this.javaBlockStates;
    }

    public List<BedrockBlockState> getBedrockBlockStates() {
        return this.bedrockBlockStates;
    }

    public Map<BlockState, BlockState> getBedrockToJavaBlockStates() {
        return this.bedrockToJavaBlockStates;
    }

    public Map<String, String> getBedrockBlockTags() {
        return this.bedrockBlockTags;
    }

    public Map<String, Map<String, Set<String>>> getBedrockBlockTraits() {
        return this.bedrockBlockTraits;
    }

    public BiMap<String, Integer> getBedrockLegacyBlocks() {
        return this.bedrockLegacyBlocks;
    }

    public Int2ObjectMap<BedrockBlockState> getBedrockLegacyBlockStates() {
        return this.bedrockLegacyBlockStates;
    }

    public IntSet getJavaPreWaterloggedBlockStates() {
        return this.javaPreWaterloggedBlockStates;
    }

    public Int2IntMap getJavaPottedBlockStates() {
        return this.javaPottedBlockStates;
    }

    public Map<String, IntSet> getJavaHeightMapBlockStates() {
        return this.javaHeightMapBlockStates;
    }

    public CompoundTag getBedrockBiomeDefinitions() {
        return this.bedrockBiomeDefinitions;
    }

    public BiMap<String, Integer> getBedrockBiomes() {
        return this.bedrockBiomes;
    }

    public BiMap<String, Integer> getJavaBiomes() {
        return this.javaBiomes;
    }

    public Map<String, Map<String, Object>> getBedrockToJavaBiomeExtraData() {
        return this.bedrockToJavaBiomeExtraData;
    }

    public ItemUpgrader getBedrockItemUpgrader() {
        return this.bedrockItemUpgrader;
    }

    public BiMap<String, Integer> getJavaItems() {
        return this.javaItems;
    }

    public BiMap<String, Integer> getBedrockItems() {
        return this.bedrockItems;
    }

    public Map<String, String> getBedrockItemTags() {
        return this.bedrockItemTags;
    }

    public Map<String, Map<BlockState, ItemRewriter.Rewriter>> getBedrockToJavaBlockItems() {
        return this.bedrockToJavaBlockItems;
    }

    public Map<String, Map<Integer, ItemRewriter.Rewriter>> getBedrockToJavaMetaItems() {
        return this.bedrockToJavaMetaItems;
    }

    public BiMap<String, Integer> getJavaMenus() {
        return this.javaMenus;
    }

    public BiMap<String, Integer> getBedrockEntities() {
        return this.bedrockEntities;
    }

    public Map<String, EntityTypes1_20_5> getBedrockToJavaEntities() {
        return this.bedrockToJavaEntities;
    }

    public BiMap<String, Integer> getJavaBlockEntities() {
        return this.javaBlockEntities;
    }

    public BiMap<String, Integer> getJavaAttributes() {
        return this.javaAttributes;
    }

    public BiMap<String, Integer> getJavaEffects() {
        return this.javaEffects;
    }

    public BiMap<String, Integer> getBedrockEffects() {
        return this.bedrockEffects;
    }

    public Map<String, String> getBedrockToJavaEffects() {
        return this.bedrockToJavaEffects;
    }

    public BiMap<String, String> getBedrockToJavaExperimentalFeatures() {
        return this.bedrockToJavaExperimentalFeatures;
    }

    public BiMap<String, String> getBedrockToJavaBannerPatterns() {
        return this.bedrockToJavaBannerPatterns;
    }

    public BiMap<String, String> getBedrockToJavaPaintings() {
        return this.bedrockToJavaPaintings;
    }

    @Override
    protected Logger getLogger() {
        return ViaBedrock.getPlatform().getLogger();
    }

    private ResourcePack readResourcePack(String file, final UUID uuid, final String version) {
        file = "assets/viabedrock/data/" + file;
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            final byte[] bytes = ByteStreams.toByteArray(inputStream);
            final ResourcePack resourcePack = new ResourcePack(uuid, version, "", "", "", false, false, 0, PackType.Resources);
            resourcePack.setCompressedDataLength(bytes.length, bytes.length);
            resourcePack.processDataChunk(0, bytes);
            return resourcePack;
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Could not read " + file, e);
            return null;
        }
    }

    private CompoundTag readNBT(String file) {
        file = "assets/viabedrock/data/" + file;
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open " + file);
                return null;
            }

            return NBTIO.readTag(new DataInputStream(new GZIPInputStream(inputStream)), TagLimiter.noop(), true, CompoundTag.class);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not read " + file, e);
            return null;
        }
    }

    private JsonObject readJson(String file) {
        return this.readJson(file, JsonObject.class);
    }

    private <T> T readJson(String file, final Class<T> classOfT) {
        file = "assets/viabedrock/data/" + file;
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open " + file);
                return null;
            }

            return GsonUtil.getGson().fromJson(new InputStreamReader(inputStream), classOfT);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not read " + file, e);
            return null;
        }
    }

    private void buildLegacyBlockStateMappings() {
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("assets/viabedrock/data/bedrock/block_id_meta_to_1_12_0_nbt.bin")) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open block_id_meta_to_1_12_0_nbt.bin");
                return;
            }
            final byte[] bytes = ByteStreams.toByteArray(inputStream);
            final ByteBuf buf = Unpooled.wrappedBuffer(bytes);

            this.bedrockLegacyBlockStates = new Int2ObjectOpenHashMap<>();
            final int blockCount = BedrockTypes.UNSIGNED_VAR_INT.read(buf);
            for (int i = 0; i < blockCount; i++) {
                final String identifier = BedrockTypes.STRING.read(buf).toLowerCase(Locale.ROOT);
                if (!this.bedrockLegacyBlocks.containsKey(identifier)) {
                    throw new RuntimeException("Unknown block identifier in block_id_meta_to_1_12_0_nbt.bin: " + identifier);
                }
                final int id = this.bedrockLegacyBlocks.get(identifier);

                final int metaCount = BedrockTypes.UNSIGNED_VAR_INT.read(buf);
                for (int i1 = 0; i1 < metaCount; i1++) {
                    final int metadata = BedrockTypes.UNSIGNED_VAR_INT.read(buf);
                    final CompoundTag tag = (CompoundTag) BedrockTypes.TAG_LE.read(buf);
                    this.bedrockBlockStateUpgrader.upgradeToLatest(tag);
                    final BedrockBlockState bedrockBlockState = BedrockBlockState.fromNbt(tag);
                    if (!this.bedrockBlockStates.contains(bedrockBlockState)) {
                        throw new RuntimeException("Legacy block state " + bedrockBlockState.toBlockStateString() + " is not mapped to a modern block state");
                    }

                    this.bedrockLegacyBlockStates.put(id << 6 | metadata & 63, bedrockBlockState);
                }
            }
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Could not read block_id_meta_to_1_12_0_nbt.bin", e);
            this.bedrockLegacyBlockStates = null;
        }
    }

}
