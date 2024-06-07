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
package net.raphimc.viabedrock.api.model.inventory;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import net.raphimc.viabedrock.protocol.data.enums.MenuType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;

public class InventoryContainer extends Container {

    public InventoryContainer(byte windowId) {
        super(windowId, MenuType.INVENTORY, 36);
    }

    @Override
    public void setItems(BedrockItem[] items) {
        System.arraycopy(items, 0, this.items, 0, Math.min(items.length, this.items.length));
    }

    @Override
    public Item[] getJavaItems(UserConnection user) {
        final Item[] combinedItems = StructuredItem.emptyArray(45);
        final Item[] inventoryItems = super.getJavaItems(user);

        System.arraycopy(inventoryItems, 9, combinedItems, 9, 27);
        System.arraycopy(inventoryItems, 0, combinedItems, 36, 9);

        return combinedItems;
    }

}
