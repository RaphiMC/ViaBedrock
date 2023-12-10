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
package net.raphimc.viabedrock.protocol.rewriter.blockentity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.LongTag;
import com.viaversion.viaversion.util.UUIDUtil;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;

public class ConduitBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        if (bedrockTag.get("Target") instanceof LongTag && bedrockTag.<LongTag>get("Target").asLong() != -1) {
            final long target = bedrockTag.<LongTag>get("Target").asLong();

            final Entity entity = user.get(EntityTracker.class).getEntityByUid(target);
            if (entity != null) {
                javaTag.put("Target", new IntArrayTag(UUIDUtil.toIntArray(entity.javaUuid())));
            }
        }

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

}
