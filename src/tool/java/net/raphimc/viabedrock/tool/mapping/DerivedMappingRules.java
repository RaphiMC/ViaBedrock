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

import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Derives the parts of the curated data which follow a stable naming rule. Other mappings remain hand maintained.
 */
public final class DerivedMappingRules {

    private DerivedMappingRules() {
    }

    public static Map<String, String> blockTags(final MappingAssets assets) {
        final Map<String, String> tags = new TreeMap<>();
        for (BedrockBlockState state : assets.bedrockBlockStates()) {
            final String identifier = state.namespacedIdentifier();
            if (identifier.endsWith("_hanging_sign")) {
                tags.put(identifier, "hanging_sign");
            } else if (identifier.endsWith("_shelf")) {
                tags.put(identifier, "shelf");
            } else if (identifier.endsWith("_standing_sign") || identifier.endsWith("_wall_sign")
                    || identifier.equals("minecraft:standing_sign") || identifier.equals("minecraft:wall_sign")) {
                tags.put(identifier, "sign");
            }
        }
        return tags;
    }

    public static Map<String, String> pottedBlockStates(final MappingAssets assets) {
        final Set<String> javaBlocks = assets.javaNamespaced("blocks");
        final Map<String, String> potted = new TreeMap<>();
        final Map<String, String> directTargets = new TreeMap<>();
        for (String identifier : javaBlocks) {
            if (identifier.startsWith("minecraft:potted_")) {
                directTargets.put("minecraft:" + identifier.substring("minecraft:potted_".length()), identifier);
            }
        }

        for (String stateString : assets.javaBlockStates()) {
            final BlockState state = BlockState.fromString(stateString);
            final String target = directTargets.get(state.namespacedIdentifier());
            if (target != null && !state.hasProperty("waterlogged", "true")) {
                potted.put(state.toBlockStateString(true), target);
            }
        }
        return potted;
    }

}
