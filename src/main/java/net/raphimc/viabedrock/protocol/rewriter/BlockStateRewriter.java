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
import com.viaversion.viaversion.libs.fastutil.ints.*;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.block_state.BlockStateSanitizer;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.util.BlockStateHasher;
import net.raphimc.viabedrock.api.util.HashedPaletteComparator;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.BlockProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BlockStateRewriter extends StoredObject {

    private final Int2IntMap blockStateIdMappings = new Int2IntOpenHashMap(); // Bedrock -> Java
    private final Int2IntMap legacyBlockStateIdMappings = new Int2IntOpenHashMap(); // Bedrock -> Bedrock
    private final Object2IntMap<BlockState> blockStateTagMappings = new Object2IntOpenHashMap<>(); // Bedrock -> Bedrock
    private final IntList waterIds = new IntArrayList(); // Bedrock
    private final BlockStateSanitizer blockStateSanitizer;

    public BlockStateRewriter(final UserConnection user, final BlockProperties[] blockProperties, final boolean hashedRuntimeBlockIds) {
        super(user);

        this.blockStateIdMappings.defaultReturnValue(-1);
        this.legacyBlockStateIdMappings.defaultReturnValue(-1);
        this.blockStateTagMappings.defaultReturnValue(-1);

        final List<BedrockBlockState> bedrockBlockStates = new ArrayList<>(BedrockProtocol.MAPPINGS.getBedrockBlockStates());
        final Map<BlockState, Integer> javaBlockStates = BedrockProtocol.MAPPINGS.getJavaBlockStates();
        final Map<BlockState, BlockState> bedrockToJavaBlockStates = BedrockProtocol.MAPPINGS.getBedrockToJavaBlockStates();

        for (BlockProperties blockProperty : blockProperties) {
            final CompoundTag blockStateTag = new CompoundTag();
            blockStateTag.put("name", new StringTag(blockProperty.name()));
            blockStateTag.put("states", new CompoundTag());
            blockStateTag.put("network_id", new IntTag(BlockStateHasher.hash(blockStateTag)));

            bedrockBlockStates.add(BedrockBlockState.fromNbt(blockStateTag));
        }

        bedrockBlockStates.sort((a, b) -> HashedPaletteComparator.INSTANCE.compare(a.namespacedIdentifier(), b.namespacedIdentifier()));

        for (int i = 0; i < bedrockBlockStates.size(); i++) {
            final BedrockBlockState bedrockBlockState = bedrockBlockStates.get(i);
            final int bedrockId = hashedRuntimeBlockIds ? bedrockBlockState.blockStateTag().<IntTag>get("network_id").asInt() : i;

            this.blockStateTagMappings.put(bedrockBlockState, bedrockId);

            if (bedrockBlockState.namespacedIdentifier().equals("minecraft:water") || bedrockBlockState.namespacedIdentifier().equals("minecraft:flowing_water")) {
                this.waterIds.add(bedrockId);
            }

            if (bedrockBlockState.identifier().contains("hanging_sign")) continue;
            if (bedrockBlockState.identifier().equals("chiseled_bookshelf")) continue;
            if (bedrockBlockState.identifier().equals("mangrove_propagule")) continue;
            if (bedrockBlockState.identifier().equals("suspicious_gravel")) continue;
            if (bedrockBlockState.identifier().equals("pink_petals")) continue;
            if (bedrockBlockState.identifier().equals("suspicious_sand")) continue;
            if (bedrockBlockState.identifier().equals("decorated_pot")) continue;
            if (bedrockBlockState.identifier().equals("torchflower_crop")) continue;
            if (bedrockBlockState.identifier().equals("calibrated_sculk_sensor")) continue;

            if (!bedrockToJavaBlockStates.containsKey(bedrockBlockState)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing bedrock -> java block state mapping: " + bedrockBlockState.toBlockStateString());
                continue;
            }

            final BlockState javaBlockState = bedrockToJavaBlockStates.get(bedrockBlockState);
            if (!javaBlockStates.containsKey(javaBlockState)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing java block state mapping: " + javaBlockState);
                continue;
            }

            final int javaId = javaBlockStates.get(javaBlockState);
            this.blockStateIdMappings.put(bedrockId, javaId);
        }

        for (Int2ObjectMap.Entry<BedrockBlockState> entry : BedrockProtocol.MAPPINGS.getLegacyBlockStates().int2ObjectEntrySet()) {
            final int legacyId = entry.getIntKey() >> 6;
            final int legacyData = entry.getIntKey() & 63;
            if (legacyData > 15) continue; // Dirty hack Mojang did in 1.12. Can be ignored safely as those values can't be used in chunk packets.

            final int bedrockId = this.blockStateTagMappings.getInt(entry.getValue());
            if (bedrockId == -1) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Legacy block state " + entry.getValue() + " is not mapped to a modern block state");
                continue;
            }

            this.legacyBlockStateIdMappings.put(legacyId >> 4 | legacyData & 15, bedrockId);
        }

        this.blockStateSanitizer = new BlockStateSanitizer(bedrockBlockStates);
    }

    public int bedrockId(final CompoundTag bedrockBlockStateTag) {
        BedrockProtocol.MAPPINGS.getBlockStateUpgrader().upgradeToLatest(bedrockBlockStateTag);
        this.blockStateSanitizer.sanitize(bedrockBlockStateTag);

        return this.bedrockId(BedrockBlockState.fromNbt(bedrockBlockStateTag));
    }

    public int bedrockId(final BlockState bedrockBlockState) {
        return this.blockStateTagMappings.getInt(bedrockBlockState);
    }

    public int bedrockId(final int legacyBlockStateId) {
        return this.legacyBlockStateIdMappings.get(legacyBlockStateId);
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
