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
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.opennbt.NBTIO;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.util.GsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

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
    private BiMap<BlockState, Integer> bedrockBlockStates; // Bedrock
    private Map<BlockState, BlockState> bedrockToJavaBlockStates; // Bedrock -> Java
    private Map<String, BlockState> defaultBlockStates; // Bedrock -> Bedrock
    private IntList preWaterloggedStates; // Java
    private BiMap<String, Integer> legacyBlocks; // Bedrock
    private Int2ObjectMap<BlockState> legacyBlockStates; // Bedrock
    private Map<String, String> legacyToModernBlockIdentifiers; // Bedrock

    public BedrockMappingData() {
        super(ProtocolVersion.v1_19_3.getName(), BedrockProtocolVersion.bedrockLatest.getName());
    }

    @Override
    public void load() {
        this.getLogger().info("Loading " + this.oldVersion + " -> " + this.newVersion + " mappings...");

        this.translations = this.readTranslationMap("bedrock/en_US.lang");
        this.registries = this.readNBT("java/registries.nbt");
        this.tags = this.readNBT("java/tags.nbt");

        final JsonObject javaBlockStatesJson = MappingDataLoader.loadData("mapping-1.19.3.json", true).getAsJsonObject("blockstates");
        this.javaBlockStates = HashBiMap.create(javaBlockStatesJson.size());
        for (Map.Entry<String, JsonElement> entry : javaBlockStatesJson.entrySet()) {
            final int id = Integer.parseInt(entry.getKey());
            final BlockState blockState = BlockState.fromString(entry.getValue().getAsString());
            this.javaBlockStates.put(blockState, id);
        }

        final ListTag bedrockBlockStatesTag = this.readNBT("bedrock/block_palette.1_19_60.nbt").get("blocks");
        this.bedrockBlockStates = HashBiMap.create(bedrockBlockStatesTag.size());
        this.defaultBlockStates = new HashMap<>(bedrockBlockStatesTag.size());
        for (int i = 0; i < bedrockBlockStatesTag.getValue().size(); i++) {
            final BlockState blockState = BlockState.fromNbt((CompoundTag) bedrockBlockStatesTag.getValue().get(i));
            if (!this.defaultBlockStates.containsKey(blockState.getNamespacedIdentifier())) {
                this.defaultBlockStates.put(blockState.getNamespacedIdentifier(), blockState);
            }
            this.bedrockBlockStates.put(blockState, i);
        }

        final JsonObject bedrockToJavaBlockStatesJson = this.readJson("blocksB2J.json");
        this.bedrockToJavaBlockStates = new HashMap<>(bedrockToJavaBlockStatesJson.size());
        for (Map.Entry<String, JsonElement> entry : bedrockToJavaBlockStatesJson.entrySet()) {
            final BlockState bedrockBlockState = BlockState.fromString(entry.getKey());
            final BlockState javaBlockState = BlockState.fromString(entry.getValue().getAsString());
            this.bedrockToJavaBlockStates.put(bedrockBlockState, javaBlockState);
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

    public BiMap<BlockState, Integer> getBedrockBlockStates() {
        return Maps.unmodifiableBiMap(this.bedrockBlockStates);
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

    public Map<String, String> getLegacyToModernBlockIdentifiers() {
        return this.legacyToModernBlockIdentifiers;
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

    private void buildLegacyBlockStateMappings() {
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("assets/viabedrock/data/bedrock/r12_to_current_block_map.bin")) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open r12_to_current_block_map.bin");
                return;
            }
            final byte[] bytes = ByteStreams.toByteArray(inputStream);
            final ByteBuf buf = Unpooled.wrappedBuffer(bytes);

            this.legacyBlockStates = new Int2ObjectOpenHashMap<>();
            this.legacyToModernBlockIdentifiers = new HashMap<>();
            while (buf.isReadable()) {
                final String identifier = BedrockTypes.STRING.read(buf).toLowerCase(Locale.ROOT);
                final int metadata = buf.readShortLE();
                final CompoundTag current = (CompoundTag) BedrockTypes.TAG.read(buf);

                if (!this.legacyBlocks.containsKey(identifier)) {
                    this.getLogger().warning("Unknown block identifier in r12_to_current_block_map.bin: " + identifier);
                    continue;
                }
                final int id = this.legacyBlocks.get(identifier);
                final BlockState blockState = BlockState.fromNbt(current);

                this.legacyBlockStates.put(id << 6 | metadata & 63, blockState);

                final String modernIdentifier = blockState.getNamespacedIdentifier();
                if (!identifier.equals(modernIdentifier) && !this.legacyToModernBlockIdentifiers.containsKey(identifier)) {
                    this.legacyToModernBlockIdentifiers.put(identifier, modernIdentifier);
                }
            }
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Could not read r12_to_current_block_map.bin", e);
            this.legacyBlockStates = null;
            this.legacyToModernBlockIdentifiers = null;
        }
    }

}
