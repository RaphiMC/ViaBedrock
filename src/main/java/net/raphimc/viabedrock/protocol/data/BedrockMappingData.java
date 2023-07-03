/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.fastutil.ints.*;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.opennbt.NBTIO;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viaversion.util.GsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.api.chunk.block_state.BlockStateUpgrader;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.model.ResourcePack;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class BedrockMappingData extends MappingDataBase {

    // Bedrock misc
    private ResourcePack bedrockVanillaResourcePack;
    private BufferedImage bedrockSteveSkin;
    private JsonObject bedrockSkinGeometry;

    // Java misc
    private CompoundTag javaRegistries;
    private CompoundTag javaTags;

    // Block states
    private BlockStateUpgrader bedrockBlockStateUpgrader;
    private BiMap<BlockState, Integer> javaBlockStates;
    private List<BedrockBlockState> bedrockBlockStates;
    private Map<BlockState, BlockState> bedrockToJavaBlockStates;
    private IntSet javaPreWaterloggedStates;
    private Int2IntMap javaPottedBlockStates;
    private BiMap<String, Integer> bedrockLegacyBlocks;
    private Int2ObjectMap<BedrockBlockState> bedrockLegacyBlockStates;
    private Map<String, String> bedrockBlockTags;

    // Biomes
    private Map<String, CompoundTag> bedrockBiomeDefinitions;
    private BiMap<String, Integer> bedrockBiomes;
    private Map<String, Map<String, Object>> bedrockToJavaBiomeExtraData;

    // Items
    private BiMap<String, Integer> bedrockItems;
    private Map<String, String> bedrockToJavaItems;

    // Entities
    private BiMap<String, Integer> bedrockEntities;
    private Map<String, Entity1_19_4Types> bedrockToJavaEntities;
    private BiMap<String, Integer> javaBlockEntities;

    // Effects
    private BiMap<String, Integer> javaEffects;
    private BiMap<String, Integer> bedrockEffects;
    private Map<String, String> bedrockToJavaEffects;

    public BedrockMappingData() {
        super(BedrockProtocolVersion.bedrockLatest.getName(), ProtocolVersion.v1_20.getName());
    }

    @Override
    public void load() {
        if (Via.getManager().isDebug()) {
            this.getLogger().info("Loading " + this.unmappedVersion + " -> " + this.mappedVersion + " mappings...");
        }

        { // Bedrock misc
            this.bedrockVanillaResourcePack = this.readResourcePack("bedrock/vanilla.mcpack", UUID.fromString("0575c61f-a5da-4b7f-9961-ffda2908861e"), "0.0.1");
            this.bedrockSteveSkin = this.readImage("bedrock/skin/steve.png");
            this.bedrockSkinGeometry = JsonUtil.sort(this.readJson("bedrock/skin/geometry.json"), Comparator.naturalOrder());
        }

        { // Java misc
            this.javaRegistries = this.readNBT("java/registries.nbt");
            this.javaTags = this.readNBT("java/tags.nbt");
        }

        final JsonObject javaMapping1_20Json = this.readJson("java/mapping-1.20.json");

        { // Block states
            this.bedrockBlockStateUpgrader = new BlockStateUpgrader();

            final JsonArray javaBlockStatesJson = javaMapping1_20Json.getAsJsonArray("blockstates");
            this.javaBlockStates = HashBiMap.create(javaBlockStatesJson.size());
            for (int i = 0; i < javaBlockStatesJson.size(); i++) {
                final BlockState blockState = BlockState.fromString(javaBlockStatesJson.get(i).getAsString());
                this.javaBlockStates.put(blockState, i);
            }

            final ListTag bedrockBlockStatesTag = this.readNBT("bedrock/block_palette.1_20_0.nbt").get("blocks");
            this.bedrockBlockStates = new ArrayList<>(bedrockBlockStatesTag.size());
            for (Tag tag : bedrockBlockStatesTag.getValue()) {
                this.bedrockBlockStates.add(BedrockBlockState.fromNbt((CompoundTag) tag));
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
                    throw new RuntimeException("Duplicate bedrock -> java block mapping for " + bedrockBlockState.toBlockStateString());
                }
            }

            final JsonArray javaPreWaterloggedStatesJson = this.readJson("custom/pre_waterlogged_states.json").getAsJsonArray("blockstates");
            this.javaPreWaterloggedStates = new IntOpenHashSet(javaPreWaterloggedStatesJson.size());
            for (JsonElement entry : javaPreWaterloggedStatesJson) {
                final BlockState javaBlockState = BlockState.fromString(entry.getAsString());
                if (!this.javaBlockStates.containsKey(javaBlockState)) {
                    throw new RuntimeException("Unknown java block state: " + javaBlockState.toBlockStateString());
                }

                this.javaPreWaterloggedStates.add(this.javaBlockStates.get(javaBlockState).intValue());
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

            final JsonObject bedrockLegacyBlocksJson = this.readJson("bedrock/block_legacy_id_map.json");
            this.bedrockLegacyBlocks = HashBiMap.create(bedrockLegacyBlocksJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockLegacyBlocksJson.entrySet()) {
                this.bedrockLegacyBlocks.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().getAsInt());
            }

            this.buildLegacyBlockStateMappings();

            final JsonObject bedrockBlockTagsJson = this.readJson("custom/block_tags.json");
            this.bedrockBlockTags = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : bedrockBlockTagsJson.entrySet()) {
                final String tagName = entry.getKey();
                for (JsonElement tagValueJson : entry.getValue().getAsJsonArray()) {
                    this.bedrockBlockTags.put(tagValueJson.getAsString(), tagName);
                }
            }
        }

        { // Biomes
            final CompoundTag bedrockBiomeDefinitionsTag = this.readNBT("bedrock/biome_definitions.nbt");
            this.bedrockBiomeDefinitions = new HashMap<>(bedrockBiomeDefinitionsTag.size());
            for (Map.Entry<String, Tag> entry : bedrockBiomeDefinitionsTag.getValue().entrySet()) {
                this.bedrockBiomeDefinitions.put(entry.getKey(), (CompoundTag) entry.getValue());
            }

            final JsonObject bedrockBiomesJson = this.readJson("bedrock/biomes.json", JsonObject.class);
            this.bedrockBiomes = HashBiMap.create(bedrockBiomesJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockBiomesJson.entrySet()) {
                final String bedrockBiomeName = entry.getKey();
                if (!this.bedrockBiomeDefinitions.containsKey(bedrockBiomeName)) {
                    throw new RuntimeException("Unknown bedrock biome: " + bedrockBiomeName);
                }

                this.bedrockBiomes.put(bedrockBiomeName, entry.getValue().getAsInt());
            }

            for (String bedrockBiomeName : this.bedrockBiomeDefinitions.keySet()) {
                if (!this.bedrockBiomes.containsKey(bedrockBiomeName)) {
                    throw new RuntimeException("Missing bedrock biome id mapping: " + bedrockBiomeName);
                }
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
            final JsonArray bedrockItemsJson = this.readJson("bedrock/runtime_item_states.1_20_0.json", JsonArray.class);
            this.bedrockItems = HashBiMap.create(bedrockItemsJson.size());
            for (JsonElement entry : bedrockItemsJson) {
                final JsonObject itemEntry = entry.getAsJsonObject();
                final String identifier = itemEntry.get("name").getAsString();
                final int id = itemEntry.get("id").getAsInt();
                this.bedrockItems.put(identifier, id);
            }

            final JsonObject bedrockItemMappingsJson = this.readJson("custom/item_mappings.json");
            this.bedrockToJavaItems = new HashMap<>(bedrockItemMappingsJson.size());
            for (Map.Entry<String, JsonElement> entry : bedrockItemMappingsJson.entrySet()) {
                final String bedrockIdentifier = entry.getKey();
                final String javaIdentifier = entry.getValue().getAsString();
                this.bedrockToJavaItems.put(bedrockIdentifier, javaIdentifier);
            }
        }

        { // Entities
            final CompoundTag entityIdentifiersTag = this.readNBT("bedrock/entity_identifiers.nbt");
            final ListTag entityIdentifiersListTag = entityIdentifiersTag.get("idlist");
            this.bedrockEntities = HashBiMap.create(entityIdentifiersListTag.size());
            for (Tag tag : entityIdentifiersListTag) {
                final CompoundTag entry = (CompoundTag) tag;
                this.bedrockEntities.put(entry.<StringTag>get("id").getValue(), entry.<IntTag>get("rid").getValue());
            }

            Via.getManager().getProtocolManager().addMappingLoaderFuture(BedrockProtocol.class, Protocol1_19_4To1_19_3.class, () -> {
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
                    Entity1_19_4Types javaEntityType = null;
                    for (Entity1_19_4Types type : Entity1_19_4Types.values()) {
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
            });

            final JsonArray javaBlockEntitiesJson = javaMapping1_20Json.get("blockentities").getAsJsonArray();
            this.javaBlockEntities = HashBiMap.create(javaBlockEntitiesJson.size());
            for (int i = 0; i < javaBlockEntitiesJson.size(); i++) {
                this.javaBlockEntities.put(javaBlockEntitiesJson.get(i).getAsString(), i);
            }
        }

        { // Effects
            final JsonArray javaEffectsJson = this.readJson("java/effects.json", JsonArray.class);
            this.javaEffects = HashBiMap.create(javaEffectsJson.size());
            for (int i = 0; i < javaEffectsJson.size(); i++) {
                this.javaEffects.put(javaEffectsJson.get(i).getAsString(), i);
            }

            final JsonArray bedrockEffectsJson = this.readJson("bedrock/effects.json", JsonArray.class);
            this.bedrockEffects = HashBiMap.create(bedrockEffectsJson.size());
            for (int i = 0; i < bedrockEffectsJson.size(); i++) {
                this.bedrockEffects.put(bedrockEffectsJson.get(i).getAsString(), i);
            }

            this.bedrockToJavaEffects = new HashMap<>(this.bedrockEffects.size());
            for (String bedrockIdentifier : this.bedrockEffects.keySet()) {
                if (this.javaEffects.containsKey(bedrockIdentifier)) {
                    this.bedrockToJavaEffects.put(bedrockIdentifier, bedrockIdentifier);
                }
            }

            final JsonObject effectMappingsJson = this.readJson("custom/effect_mappings.json");
            for (Map.Entry<String, JsonElement> entry : effectMappingsJson.entrySet()) {
                this.bedrockToJavaEffects.put(entry.getKey(), entry.getValue().getAsString());
            }

            for (String bedrockIdentifier : this.bedrockEffects.keySet()) {
                if (!this.bedrockToJavaEffects.containsKey(bedrockIdentifier)) {
                    throw new IllegalStateException("Missing bedrock -> java effect mapping for " + bedrockIdentifier);
                }
                final String javaIdentifier = this.bedrockToJavaEffects.get(bedrockIdentifier);
                if (!this.javaEffects.containsKey(javaIdentifier)) {
                    throw new IllegalStateException("Missing java effect mapping for: " + javaIdentifier);
                }
            }
        }
    }

    public ResourcePack getBedrockVanillaResourcePack() {
        return this.bedrockVanillaResourcePack;
    }

    public BufferedImage getSteveSkin() {
        return this.bedrockSteveSkin;
    }

    public JsonObject getBedrockSkinGeometry() {
        return this.bedrockSkinGeometry;
    }

    public CompoundTag getJavaRegistries() {
        return this.javaRegistries;
    }

    public CompoundTag getJavaTags() {
        return this.javaTags;
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

    public IntSet getJavaPreWaterloggedStates() {
        return this.javaPreWaterloggedStates;
    }

    public Int2IntMap getJavaPottedBlockStates() {
        return this.javaPottedBlockStates;
    }

    public BiMap<String, Integer> getBedrockLegacyBlocks() {
        return this.bedrockLegacyBlocks;
    }

    public Int2ObjectMap<BedrockBlockState> getBedrockLegacyBlockStates() {
        return this.bedrockLegacyBlockStates;
    }

    public Map<String, String> getBedrockBlockTags() {
        return this.bedrockBlockTags;
    }

    public Map<String, CompoundTag> getBedrockBiomeDefinitions() {
        return this.bedrockBiomeDefinitions;
    }

    public BiMap<String, Integer> getBedrockBiomes() {
        return this.bedrockBiomes;
    }

    public Map<String, Map<String, Object>> getBedrockToJavaBiomeExtraData() {
        return this.bedrockToJavaBiomeExtraData;
    }

    public BiMap<String, Integer> getBedrockItems() {
        return this.bedrockItems;
    }

    public Map<String, String> getBedrockToJavaItems() {
        return this.bedrockToJavaItems;
    }

    public BiMap<String, Integer> getBedrockEntities() {
        return this.bedrockEntities;
    }

    public Map<String, Entity1_19_4Types> getBedrockToJavaEntities() {
        return this.bedrockToJavaEntities;
    }

    public BiMap<String, Integer> getJavaBlockEntities() {
        return this.javaBlockEntities;
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

    @Override
    protected Logger getLogger() {
        return ViaBedrock.getPlatform().getLogger();
    }

    private ResourcePack readResourcePack(String file, final UUID uuid, final String version) {
        file = "assets/viabedrock/data/" + file;
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            final byte[] bytes = ByteStreams.toByteArray(inputStream);
            final ResourcePack resourcePack = new ResourcePack(uuid, version, "", "", "", false, false, 0, ResourcePack.TYPE_RESOURCE);
            resourcePack.setCompressedDataLength(bytes.length, bytes.length);
            resourcePack.processDataChunk(0, bytes);
            return resourcePack;
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Could not read " + file, e);
            return null;
        }
    }

    private List<String> readTextList(String file) {
        file = "assets/viabedrock/data/" + file;
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open " + file);
                return Collections.emptyList();
            }

            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not read " + file, e);
            return Collections.emptyList();
        }
    }

    private CompoundTag readNBT(String file) {
        file = "assets/viabedrock/data/" + file;
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open " + file);
                return null;
            }

            return NBTIO.readTag(new GZIPInputStream(inputStream));
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

    private BufferedImage readImage(String file) {
        file = "assets/viabedrock/data/" + file;
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open " + file);
                return null;
            }

            return ImageIO.read(inputStream);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not read " + file, e);
            return null;
        }
    }

    private void buildLegacyBlockStateMappings() {
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("assets/viabedrock/data/bedrock/r12_to_current_block_map.bin")) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open r12_to_current_block_map.bin");
                return;
            }
            final byte[] bytes = ByteStreams.toByteArray(inputStream);
            final ByteBuf buf = Unpooled.wrappedBuffer(bytes);

            this.bedrockLegacyBlockStates = new Int2ObjectOpenHashMap<>();
            while (buf.isReadable()) {
                final String identifier = BedrockTypes.STRING.read(buf).toLowerCase(Locale.ROOT);
                if (!this.bedrockLegacyBlocks.containsKey(identifier)) {
                    throw new RuntimeException("Unknown block identifier in r12_to_current_block_map.bin: " + identifier);
                }
                final int id = this.bedrockLegacyBlocks.get(identifier);
                final int metadata = buf.readShortLE();
                final CompoundTag tag = (CompoundTag) BedrockTypes.NETWORK_TAG.read(buf);
                final BedrockBlockState bedrockBlockState = BedrockBlockState.fromNbt(tag);
                if (!this.bedrockBlockStates.contains(bedrockBlockState)) {
                    throw new RuntimeException("Legacy block state " + bedrockBlockState.toBlockStateString() + " is not mapped to a modern block state");
                }

                this.bedrockLegacyBlockStates.put(id << 6 | metadata & 63, bedrockBlockState);
            }
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Could not read r12_to_current_block_map.bin", e);
            this.bedrockLegacyBlockStates = null;
        }
    }

}
