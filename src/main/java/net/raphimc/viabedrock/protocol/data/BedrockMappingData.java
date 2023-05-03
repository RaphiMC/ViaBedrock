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
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
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
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.protocol.rewriter.blockstate.BlockStateUpgrader;
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

    private Map<String, String> translations; // Bedrock
    private CompoundTag registries; // Java
    private CompoundTag tags; // Java
    private BiMap<BlockState, Integer> javaBlockStates; // Java
    private BlockStateUpgrader blockStateUpgrader; // Bedrock
    private List<BlockState> bedrockBlockStates; // Bedrock
    private Map<BlockState, BlockState> bedrockToJavaBlockStates; // Bedrock -> Java
    private Map<String, BlockState> defaultBlockStates; // Bedrock -> Bedrock
    private IntList preWaterloggedStates; // Java
    private BiMap<String, Integer> legacyBlocks; // Bedrock
    private Int2ObjectMap<BlockState> legacyBlockStates; // Bedrock
    private BiMap<String, Integer> biomes; // Bedrock
    private Map<String, CompoundTag> biomeDefinitions; // Bedrock
    private Map<String, Map<String, Object>> biomeExtraData; // Bedrock -> Java
    private BiMap<String, Integer> items; // Bedrock
    private Map<String, String> entityIdentifiers; // Bedrock -> Java
    private BufferedImage steveSkin; // Bedrock
    private JsonObject skinGeometry; // Bedrock

    public BedrockMappingData() {
        super(BedrockProtocolVersion.bedrockLatest.getName(), ProtocolVersion.v1_19_4.getName());
    }

    @Override
    public void load() {
        if (Via.getManager().isDebug()) {
            this.getLogger().info("Loading " + this.unmappedVersion + " -> " + this.mappedVersion + " mappings...");
        }

        this.translations = this.readTranslationMap("bedrock/en_US.lang");
        this.registries = this.readNBT("java/registries.nbt");
        this.tags = this.readNBT("java/tags.nbt");

        final JsonArray javaBlockStatesJson = this.readJson("java/mapping-1.19.4.json").getAsJsonArray("blockstates");
        this.javaBlockStates = HashBiMap.create(javaBlockStatesJson.size());
        for (int i = 0; i < javaBlockStatesJson.size(); i++) {
            final BlockState blockState = BlockState.fromString(javaBlockStatesJson.get(i).getAsString());
            this.javaBlockStates.put(blockState, i);
        }

        this.blockStateUpgrader = new BlockStateUpgrader();

        final ListTag bedrockBlockStatesTag = this.readNBT("bedrock/block_palette.1_19_80.nbt").get("blocks");
        this.bedrockBlockStates = new ArrayList<>(bedrockBlockStatesTag.size());
        this.defaultBlockStates = new HashMap<>(bedrockBlockStatesTag.size());
        for (Tag tag : bedrockBlockStatesTag.getValue()) {
            final BlockState blockState = BlockState.fromNbt((CompoundTag) tag);
            if (!this.defaultBlockStates.containsKey(blockState.getNamespacedIdentifier())) {
                this.defaultBlockStates.put(blockState.getNamespacedIdentifier(), blockState);
            }
            this.bedrockBlockStates.add(blockState);
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
        this.preWaterloggedStates = new IntArrayList(preWaterloggedStatesJson.size());
        for (JsonElement entry : preWaterloggedStatesJson) {
            this.preWaterloggedStates.add(this.javaBlockStates.get(BlockState.fromString(entry.getAsString())).intValue());
        }

        final JsonObject legacyBlocksJson = this.readJson("bedrock/block_id_map.json");
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

        final JsonArray itemsJson = this.readJson("bedrock/runtime_item_states.1_19_80.json", JsonArray.class);
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
        this.skinGeometry = this.readJson("bedrock/skin/geometry.json");
    }

    public Map<String, String> getTranslations() {
        return Collections.unmodifiableMap(this.translations);
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

    public List<BlockState> getBedrockBlockStates() {
        return Collections.unmodifiableList(this.bedrockBlockStates);
    }

    public Map<BlockState, BlockState> getBedrockToJavaBlockStates() {
        return Collections.unmodifiableMap(this.bedrockToJavaBlockStates);
    }

    public Map<String, BlockState> getDefaultBlockStates() {
        return Collections.unmodifiableMap(this.defaultBlockStates);
    }

    public IntList getPreWaterloggedStates() {
        return this.preWaterloggedStates;
    }

    public BiMap<String, Integer> getLegacyBlocks() {
        return Maps.unmodifiableBiMap(this.legacyBlocks);
    }

    public Int2ObjectMap<BlockState> getLegacyBlockStates() {
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

    @Override
    protected Logger getLogger() {
        return ViaBedrock.getPlatform().getLogger();
    }

    private Map<String, String> readTranslationMap(final String file) {
        final List<String> lines = this.readTextList(file);
        return lines.stream()
                .filter(line -> !line.startsWith("##"))
                .filter(line -> line.contains("="))
                .map(line -> line.contains("##") ? line.substring(0, line.indexOf("##")) : line)
                .map(String::trim)
                .map(line -> line.split("=", 2))
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
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
                if (metadata > 15) continue; // Dirty hack Mojang did in 1.12. Can be ignored safely as those values can't be used in chunk packets

                if (!this.legacyBlocks.containsKey(identifier)) {
                    this.getLogger().warning("Unknown block identifier in r12_to_current_block_map.bin: " + identifier);
                    continue;
                }
                final int id = this.legacyBlocks.get(identifier);

                this.legacyBlockStates.put(id << 4 | metadata & 15, BlockState.fromNbt(current));
            }
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Could not read r12_to_current_block_map.bin", e);
            this.legacyBlockStates = null;
        }
    }

}
