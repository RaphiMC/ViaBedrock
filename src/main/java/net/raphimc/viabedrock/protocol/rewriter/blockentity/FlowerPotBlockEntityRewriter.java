/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol.rewriter.blockentity;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.chunk.BlockEntityWithBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;

import java.util.Collections;
import java.util.logging.Level;

public class FlowerPotBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    private static final int DEFAULT_BLOCK_STATE = BedrockProtocol.MAPPINGS.getJavaBlockStates().get(new BlockState("flower_pot", Collections.emptyMap()));

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();

        final BlockEntity javaBlockEntity = new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, null);
        final BlockEntity defaultJavaBlockEntity = new BlockEntityWithBlockState(javaBlockEntity, DEFAULT_BLOCK_STATE);

        final BlockStateRewriter blockStateRewriter = user.get(BlockStateRewriter.class);
        final int bedrockBlockState;
        if (bedrockTag.get("item") instanceof ShortTag itemTag && bedrockTag.get("mData") instanceof IntTag dataTag) {
            final short id = itemTag.asShort();
            final int metadata = dataTag.asInt();
            if (metadata < 0 || metadata > 15) return defaultJavaBlockEntity;
            final int legacyBlockStateId = (id << 4) | metadata;

            bedrockBlockState = blockStateRewriter.bedrockId(legacyBlockStateId);
            if (bedrockBlockState == -1) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing legacy block state: " + legacyBlockStateId);
                return defaultJavaBlockEntity;
            }
        } else if (bedrockTag.get("PlantBlock") instanceof CompoundTag plantBlockTag && !plantBlockTag.isEmpty()) {
            bedrockBlockState = blockStateRewriter.bedrockId(plantBlockTag);
            if (bedrockBlockState == -1) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + plantBlockTag);
                return defaultJavaBlockEntity;
            }
        } else {
            return defaultJavaBlockEntity;
        }

        final int javaBlockState = blockStateRewriter.javaId(bedrockBlockState);
        if (javaBlockState == -1) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + bedrockBlockState);
            return defaultJavaBlockEntity;
        }

        final int pottedJavaBlockState = BedrockProtocol.MAPPINGS.getJavaPottedBlockStates().getOrDefault(javaBlockState, -1);
        if (pottedJavaBlockState == -1) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing potted block state: " + javaBlockState);
            return defaultJavaBlockEntity;
        }

        return new BlockEntityWithBlockState(javaBlockEntity, pottedJavaBlockState);
    }

}
