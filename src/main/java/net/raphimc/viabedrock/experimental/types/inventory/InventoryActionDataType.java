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
package net.raphimc.viabedrock.experimental.types.inventory;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.experimental.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.experimental.model.inventory.InventorySource;
import net.raphimc.viabedrock.experimental.types.ExperimentalTypes;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class InventoryActionDataType extends Type<InventoryActionData> {

    private ItemRewriter itemRewriter;

    public InventoryActionDataType() {
        super(InventoryActionData.class);
    }

    @Override
    public InventoryActionData read(ByteBuf buffer) {
        final InventorySource source = ExperimentalTypes.INVENTORY_SOURCE.read(buffer);
        final int slot = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        final BedrockItem fromItem = null; //TODO
        final BedrockItem toItem = null; //TODO
        //final int stackNetworkId = BedrockTypes.VAR_INT.read(buffer);

        return new InventoryActionData(source, slot, fromItem, toItem);
    }

    @Override
    public void write(ByteBuf buffer, InventoryActionData value) {
        ExperimentalTypes.INVENTORY_SOURCE.write(buffer, value.source());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.slot());
        itemRewriter.itemType().write(buffer, value.fromItem()); //TODO
        itemRewriter.itemType().write(buffer, value.toItem()); //TODO
        //BedrockTypes.VAR_INT.write(buffer, value.stackNetworkId());
    }
}
