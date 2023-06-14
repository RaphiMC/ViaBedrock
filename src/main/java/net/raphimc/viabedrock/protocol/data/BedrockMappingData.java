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
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.opennbt.NBTIO;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
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

    private ResourcePack vanillaResourcePack; // Bedrock
    private CompoundTag registries; // Java
    private CompoundTag tags; // Java
    private BiMap<BlockState, Integer> javaBlockStates; // Java
    private BlockStateUpgrader blockStateUpgrader; // Bedrock
    private List<BedrockBlockState> bedrockBlockStates; // Bedrock
    private Map<BlockState, BlockState> bedrockToJavaBlockStates; // Bedrock -> Java
    private IntSet preWaterloggedStates; // Java
    private BiMap<String, Integer> legacyBlocks; // Bedrock
    private Int2ObjectMap<BedrockBlockState> legacyBlockStates; // Bedrock
    private BiMap<String, Integer> biomes; // Bedrock
    private Map<String, CompoundTag> biomeDefinitions; // Bedrock
    private Map<String, Map<String, Object>> biomeExtraData; // Bedrock -> Java
    private BiMap<String, Integer> items; // Bedrock
    private Map<String, String> entityIdentifiers; // Bedrock -> Java
    private BufferedImage steveSkin; // Bedrock
    private JsonObject skinGeometry; // Bedrock
    private Map<String, String> blockTags; // Bedrock
    private BiMap<String, Integer> blockEntities; // Java

    public BedrockMappingData() {
        super(BedrockProtocolVersion.bedrockLatest.getName(), ProtocolVersion.v1_20.getName());
    }

    @Override
    public void load() {
        if (Via.getManager().isDebug()) {
            this.getLogger().info("Loading " + this.unmappedVersion + " -> " + this.mappedVersion + " mappings...");
        }

        this.vanillaResourcePack = this.readResourcePack("bedrock/vanilla.mcpack", UUID.fromString("0575c61f-a5da-4b7f-9961-ffda2908861e"), "0.0.1");
        this.registries = this.readNBT("java/registries.nbt");
        this.tags = this.readNBT("java/tags.nbt");

        final JsonObject mapping1_20 = this.readJson("java/mapping-1.20.json");

        final JsonArray javaBlockStatesJson = mapping1_20.getAsJsonArray("blockstates");
        this.javaBlockStates = HashBiMap.create(javaBlockStatesJson.size());
        for (int i = 0; i < javaBlockStatesJson.size(); i++) {
            final BlockState blockState = BlockState.fromString(javaBlockStatesJson.get(i).getAsString());
            this.javaBlockStates.put(blockState, i);
        }

        this.blockStateUpgrader = new BlockStateUpgrader();

        final ListTag bedrockBlockStatesTag = this.readNBT("bedrock/block_palette.1_20_0.nbt").get("blocks");
        this.bedrockBlockStates = new ArrayList<>(bedrockBlockStatesTag.size());
        for (Tag tag : bedrockBlockStatesTag.getValue()) {
            this.bedrockBlockStates.add(BedrockBlockState.fromNbt((CompoundTag) tag));
        }

        final JsonObject blockMappings = this.readJson("custom/blockstate_mappings.json");
        this.bedrockToJavaBlockStates = new HashMap<>(blockMappings.size());
        for (Map.Entry<String, JsonElement> entry : blockMappings.entrySet()) {
            final BlockState bedrockBlockState = BlockState.fromString(entry.getKey());
            final BlockState javaBlockState = BlockState.fromString(entry.getValue().getAsString());
            if (this.bedrockToJavaBlockStates.put(bedrockBlockState, javaBlockState) != null) {
                throw new RuntimeException("Duplicate bedrock -> java block mapping for " + bedrockBlockState.toBlockStateString());
            }
        }

        final JsonArray preWaterloggedStatesJson = this.readJson("custom/pre_waterlogged_states.json").getAsJsonArray("blockstates");
        this.preWaterloggedStates = new IntOpenHashSet(preWaterloggedStatesJson.size());
        for (JsonElement entry : preWaterloggedStatesJson) {
            this.preWaterloggedStates.add(this.javaBlockStates.get(BlockState.fromString(entry.getAsString())).intValue());
        }

        final JsonObject legacyBlocksJson = this.readJson("bedrock/block_legacy_id_map.json");
        this.legacyBlocks = HashBiMap.create(legacyBlocksJson.size());
        for (Map.Entry<String, JsonElement> entry : legacyBlocksJson.entrySet()) {
            final String identifier = entry.getKey();
            final int id = entry.getValue().getAsInt();
            this.legacyBlocks.put(identifier.toLowerCase(Locale.ROOT), id);
        }

        this.buildLegacyBlockStateMappings();

        final JsonArray biomesJson = this.readJson("bedrock/biomes.json", JsonArray.class);
        this.biomes = HashBiMap.create(biomesJson.size());
        for (JsonElement entry : biomesJson) {
            final JsonObject biomeEntry = entry.getAsJsonObject();
            final String identifier = biomeEntry.get("name").getAsString();
            final int id = biomeEntry.get("id").getAsInt();
            this.biomes.put(identifier, id);
        }

        final CompoundTag biomeDefinitionsTag = this.readNBT("bedrock/biome_definitions.nbt");
        this.biomeDefinitions = new HashMap<>(biomeDefinitionsTag.size());
        for (Map.Entry<String, Tag> entry : biomeDefinitionsTag.getValue().entrySet()) {
            this.biomeDefinitions.put(entry.getKey(), (CompoundTag) entry.getValue());
        }

        final JsonObject biomeExtraDataJson = this.readJson("custom/biome_extra_data.json");
        this.biomeExtraData = new HashMap<>(biomeExtraDataJson.size());
        for (Map.Entry<String, JsonElement> entry : biomeExtraDataJson.entrySet()) {
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
            this.biomeExtraData.put(dataName, extraData);
        }

        final JsonArray itemsJson = this.readJson("bedrock/runtime_item_states.1_20_0.json", JsonArray.class);
        this.items = HashBiMap.create(itemsJson.size());
        for (JsonElement entry : itemsJson) {
            final JsonObject itemEntry = entry.getAsJsonObject();
            final String identifier = itemEntry.get("name").getAsString();
            final int id = itemEntry.get("id").getAsInt();
            this.items.put(identifier, id);
        }

        final JsonObject entityIdentifiersJson = this.readJson("custom/entity_identifiers.json");
        this.entityIdentifiers = new HashMap<>(entityIdentifiersJson.size());
        for (Map.Entry<String, JsonElement> entry : entityIdentifiersJson.entrySet()) {
            final String bedrockIdentifier = entry.getKey();
            final String javaIdentifier = entry.getValue().getAsString();
            this.entityIdentifiers.put(bedrockIdentifier, javaIdentifier);
        }

        this.steveSkin = this.readImage("bedrock/skin/steve.png");
        this.skinGeometry = JsonUtil.sort(this.readJson("bedrock/skin/geometry.json"), Comparator.naturalOrder());

        final JsonObject blockTagsJson = this.readJson("custom/block_tags.json");
        this.blockTags = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : blockTagsJson.entrySet()) {
            final String tagName = entry.getKey();
            for (JsonElement tagValueJson : entry.getValue().getAsJsonArray()) {
                this.blockTags.put(tagValueJson.getAsString(), tagName);
            }
        }

        final JsonArray blockEntitiesJson = mapping1_20.get("blockentities").getAsJsonArray();
        this.blockEntities = HashBiMap.create(blockEntitiesJson.size());
        for (int i = 0; i < blockEntitiesJson.size(); i++) {
            this.blockEntities.put(blockEntitiesJson.get(i).getAsString(), i);
        }
    }

    public ResourcePack getVanillaResourcePack() {
        return this.vanillaResourcePack;
    }

    public CompoundTag getRegistries() {
        return this.registries;
    }

    public CompoundTag getTags() {
        return this.tags;
    }

    public BiMap<BlockState, Integer> getJavaBlockStates() {
        return Maps.unmodifiableBiMap(this.javaBlockStates);
    }

    public BlockStateUpgrader getBlockStateUpgrader() {
        return this.blockStateUpgrader;
    }

    public List<BedrockBlockState> getBedrockBlockStates() {
        return Collections.unmodifiableList(this.bedrockBlockStates);
    }

    public Map<BlockState, BlockState> getBedrockToJavaBlockStates() {
        return Collections.unmodifiableMap(this.bedrockToJavaBlockStates);
    }

    public IntSet getPreWaterloggedStates() {
        return this.preWaterloggedStates;
    }

    public BiMap<String, Integer> getLegacyBlocks() {
        return Maps.unmodifiableBiMap(this.legacyBlocks);
    }

    public Int2ObjectMap<BedrockBlockState> getLegacyBlockStates() {
        return this.legacyBlockStates;
    }

    public BiMap<String, Integer> getBiomes() {
        return Maps.unmodifiableBiMap(this.biomes);
    }

    public Map<String, CompoundTag> getBiomeDefinitions() {
        return Collections.unmodifiableMap(this.biomeDefinitions);
    }

    public Map<String, Map<String, Object>> getBiomeExtraData() {
        return Collections.unmodifiableMap(this.biomeExtraData);
    }

    public BiMap<String, Integer> getItems() {
        return Maps.unmodifiableBiMap(this.items);
    }

    public Map<String, String> getEntityIdentifiers() {
        return Collections.unmodifiableMap(this.entityIdentifiers);
    }

    public BufferedImage getSteveSkin() {
        return this.steveSkin;
    }

    public JsonObject getSkinGeometry() {
        return this.skinGeometry;
    }

    public Map<String, String> getBlockTags() {
        return Collections.unmodifiableMap(this.blockTags);
    }

    public BiMap<String, Integer> getBlockEntities() {
        return Maps.unmodifiableBiMap(this.blockEntities);
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

            this.legacyBlockStates = new Int2ObjectOpenHashMap<>();
            while (buf.isReadable()) {
                final String identifier = BedrockTypes.STRING.read(buf).toLowerCase(Locale.ROOT);
                final int metadata = buf.readShortLE();
                final CompoundTag current = (CompoundTag) BedrockTypes.NETWORK_TAG.read(buf);

                if (!this.legacyBlocks.containsKey(identifier)) {
                    this.getLogger().warning("Unknown block identifier in r12_to_current_block_map.bin: " + identifier);
                    continue;
                }
                final int id = this.legacyBlocks.get(identifier);

                this.legacyBlockStates.put(id << 6 | metadata & 63, BedrockBlockState.fromNbt(current));
            }
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Could not read r12_to_current_block_map.bin", e);
            this.legacyBlockStates = null;
        }
    }

}
