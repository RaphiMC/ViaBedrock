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
package net.raphimc.viabedrock.protocol.rewriter;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BlockStateRewriter extends StoredObject {

    private final Int2IntMap blockStateIdMappings = new Int2IntOpenHashMap(); // Bedrock -> Java
    private final Map<BlockState, Integer> blockStateTagMappings = new HashMap<>(); // Bedrock -> Bedrock
    private final IntList waterIds = new IntArrayList(); // Bedrock

    public BlockStateRewriter(final UserConnection user) {
        super(user);

        this.blockStateIdMappings.defaultReturnValue(-1);

        final Map<BlockState, Integer> bedrockBlockStates = BedrockProtocol.MAPPINGS.getBedrockBlockStates();
        final Map<BlockState, Integer> javaBlockStates = BedrockProtocol.MAPPINGS.getJavaBlockStates();
        final Map<BlockState, BlockState> bedrockToJavaBlockStates = BedrockProtocol.MAPPINGS.getBedrockToJavaBlockStates();

        for (Map.Entry<BlockState, Integer> entry : bedrockBlockStates.entrySet()) {
            final BlockState bedrockBlockState = entry.getKey();
            final int bedrockId = entry.getValue();
            this.blockStateTagMappings.put(bedrockBlockState, bedrockId);

            if (bedrockBlockState.getIdentifier().equals("water") || bedrockBlockState.getIdentifier().equals("flowing_water")) {
                this.waterIds.add(bedrockId);
            }

            // TODO: Really hacky, but it works for now. Fix properly once the mappings are fully done.
            if (bedrockBlockState.getIdentifier().contains("hanging_sign")) continue;
            if (bedrockBlockState.getIdentifier().contains("bamboo")) continue;
            if (bedrockBlockState.getIdentifier().contains("element")) continue;
            if (bedrockBlockState.getIdentifier().equals("border_block")) continue;
            if (bedrockBlockState.getIdentifier().equals("reserved6")) continue;
            if (bedrockBlockState.getIdentifier().equals("frame")) continue;
            if (bedrockBlockState.getIdentifier().equals("glow_frame")) continue;
            if (bedrockBlockState.getIdentifier().equals("chiseled_bookshelf")) continue;
            if (bedrockBlockState.getIdentifier().equals("glowingobsidian")) continue;
            if (bedrockBlockState.getIdentifier().equals("netherreactor")) continue;
            if (bedrockBlockState.getIdentifier().equals("unknown")) continue;
            if (bedrockBlockState.getIdentifier().equals("chemistry_table")) continue;
            if (bedrockBlockState.getIdentifier().equals("mangrove_propagule")) continue;
            if (bedrockBlockState.getIdentifier().equals("muddy_mangrove_roots")) continue;
            if (bedrockBlockState.getIdentifier().equals("info_update2")) continue;
            if (bedrockBlockState.getIdentifier().equals("info_update")) continue;
            if (bedrockBlockState.getIdentifier().equals("chemical_heat")) continue;
            if (bedrockBlockState.getIdentifier().equals("client_request_placeholder_block")) continue;
            if (bedrockBlockState.getIdentifier().equals("coral_fan_hang3")) continue;
            if (bedrockBlockState.getIdentifier().equals("jigsaw")) continue;
            if (bedrockBlockState.getIdentifier().equals("allow")) continue;
            if (bedrockBlockState.getIdentifier().equals("deny")) continue;
            if (bedrockBlockState.getIdentifier().equals("camera")) continue;
            if (!bedrockToJavaBlockStates.containsKey(bedrockBlockState)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "B -> J missing: " + bedrockBlockState);
                continue;
            }
            BlockState javaBlockState = bedrockToJavaBlockStates.get(bedrockBlockState);
            if (!javaBlockStates.containsKey(javaBlockState)) {
                javaBlockState = javaBlockState.withoutProperties("age");
                if (!javaBlockStates.containsKey(javaBlockState)) {
                    javaBlockState = javaBlockState.withoutProperties("facing");
                    if (!javaBlockStates.containsKey(javaBlockState)) {
                        javaBlockState = javaBlockState.withProperty("facing", "north");
                        if (!javaBlockStates.containsKey(javaBlockState)) {
                            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "J missing: " + javaBlockState);
                            continue;
                        }
                    }
                }
            }

            final int javaId = javaBlockStates.get(javaBlockState);
            this.blockStateIdMappings.put(bedrockId, javaId);
        }
    }

    public int bedrockId(final Tag bedrockBlockStateTag) {
        BlockState blockState = BlockState.fromNbt((CompoundTag) bedrockBlockStateTag);
        int runtimeId = this.bedrockId(blockState);
        if (runtimeId == -1) {
            final String originalNamespacedIdentifier = blockState.getNamespacedIdentifier();
            final String convertedNamespacedIdentifier = BedrockProtocol.MAPPINGS.getLegacyToModernBlockIdentifiers().get(originalNamespacedIdentifier);
            if (convertedNamespacedIdentifier != null) {
                blockState = blockState.withNamespacedIdentifier(convertedNamespacedIdentifier);
            }
            runtimeId = this.bedrockId(blockState);
            if (runtimeId == -1) {
                blockState = BedrockProtocol.MAPPINGS.getDefaultBlockStates().getOrDefault(originalNamespacedIdentifier, null);
                if (blockState != null) {
                    runtimeId = this.bedrockId(blockState);
                }
            }
        }

        return runtimeId;
    }

    public int bedrockId(final BlockState bedrockBlockState) {
        return this.blockStateTagMappings.getOrDefault(bedrockBlockState, -1);
    }

    public int javaId(final int bedrockBlockStateId) {
        return this.blockStateIdMappings.get(bedrockBlockStateId);
    }

    public int waterlog(final int javaBlockStateId) {
        if (BedrockProtocol.MAPPINGS.getPreWaterloggedStates().contains(javaBlockStateId)) {
            return javaBlockStateId;
        }

        final BlockState waterlogged = BedrockProtocol.MAPPINGS.getJavaBlockStates().inverse().get(javaBlockStateId).withProperty("waterlogged", "true");
        return BedrockProtocol.MAPPINGS.getJavaBlockStates().getOrDefault(waterlogged, -1);
    }

    public boolean isWater(final int bedrockBlockStateId) {
        return this.waterIds.contains(bedrockBlockStateId);
    }

}
