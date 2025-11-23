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
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemReleaseInventoryTransaction_ActionType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemUseInventoryTransaction_ActionType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemUseInventoryTransaction_PredictedResult;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemUseOnActorInventoryTransaction_ActionType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.Position3f;

public interface InventoryTransactionData {

    // NormalTransactionData represents an inventory transaction data object for normal transactions, such as crafting. It has no content.
    record NormalTransactionData() implements InventoryTransactionData {}

    // MismatchTransactionData represents a mismatched inventory transaction's data object.
    record MismatchTransactionData() implements InventoryTransactionData {}


    // UseItemTransactionData represents an inventory transaction data object sent when the client uses an item on a block.
    record UseItemTransactionData(
            ItemUseInventoryTransaction_ActionType actionType,
            ItemUseInventoryTransaction_TriggerType triggerType,
            BlockPosition blockPosition,
            int face,
            int hotbarSlot,
            BedrockItem itemInHand,
            Position3f playerPosition,
            Position3f clickPosition,
            int blockRuntimeId,
            ItemUseInventoryTransaction_PredictedResult predictedResult
    ) implements InventoryTransactionData {}

    // UseItemOnEntityTransactionData represents an inventory transaction data object sent when the client uses an item on an entity.
    record UseItemOnEntityTransactionData(
            long entityRuntimeId,
            ItemUseOnActorInventoryTransaction_ActionType actionType,
            int hotbarSlot,
            BedrockItem itemInHand,
            Position3f playerPosition,
            Position3f clickPosition
    ) implements InventoryTransactionData {}

    // ReleaseItemTransactionData represents an inventory transaction data object sent when the client releases the item it was using,
    // for example when stopping while eating or stopping the charging of a bow.
    record ReleaseItemTransactionData(
            ItemReleaseInventoryTransaction_ActionType actionType,
            int hotbarSlot,
            BedrockItem itemInHand,
            Position3f headPosition
    ) implements InventoryTransactionData {}

}
