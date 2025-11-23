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
import net.raphimc.viabedrock.experimental.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.experimental.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.experimental.model.inventory.InventoryTransactionData;
import net.raphimc.viabedrock.experimental.model.inventory.LegacySetItemSlotData;
import net.raphimc.viabedrock.experimental.types.ExperimentalBedrockTypes;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.List;

public class InventoryTransactionPacketType extends Type<BedrockInventoryTransaction> {

    private ItemRewriter itemRewriter; //TODO

    public InventoryTransactionPacketType() {
        super(BedrockInventoryTransaction.class);
    }

    @Override
    public BedrockInventoryTransaction read(ByteBuf buffer) {
        int legacyRequestId = BedrockTypes.VAR_INT.read(buffer);
        LegacySetItemSlotData[] legacySlots = new LegacySetItemSlotData[0];
        if (legacyRequestId != 0) {
            legacySlots = ExperimentalBedrockTypes.LEGACY_SET_ITEM_SLOT_DATA.read(buffer);
        }
        ComplexInventoryTransaction_Type type = ComplexInventoryTransaction_Type.getByValue(BedrockTypes.UNSIGNED_VAR_INT.read(buffer));
        InventoryActionData[] actions = ExperimentalBedrockTypes.INVENTORY_ACTION_DATA.read(buffer);
        InventoryTransactionData transactionData = switch (type) {
            case NormalTransaction ->  new InventoryTransactionData.NormalTransactionData();
            case InventoryMismatch -> new InventoryTransactionData.MismatchTransactionData();
            case ItemUseTransaction -> new InventoryTransactionData.UseItemTransactionData(
                    BedrockTypes.VAR_INT.read(buffer),
                    ExperimentalBedrockTypes.LEGACY_SET_ITEM_SLOT_DATA.read(buffer),
                    ExperimentalBedrockTypes.INVENTORY_ACTION_DATA.read(buffer),
                    ItemUseInventoryTransaction_ActionType.getByValue(BedrockTypes.UNSIGNED_VAR_INT.read(buffer)),
                    ItemUseInventoryTransaction_TriggerType.getByValue(BedrockTypes.UNSIGNED_VAR_INT.read(buffer)),
                    BedrockTypes.BLOCK_POSITION.read(buffer),
                    BedrockTypes.VAR_INT.read(buffer),
                    BedrockTypes.VAR_INT.read(buffer),
                    itemRewriter.itemType().read(buffer), //TODO
                    BedrockTypes.POSITION_3F.read(buffer),
                    BedrockTypes.POSITION_3F.read(buffer),
                    BedrockTypes.UNSIGNED_VAR_INT.read(buffer),
                    ItemUseInventoryTransaction_PredictedResult.getByValue(BedrockTypes.UNSIGNED_VAR_INT.read(buffer))
            );
            case ItemUseOnEntityTransaction -> new InventoryTransactionData.UseItemOnEntityTransactionData(
                    BedrockTypes.VAR_LONG.read(buffer),
                    ItemUseOnActorInventoryTransaction_ActionType.getByValue(BedrockTypes.UNSIGNED_VAR_INT.read(buffer)),
                    BedrockTypes.VAR_INT.read(buffer),
                    itemRewriter.itemType().read(buffer), //TODO
                    BedrockTypes.POSITION_3F.read(buffer),
                    BedrockTypes.POSITION_3F.read(buffer)
            );
            case ItemReleaseTransaction -> new InventoryTransactionData.ReleaseItemTransactionData(
                    ItemReleaseInventoryTransaction_ActionType.getByValue(BedrockTypes.UNSIGNED_VAR_INT.read(buffer)),
                    BedrockTypes.VAR_INT.read(buffer),
                    itemRewriter.itemType().read(buffer), //TODO
                    BedrockTypes.POSITION_3F.read(buffer)
            );
        };

        return new BedrockInventoryTransaction(legacyRequestId, List.of(legacySlots), List.of(actions), type, transactionData);
    }

    @Override
    public void write(ByteBuf buffer, BedrockInventoryTransaction bedrockInventoryTransaction) {
        BedrockTypes.VAR_INT.write(buffer, bedrockInventoryTransaction.legacyRequestId());
        if (bedrockInventoryTransaction.legacyRequestId() != 0) {
            ExperimentalBedrockTypes.LEGACY_SET_ITEM_SLOT_DATA.write(buffer, bedrockInventoryTransaction.legacySlots().toArray(new LegacySetItemSlotData[0]));
        }
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, bedrockInventoryTransaction.transactionType().getValue());
        ExperimentalBedrockTypes.INVENTORY_ACTION_DATA.write(buffer, bedrockInventoryTransaction.actions().toArray(new InventoryActionData[0]));
        switch (bedrockInventoryTransaction.transactionType()) {
            case NormalTransaction, InventoryMismatch -> {
                // No additional data to write
            }
            case ItemUseTransaction -> {
                InventoryTransactionData.UseItemTransactionData data = (InventoryTransactionData.UseItemTransactionData) bedrockInventoryTransaction.transactionData();
                BedrockTypes.VAR_INT.write(buffer, data.legacyRequestId());
                ExperimentalBedrockTypes.LEGACY_SET_ITEM_SLOT_DATA.write(buffer, data.legacySetItemSlots());
                ExperimentalBedrockTypes.INVENTORY_ACTION_DATA.write(buffer, data.actions());
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, data.actionType().getValue());
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, data.triggerType().getValue());
                BedrockTypes.BLOCK_POSITION.write(buffer, data.blockPosition());
                BedrockTypes.VAR_INT.write(buffer, data.face());
                BedrockTypes.VAR_INT.write(buffer, data.hotbarSlot());
                itemRewriter.itemType().write(buffer, data.itemInHand()); //TODO
                BedrockTypes.POSITION_3F.write(buffer, data.playerPosition());
                BedrockTypes.POSITION_3F.write(buffer, data.clickPosition());
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, data.blockRuntimeId());
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, data.predictedResult().getValue());
            }
            case ItemUseOnEntityTransaction -> {
                InventoryTransactionData.UseItemOnEntityTransactionData data = (InventoryTransactionData.UseItemOnEntityTransactionData) bedrockInventoryTransaction.transactionData();
                BedrockTypes.VAR_LONG.write(buffer, data.entityRuntimeId());
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, data.actionType().getValue());
                BedrockTypes.VAR_INT.write(buffer, data.hotbarSlot());
                itemRewriter.itemType().write(buffer, data.itemInHand()); //TODO
                BedrockTypes.POSITION_3F.write(buffer, data.playerPosition());
                BedrockTypes.POSITION_3F.write(buffer, data.clickPosition());
            }
            case ItemReleaseTransaction -> {
                InventoryTransactionData.ReleaseItemTransactionData data = (InventoryTransactionData.ReleaseItemTransactionData) bedrockInventoryTransaction.transactionData();
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, data.actionType().getValue());
                BedrockTypes.VAR_INT.write(buffer, data.hotbarSlot());
                itemRewriter.itemType().write(buffer, data.itemInHand()); //TODO
                BedrockTypes.POSITION_3F.write(buffer, data.headPosition());
            }
        }
    }
}
