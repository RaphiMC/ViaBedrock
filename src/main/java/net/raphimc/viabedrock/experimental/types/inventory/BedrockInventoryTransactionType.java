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

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.experimental.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.experimental.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.experimental.model.inventory.LegacySetItemSlotData;
import net.raphimc.viabedrock.experimental.types.ExperimentalTypes;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ComplexInventoryTransaction_Type;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemUseInventoryTransaction_PredictedResult;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.List;

public class BedrockInventoryTransactionType extends Type<BedrockInventoryTransaction> {

    private ItemRewriter itemRewriter;

    public BedrockInventoryTransactionType() {
        super(BedrockInventoryTransaction.class);
    }

    @Override
    public BedrockInventoryTransaction read(ByteBuf buffer) {
        final int legacyRequestId = BedrockTypes.VAR_INT.read(buffer);

        List<LegacySetItemSlotData> legacySlots = List.of();
        if (legacyRequestId != 0) {
            legacySlots = List.of(ExperimentalTypes.LEGACY_SET_ITEM_SLOT_DATA.read(buffer));
        }
        final ComplexInventoryTransaction_Type transactionType = ComplexInventoryTransaction_Type.getByValue(BedrockTypes.UNSIGNED_VAR_INT.read(buffer));
        final List<InventoryActionData> actions = List.of(ExperimentalTypes.INVENTORY_ACTION_DATA.read(buffer));
        final int actionType = BedrockTypes.VAR_INT.read(buffer);
        final ItemUseInventoryTransaction_TriggerType transactionTriggerType = ItemUseInventoryTransaction_TriggerType.getByValue(BedrockTypes.VAR_INT.read(buffer));
        final long runtimeEntityId = BedrockTypes.VAR_LONG.read(buffer);
        final BlockPosition blockPosition = BedrockTypes.BLOCK_POSITION.read(buffer);
        final int blockFace = BedrockTypes.VAR_INT.read(buffer);
        final int hotbarSlot = BedrockTypes.VAR_INT.read(buffer);
        final BedrockItem itemInHand = itemRewriter.itemType().read(buffer); //TODO
        final Position3f playerPosition = BedrockTypes.POSITION_3F.read(buffer);
        final Position3f clickPosition = BedrockTypes.POSITION_3F.read(buffer);
        final Position3f headPosition = BedrockTypes.POSITION_3F.read(buffer);
        final boolean usingNetIds = buffer.readBoolean();
        final int blockRuntimeId = BedrockTypes.VAR_INT.read(buffer); //TODO: Could potentially be unsigned needs double checking
        final ItemUseInventoryTransaction_PredictedResult predictedResult = ItemUseInventoryTransaction_PredictedResult.getByValue(BedrockTypes.VAR_INT.read(buffer));

        return new BedrockInventoryTransaction(
                legacyRequestId,
                legacySlots,
                actions,
                transactionType,
                actionType,
                runtimeEntityId,
                blockPosition,
                blockFace,
                hotbarSlot,
                itemInHand,
                playerPosition,
                clickPosition,
                headPosition,
                blockRuntimeId,
                transactionTriggerType,
                predictedResult
        );
    }

    @Override
    public void write(ByteBuf buffer, BedrockInventoryTransaction value) {
        BedrockTypes.VAR_INT.write(buffer, value.legacyRequestId());
        if (value.legacyRequestId() != 0) {
            ExperimentalTypes.LEGACY_SET_ITEM_SLOT_DATA.write(buffer, value.legacySlots().toArray(new LegacySetItemSlotData[0]));
        }
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.transactionType().getValue());
        ExperimentalTypes.INVENTORY_ACTION_DATA.write(buffer, value.actions().toArray(new InventoryActionData[0]));
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.actionType());
        BedrockTypes.VAR_INT.write(buffer, value.transactionTriggerType().getValue());
        //BedrockTypes.VAR_LONG.write(buffer, value.runtimeEntityId());
        BedrockTypes.BLOCK_POSITION.write(buffer, value.blockPosition());
        BedrockTypes.VAR_INT.write(buffer, value.blockFace());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.hotbarSlot());
        itemRewriter.itemType().write(buffer, value.itemInHand()); //TODO
        BedrockTypes.POSITION_3F.write(buffer, value.playerPosition());
        BedrockTypes.POSITION_3F.write(buffer, value.clickPosition());
        //BedrockTypes.POSITION_3F.write(buffer, value.headPosition());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.blockRuntimeId());
        //buffer.writeBoolean(false); //TODO: usingNetIds
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.predictedResult().getValue());
    }
}
