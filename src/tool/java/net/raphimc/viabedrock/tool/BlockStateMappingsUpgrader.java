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
import com.viaversion.viaversion.libs.gson.Gson;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.api.chunk.blockstate.JsonBlockStateUpgradeSchema;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class BlockStateMappingsUpgrader {

    public static void main(String[] args) throws Throwable {
        final byte[] data = BlockStateMappingsUpgrader.class.getResourceAsStream("/assets/viabedrock/block_state_upgrade_schema/0321_1.21.40.25_beta_to_1.21.60.28_beta.json").readAllBytes();
        final JsonBlockStateUpgradeSchema schema = new JsonBlockStateUpgradeSchema(JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject());
        final byte[] blockStateData = BlockStateMappingsUpgrader.class.getResourceAsStream("/assets/viabedrock/data/custom/blockstate_mappings.json").readAllBytes();
        final JsonObject blockStateMappingsJson = JsonParser.parseString(new String(blockStateData, StandardCharsets.UTF_8)).getAsJsonObject();
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
                        } catch (NumberFormatException e) {
                            statesTag.putString(property.getKey(), property.getValue());
                        }
                    }
                }
            }

            schema.upgrade(blockStateTag);

            final BedrockBlockState newBedrockBlockState = BedrockBlockState.fromNbt(blockStateTag);
            final String newBedrockBlockStateString = newBedrockBlockState.toBlockStateString(true);
            if (newBlockStateMappingsJson.has(newBedrockBlockStateString)) {
                throw new IllegalStateException("Duplicate block state: " + newBedrockBlockStateString);
            }
            newBlockStateMappingsJson.addProperty(newBedrockBlockStateString, javaBlockState.toBlockStateString(true));
        }

        if (newBlockStateMappingsJson.size() != blockStateMappingsJson.size()) {
            throw new IllegalStateException("Something went wrong while upgrading block state mappings");
        }

        final String json = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(GsonUtil.sort(newBlockStateMappingsJson));
        Files.writeString(new File("new_blockstate_mappings.json").toPath(), json);
    }

}
