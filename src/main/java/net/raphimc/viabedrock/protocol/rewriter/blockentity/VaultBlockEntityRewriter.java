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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.util.UUIDUtil;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;

import java.util.List;

public class VaultBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    @Override
    public BlockEntity toJava(final UserConnection user, final BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        final CompoundTag sharedData = new CompoundTag();

        this.copyItem(user, bedrockTag, sharedData, "display_item");

        if (bedrockTag.contains("connected_players")) {

            final List<LongTag> players = bedrockTag.getListTag("connected_players", LongTag.class).getValue();
            final ListTag<IntArrayTag> javaPlayers = new ListTag<>(IntArrayTag.class);
            for (LongTag player : players) {
                if (player.asLong() != -1) {
                    final Entity entity = user.get(EntityTracker.class).getEntityByUid(player.asLong());
                    if (entity != null) {
                        javaPlayers.add(new IntArrayTag(UUIDUtil.toIntArray(entity.javaUuid())));
                    }
                }
            }
            sharedData.put("connected_players", javaPlayers);
        }

        final float cpr = bedrockTag.getFloat("connected_particle_range");
        sharedData.putDouble("connected_particles_range", cpr);

        javaTag.put("shared_data", sharedData);

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

}
