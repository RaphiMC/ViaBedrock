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
package net.raphimc.viabedrock.protocol.rewriter.blockentity;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.chunk.BlockEntityWithBlockState;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.generated.bedrock.CustomBlockTags;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;

public class SkullBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    private static final IntSet SKULLS_WITH_ROTATION_UPDATE = new IntOpenHashSet();

    static {
        for (BedrockBlockState bedrockBlockState : BedrockProtocol.MAPPINGS.getBedrockBlockStates()) {
            if (CustomBlockTags.SKULL.equals(BedrockProtocol.MAPPINGS.getBedrockCustomBlockTags().get(bedrockBlockState.namespacedIdentifier()))) {
                final BlockState javaBlockState = BedrockProtocol.MAPPINGS.getBedrockToJavaBlockStates().get(bedrockBlockState);
                if (bedrockBlockState.properties().get("facing_direction").equals("1") && javaBlockState.properties().get("rotation").equals("0")) {
                    final int id = BedrockProtocol.MAPPINGS.getJavaBlockStates().getOrDefault(javaBlockState, -1);
                    if (id == -1) {
                        throw new IllegalStateException("Unable to find " + javaBlockState.toBlockStateString());
                    }
                    SKULLS_WITH_ROTATION_UPDATE.add(id);
                }
            }
        }
        if (SKULLS_WITH_ROTATION_UPDATE.isEmpty()) {
            throw new IllegalStateException("Unable to find any rotatable skulls");
        }
    }

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();

        int javaBlockState = user.get(ChunkTracker.class).getJavaBlockState(bedrockBlockEntity.position());
        if (SKULLS_WITH_ROTATION_UPDATE.contains(javaBlockState)) {
            if (bedrockTag.get("Rot") instanceof ByteTag rotTag) {
                javaBlockState += this.convertRot(rotTag.asByte());
            } else if (bedrockTag.get("Rotation") instanceof FloatTag rotationTag) {
                javaBlockState += this.convertRotation(rotationTag.asFloat());
            }
        }

        return new BlockEntityWithBlockState(new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, new CompoundTag()), javaBlockState);
    }

    private int convertRot(byte b) {
        b %= 16;
        if (b < 0) {
            b += 16;
        }

        return b;
    }

    private int convertRotation(float f) {
        f %= 360F;
        if (f < 0) {
            f += 360F;
        }

        return MathUtil.ceil((f / 360F) * 15);
    }

}
