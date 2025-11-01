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
package net.raphimc.viabedrock.experimental.types;

import com.viaversion.viaversion.api.type.Type;
import net.raphimc.viabedrock.experimental.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.experimental.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.experimental.model.inventory.InventorySource;
import net.raphimc.viabedrock.experimental.model.inventory.LegacySetItemSlotData;
import net.raphimc.viabedrock.experimental.types.inventory.BedrockInventoryTransactionType;
import net.raphimc.viabedrock.experimental.types.inventory.InventoryActionDataType;
import net.raphimc.viabedrock.experimental.types.inventory.InventorySourcePacketType;
import net.raphimc.viabedrock.experimental.types.inventory.LegacySetItemSlotDataType;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.array.ArrayType;

public class ExperimentalTypes {

    public static final Type<BedrockInventoryTransaction> INVENTORY_TRANSACTION = new BedrockInventoryTransactionType();
    public static final Type<LegacySetItemSlotData[]> LEGACY_SET_ITEM_SLOT_DATA = new ArrayType<>(new LegacySetItemSlotDataType(), BedrockTypes.UNSIGNED_VAR_INT);
    public static final Type<InventorySource> INVENTORY_SOURCE = new InventorySourcePacketType();
    public static final Type<InventoryActionData[]> INVENTORY_ACTION_DATA = new ArrayType<>(new InventoryActionDataType(), BedrockTypes.UNSIGNED_VAR_INT);

}
