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
package net.raphimc.viabedrock.experimental.model.inventory;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ComplexInventoryTransaction_Type;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemUseInventoryTransaction_PredictedResult;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.Position3f;

import java.util.List;

//All of this is taken from on https://github.com/CloudburstMC/Protocol/
public record BedrockInventoryTransaction(
        int legacyRequestId,
        List<LegacySetItemSlotData> legacySlots,
        List<InventoryActionData> actions,
        ComplexInventoryTransaction_Type transactionType,
        int actionType,
        long runtimeEntityId,
        BlockPosition blockPosition,
        int blockFace,
        int hotbarSlot,
        BedrockItem itemInHand,
        Position3f playerPosition,
        Position3f clickPosition,
        Position3f headPosition,
        /**
         * Block definition of block being picked.
         * ItemUseInventoryTransaction only
         */
        int blockRuntimeId, //TODO: Find actual type
        ItemUseInventoryTransaction_TriggerType transactionTriggerType,
        ItemUseInventoryTransaction_PredictedResult predictedResult

) {
}
