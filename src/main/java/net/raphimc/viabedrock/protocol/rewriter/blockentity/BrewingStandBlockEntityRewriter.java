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
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;

import java.util.List;

public class BrewingStandBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        List<CompoundTag> items = bedrockTag.getListTag("Items", CompoundTag.class).getValue();
        ListTag<CompoundTag> javaItems = new ListTag<>(CompoundTag.class);
        for (CompoundTag item : items) {
            CompoundTag javaItem = this.rewriteItem(user, item);
            byte newSlot = switch (item.getByte("Slot")) {
                case 0 -> 3; // Ingredient Slot
                case 1 -> 0; // Potion slots
                case 2 -> 1;
                case 3 -> 2;
                case 4 -> 4; // Fuel slot
                default -> -1; // Invalid slot, should not happen
            };

            javaItem.putByte("Slot", newSlot);
            javaItems.add(javaItem);
        }
        javaTag.put("Items", javaItems);

        this.copy(bedrockTag, javaTag, "CookTime", "BrewTime", ShortTag.class);
        byte fuel = (byte) bedrockTag.getShort("FuelAmount");
        javaTag.putByte("Fuel", fuel);

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

}
