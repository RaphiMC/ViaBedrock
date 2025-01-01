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
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;

public class BeehiveBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        final ListTag<CompoundTag> bedrockOccupants = bedrockTag.getListTag("Occupants", CompoundTag.class);
        if (bedrockOccupants != null) {
            final ListTag<CompoundTag> javaBees = new ListTag<>(CompoundTag.class);
            for (CompoundTag bedrockOccupant : bedrockOccupants) {
                final CompoundTag javaBee = new CompoundTag();
                this.copy(bedrockOccupant, javaBee, "TicksLeftToStay", "ticks_in_hive", IntTag.class);
                this.copy(bedrockOccupant, javaBee, "TicksLeftToStay", "min_ticks_in_hive", IntTag.class);
                javaBees.add(javaBee);
            }
            javaTag.put("Bees", javaBees);
        }

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

}
