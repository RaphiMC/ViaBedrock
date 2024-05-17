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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;

public class CrafterBlockEntityRewriter extends LootableContainerBlockEntityRewriter {

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final BlockEntity javaBlockEntity = super.toJava(user, bedrockBlockEntity);
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = javaBlockEntity.tag();

        this.copy(bedrockTag, javaTag, "crafting_ticks_remaining", IntTag.class);
        if (bedrockTag.get("disabled_slots") instanceof ShortTag disabledSlotsTag) {
            final short bedrockDisabledSlots = disabledSlotsTag.asShort();
            final IntList javaDisabledSlots = new IntArrayList();
            for (int i = 0; i < 9; i++) {
                if ((bedrockDisabledSlots & (1 << i)) != 0) {
                    javaDisabledSlots.add(i);
                }
            }
            javaTag.put("disabled_slots", new IntArrayTag(javaDisabledSlots.toIntArray()));
        }

        return javaBlockEntity;
    }

}
