/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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

import com.google.common.collect.ImmutableMap;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.FloatTag;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.chunk.BlockEntityWithBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;

public class SkullBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    private static final int PLAYER_HEAD_TYPE = 3;
    private static final int MAX_TYPE = 6;
    private static final int SKULL_WITH_ROTATION_UPDATE;
    private static final int SKULL_BLOCK_STATE_COUNT;

    static {
        final BlockState unpoweredRotation0SkeletonSkull = new BlockState("skeleton_skull", ImmutableMap.of("powered", "false", "rotation", "0"));
        SKULL_WITH_ROTATION_UPDATE = BedrockProtocol.MAPPINGS.getJavaBlockStates().getOrDefault(unpoweredRotation0SkeletonSkull, -1);
        if (SKULL_WITH_ROTATION_UPDATE == -1) {
            throw new IllegalStateException("Unable to find " + unpoweredRotation0SkeletonSkull.toBlockStateString());
        }

        final BlockState poweredRotation0SkeletonSkull = new BlockState("skeleton_skull", ImmutableMap.of("powered", "true", "rotation", "0"));
        final int poweredRotation0SkeletonSkullId = BedrockProtocol.MAPPINGS.getJavaBlockStates().getOrDefault(poweredRotation0SkeletonSkull, -1);
        if (poweredRotation0SkeletonSkullId == -1) {
            throw new IllegalStateException("Unable to find " + poweredRotation0SkeletonSkull.toBlockStateString());
        }

        final BlockState poweredRotation0WitherSkeletonSkull = new BlockState("wither_skeleton_skull", ImmutableMap.of("powered", "true", "rotation", "0"));
        final int poweredRotation0WitherSkeletonSkullId = BedrockProtocol.MAPPINGS.getJavaBlockStates().getOrDefault(poweredRotation0WitherSkeletonSkull, -1);
        if (poweredRotation0WitherSkeletonSkullId == -1) {
            throw new IllegalStateException("Unable to find " + poweredRotation0WitherSkeletonSkull.toBlockStateString());
        }

        SKULL_BLOCK_STATE_COUNT = poweredRotation0WitherSkeletonSkullId - poweredRotation0SkeletonSkullId;
    }

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();

        byte type = bedrockTag.get("SkullType") instanceof ByteTag ? bedrockTag.<ByteTag>get("SkullType").asByte() : 0;
        if (type < 0 || type > MAX_TYPE) type = PLAYER_HEAD_TYPE;

        int javaBlockState = user.get(ChunkTracker.class).getJavaBlockState(bedrockBlockEntity.position());
        if (javaBlockState == SKULL_WITH_ROTATION_UPDATE) {
            if (bedrockTag.get("Rot") instanceof ByteTag) {
                javaBlockState += this.convertRot(bedrockTag.<ByteTag>get("Rot").asByte());
            } else if (bedrockTag.get("Rotation") instanceof FloatTag) {
                javaBlockState += this.convertRotation(bedrockTag.<FloatTag>get("Rotation").asFloat());
            }
        }
        javaBlockState += type * SKULL_BLOCK_STATE_COUNT;

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

        return (int) Math.ceil((f / 360F) * 15);
    }

}
