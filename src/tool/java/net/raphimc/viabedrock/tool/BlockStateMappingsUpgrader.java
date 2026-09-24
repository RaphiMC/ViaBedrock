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
package net.raphimc.viabedrock.tool;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.api.chunk.blockstate.JsonBlockStateUpgradeSchema;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Runs the hand maintained bedrock to java block state mappings through one or more bedrock block state upgrade schemas.
 * <p>
 * After pulling new schemas from <a href="https://github.com/pmmp/BedrockBlockUpgradeSchema">PMMP/BedrockBlockUpgradeSchema</a>,
 * pass the ones which were added, for example {@code --from=0331_1.21.100.23_beta_to_1.21.110.26_beta}. Applying a schema
 * which is already part of the mappings merges block states and fails with a duplicate block state error.
 */
public final class BlockStateMappingsUpgrader {

    public static void main(final String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final List<Path> schemaFiles = resolveSchemas(toolArgs);
        final Path mappingsFile = toolArgs.path("mappings", ToolPaths.CUSTOM_DATA.resolve("blockstate_mappings.json"));
        final Path outputFile = toolArgs.path("output", mappingsFile);

        final List<JsonBlockStateUpgradeSchema> schemas = new ArrayList<>();
        for (Path schemaFile : schemaFiles) {
            System.out.println("Applying schema " + schemaFile.getFileName());
            schemas.add(new JsonBlockStateUpgradeSchema(JsonParser.parseString(Files.readString(schemaFile)).getAsJsonObject()));
        }

        final JsonObject blockStateMappingsJson = JsonParser.parseString(Files.readString(mappingsFile)).getAsJsonObject();
        final JsonObject newBlockStateMappingsJson = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : blockStateMappingsJson.entrySet()) {
            final BlockState bedrockBlockState = BlockState.fromString(entry.getKey());
            final BlockState javaBlockState = BlockState.fromString(entry.getValue().getAsString());

            final CompoundTag blockStateTag = new CompoundTag();
            blockStateTag.putInt("version", 0);
            blockStateTag.putString("name", bedrockBlockState.namespacedIdentifier());
            final CompoundTag statesTag = new CompoundTag();
            blockStateTag.put("states", statesTag);
            for (Map.Entry<String, String> property : bedrockBlockState.properties().entrySet()) {
                if (property.getValue().equals("true")) {
                    statesTag.putBoolean(property.getKey(), true);
                } else if (property.getValue().equals("false")) {
                    statesTag.putBoolean(property.getKey(), false);
                } else {
                    final boolean byteVal = property.getKey().equals("coral_hang_type_bit") || property.getKey().equals("dead_bit") || property.getKey().equals("color_bit")
                        || property.getKey().equals("allow_underwater_bit") || property.getKey().equals("active");
                    if (byteVal) {
                        statesTag.putByte(property.getKey(), Byte.parseByte(property.getValue()));
                    } else {
                        try {
                            statesTag.putInt(property.getKey(), Integer.parseInt(property.getValue()));
                        } catch (final NumberFormatException e) {
                            statesTag.putString(property.getKey(), property.getValue());
                        }
                    }
                }
            }

            for (JsonBlockStateUpgradeSchema schema : schemas) {
                schema.upgrade(blockStateTag);
            }

            final BedrockBlockState newBedrockBlockState = BedrockBlockState.fromNbt(blockStateTag);
            final String newBedrockBlockStateString = newBedrockBlockState.toBlockStateString(true);
            if (newBlockStateMappingsJson.has(newBedrockBlockStateString)) {
                throw new IllegalStateException("Duplicate block state: " + newBedrockBlockStateString + ". The mappings most likely already contain this schema.");
            }
            newBlockStateMappingsJson.addProperty(newBedrockBlockStateString, javaBlockState.toBlockStateString(true));
        }

        if (newBlockStateMappingsJson.size() != blockStateMappingsJson.size()) {
            throw new IllegalStateException("Something went wrong while upgrading block state mappings");
        }

        ToolPaths.writeJson(outputFile, GsonUtil.sort(newBlockStateMappingsJson));
    }

    private static List<Path> resolveSchemas(final ToolArgs toolArgs) throws IOException {
        final List<Path> allSchemas = listSchemas();

        final List<String> requested = toolArgs.list("schema");
        if (!requested.isEmpty()) {
            final List<Path> schemas = new ArrayList<>();
            for (String name : requested) {
                schemas.add(resolveSchema(allSchemas, name));
            }
            return schemas;
        }

        final List<String> from = toolArgs.list("from");
        if (!from.isEmpty()) {
            final Path firstSchema = resolveSchema(allSchemas, from.get(0));
            return allSchemas.subList(allSchemas.indexOf(firstSchema), allSchemas.size());
        }

        final StringBuilder message = new StringBuilder("Missing required argument --schema or --from. The mappings don't record which schemas they already contain, so the new ones have to be named. The newest schemas are:");
        for (Path schema : allSchemas.subList(Math.max(0, allSchemas.size() - 5), allSchemas.size())) {
            message.append("\n  ").append(schema.getFileName());
        }
        throw new IllegalArgumentException(message.toString());
    }

    private static Path resolveSchema(final List<Path> allSchemas, final String name) {
        final Path asPath = Path.of(name);
        if (Files.isRegularFile(asPath)) {
            return asPath;
        }
        final String fileName = name.endsWith(".json") ? name : name + ".json";
        return allSchemas.stream()
            .filter(schema -> schema.getFileName().toString().equals(fileName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown schema '" + name + "'. It is neither a file nor a name in " + ToolPaths.BLOCK_STATE_UPGRADE_SCHEMAS));
    }

    private static List<Path> listSchemas() throws IOException {
        try (Stream<Path> files = Files.list(ToolPaths.BLOCK_STATE_UPGRADE_SCHEMAS)) {
            final List<Path> schemas = files.filter(file -> file.getFileName().toString().endsWith(".json")).sorted().toList();
            if (schemas.isEmpty()) {
                throw new IllegalStateException("No schemas found in " + ToolPaths.BLOCK_STATE_UPGRADE_SCHEMAS);
            }
            return schemas;
        }
    }

    private BlockStateMappingsUpgrader() {
    }

}
