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

import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.opennbt.NBTIO;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class BedrockMappingData extends MappingDataBase {

    private Map<String, String> translations;
    private CompoundTag registries;
    private CompoundTag tags;

    public BedrockMappingData() {
        super(ProtocolVersion.v1_19_3.getName(), BedrockProtocolVersion.bedrockLatest.getName());
    }

    @Override
    public void load() {
        this.getLogger().info("Loading " + this.oldVersion + " -> " + this.newVersion + " mappings...");

        this.translations = this.readTranslationMap("en_US.lang");
        this.registries = this.readNBT("registries.nbt");
        this.tags = this.readNBT("tags.nbt");
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
            this.getLogger().severe("Could not read " + file);
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
            this.getLogger().severe("Could not read " + file);
            return null;
        }
    }

}
