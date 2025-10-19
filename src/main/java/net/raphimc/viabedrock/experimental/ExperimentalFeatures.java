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
package net.raphimc.viabedrock.experimental;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.experimental.model.inventory.InventorySource;
import net.raphimc.viabedrock.experimental.util.ProtocolUtil;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.data.enums.java.InteractionHand;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerActionAction;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.experimental.model.inventory.BedrockInventoryTransaction;

import java.util.List;
import java.util.logging.Level;

/**
 * This class is used to register experimental features that are not yet stable/tested enough to be included in the main protocol.
 * These features may be subject to change or removal in future versions.
 */
public class ExperimentalFeatures {

    public static void registerPacketTranslators(final BedrockProtocol protocol) {
        ProtocolUtil.prependServerbound(protocol, ServerboundPackets1_21_6.PLAYER_ACTION, wrapper -> {
            final PlayerActionAction action = PlayerActionAction.values()[wrapper.passthrough(Types.VAR_INT)]; // action
            wrapper.passthrough(Types.BLOCK_POSITION1_14); // block position
            wrapper.passthrough(Types.UNSIGNED_BYTE); // face
            wrapper.passthrough(Types.VAR_INT); // sequence number

            if (action != PlayerActionAction.RELEASE_USE_ITEM) {
                return;
            }


            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();

            wrapper.clearPacket();
            wrapper.setPacketType(ServerboundBedrockPackets.INVENTORY_TRANSACTION);

            wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemReleaseTransaction.getValue()); // transaction type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // actions count
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // action type
            wrapper.write(BedrockTypes.VAR_INT, (int) inventoryContainer.getSelectedHotbarSlot()); // selected hotbar slot
            wrapper.write(wrapper.user().get(ItemRewriter.class).itemType(), inventoryContainer.getSelectedHotbarItem()); // hand item
            wrapper.write(BedrockTypes.POSITION_3F, wrapper.user().get(EntityTracker.class).getClientPlayer().position()); // head position, the same as player position.

            wrapper.sendToServer(BedrockProtocol.class);
            wrapper.cancel();
        });

        // TODO: Track when the player start using item and send the StartUsingItem input data to the server.
        protocol.registerServerbound(ServerboundPackets1_21_6.USE_ITEM, ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();

            final int hand = wrapper.read(Types.VAR_INT); // hand
            wrapper.read(Types.VAR_INT); // sequence
            wrapper.read(Types.FLOAT); // yaw
            wrapper.read(Types.FLOAT); // pitch

            // Bedrock can't hold the majority of item in offhand and can't use any either.
            // TODO: We need to handle cases where the item changes, or it affect player movement (eg: eating/blocking/etc)
            if (hand != InteractionHand.MAIN_HAND.ordinal()) {
                wrapper.cancel();
                return;
            }

            wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemUseTransaction.getValue()); // transaction type

            // Actions are used to tell the server what item changed on the client-side, however this is never used on non-block interaction item use.
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // actions count

            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 1); // action type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_TriggerType.Unknown.getValue()); // trigger type

            wrapper.write(BedrockTypes.BLOCK_POSITION, new BlockPosition(0, 0, 0)); // block position

            // When player isn't right-clicking a block, block face always default back to 255
            // Block face will always be 255 if the player isn't right-clicking a block, this is vanilla behaviour.
            wrapper.write(BedrockTypes.VAR_INT, 255); // Block face

            wrapper.write(BedrockTypes.VAR_INT, (int) inventoryContainer.getSelectedHotbarSlot()); // hotbar slot
            wrapper.write(wrapper.user().get(ItemRewriter.class).itemType(), inventoryContainer.getSelectedHotbarItem()); // hand item
            wrapper.write(BedrockTypes.POSITION_3F, entityTracker.getClientPlayer().position()); // player position
            wrapper.write(BedrockTypes.POSITION_3F, Position3f.ZERO); // Click position

            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // block runtime id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_PredictedResult.Failure.getValue()); // predicted result.
        });

        //Block Placing
        protocol.registerServerbound(ServerboundPackets1_21_6.USE_ITEM_ON, ServerboundBedrockPackets.PLAYER_ACTION, wrapper -> {
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);
            final InteractionHand hand = InteractionHand.values()[wrapper.read(Types.VAR_INT)]; // hand
            BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_14); // block position
            BlockFace face = BlockFace.values()[wrapper.read(Types.VAR_INT)]; // face TODO: This is incorrect
            Position3f clickPosition = new Position3f(
                    wrapper.read(Types.FLOAT), // x
                    wrapper.read(Types.FLOAT), // y
                    wrapper.read(Types.FLOAT)  // z
            );
            boolean insideBlock = wrapper.read(Types.BOOLEAN); // inside block
            boolean worldBorder = wrapper.read(Types.BOOLEAN); // world border (Seems to always be false, even when interacting with blocks around or outside the world border, or while the player is outside the border.)
            int sequence = wrapper.read(Types.VAR_INT); // sequence number

            if (hand != InteractionHand.MAIN_HAND) {
                PacketFactory.sendJavaBlockChangedAck(wrapper.user(), sequence); // Prevent ghost blocks
                wrapper.cancel();
                return;
            }

            BlockPosition resultPos = position.getRelative(face);
            if (insideBlock) {
                resultPos = position;
            }

            //Block Place
            wrapper.write(BedrockTypes.VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
            wrapper.write(BedrockTypes.VAR_INT, PlayerActionType.StartItemUseOn.getValue()); // action type
            wrapper.write(BedrockTypes.POSITION_3I, position); // block position
            wrapper.write(BedrockTypes.POSITION_3I, resultPos); // result position
            wrapper.write(BedrockTypes.VAR_INT, face.ordinal()); // face

            //Bedrock requires an inventory transaction to be sent
            /*BedrockInventoryTransaction transaction = new BedrockInventoryTransaction(
                    0,
                    List.of(),
                    List.of(),
                    ComplexInventoryTransaction_Type.ItemUseTransaction,
                    0,
                    clientPlayer.runtimeId(), //TODO: Check
                    position,
                    face.ordinal(),
                    inventoryTracker.getInventoryContainer().getSelectedHotbarSlot(),
                    inventoryTracker.getInventoryContainer().getSelectedHotbarItem(),
                    clientPlayer.position(),
                    clickPosition,
                    null, //TODO
                    inventoryTracker.getInventoryContainer().getSelectedHotbarItem().blockRuntimeId(),
                    ItemUseInventoryTransaction_TriggerType.PlayerInput,
                    ItemUseInventoryTransaction_PredictedResult.Success
            );*/
            final PacketWrapper inventoryTransactionPacket = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper.user());
            //inventoryTransactionPacket.write(BedrockTypes.INVENTORY_TRANSACTION, transaction);
            inventoryTransactionPacket.write(BedrockTypes.VAR_INT, 0); // legacy request id
            inventoryTransactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemUseTransaction.getValue()); // transaction type
            inventoryTransactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, 1); // actions count

            //Send the action data
            inventoryTransactionPacket.write(BedrockTypes.INVENTORY_SOURCE, new InventorySource(
                    InventorySourceType.ContainerInventory,
                    0,
                    InventorySource_InventorySourceFlags.NoFlag
            ));
            inventoryTransactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, (int) inventoryTracker.getInventoryContainer().getSelectedHotbarSlot()); // slot
            inventoryTransactionPacket.write(itemRewriter.itemType(), inventoryTracker.getInventoryContainer().getSelectedHotbarItem()); // from item
            inventoryTransactionPacket.write(itemRewriter.itemType(), inventoryTracker.getInventoryContainer().getSelectedHotbarItem().copyAndDecrease()); // to item
            inventoryTransactionPacket.write(BedrockTypes.VAR_INT, 0); // stack network id

            inventoryTransactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // action type
            inventoryTransactionPacket.write(BedrockTypes.POSITION_3I, position); // block position
            inventoryTransactionPacket.write(BedrockTypes.VAR_INT, face.ordinal()); // block face
            inventoryTransactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, (int) inventoryTracker.getInventoryContainer().getSelectedHotbarSlot()); // hotbar slot
            inventoryTransactionPacket.write(itemRewriter.itemType(), inventoryTracker.getInventoryContainer().getSelectedHotbarItem()); // hand item
            inventoryTransactionPacket.write(BedrockTypes.POSITION_3F, clientPlayer.position()); // player position
            inventoryTransactionPacket.write(BedrockTypes.POSITION_3F, clickPosition); // click position
            inventoryTransactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, inventoryTracker.getInventoryContainer().getSelectedHotbarItem().blockRuntimeId()); // block runtime id
            inventoryTransactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_TriggerType.PlayerInput.getValue()); // trigger type
            inventoryTransactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_PredictedResult.Success.getValue()); // predicted result

            inventoryTransactionPacket.scheduleSendToServer(BedrockProtocol.class);

            //Bedrock requires a StopItemUse packet to be sent
            final PacketWrapper stopItemUsePacket = PacketWrapper.create(ServerboundBedrockPackets.PLAYER_ACTION, wrapper.user());
            stopItemUsePacket.write(BedrockTypes.VAR_LONG, clientPlayer.runtimeId());
            stopItemUsePacket.write(BedrockTypes.VAR_INT, PlayerActionType.StopItemUseOn.getValue());
            stopItemUsePacket.write(BedrockTypes.POSITION_3I, position);
            stopItemUsePacket.write(BedrockTypes.POSITION_3I, new BlockPosition(0, 0, 0)); // result position (Origin is sent by the bedrock client)
            stopItemUsePacket.write(BedrockTypes.VAR_INT, 0); // face (0 is sent by the bedrock client)
            stopItemUsePacket.scheduleSendToServer(BedrockProtocol.class);

            //Not necessarily required as bedrock sends a Block Update packet, but it should help with ghost blocks if the place fails
            PacketFactory.sendJavaBlockChangedAck(wrapper.user(), sequence);
        });

    }

    public static void registerTasks() {
    }

}

//InventoryTransactionPacket(legacyRequestId=0, legacySlots=[], actions=[InventoryActionData(source=InventorySource(type=CONTAINER, containerId=0, flag=NONE), slot=3, fromItem=BaseItemData(definition=SimpleItemDefinition(identifier=minecraft:dirt, runtimeId=3, version=LEGACY, componentBased=false, componentData=null), damage=0, count=1, tag=null, canPlace=[], canBreak=[], blockingTicks=0, blockDefinition=UnknownDefinition[runtimeId=-2108756090], usingNetId=false, netId=0), toItem=BaseItemData(definition=SimpleItemDefinition(identifier=minecraft:air, runtimeId=0, version=LEGACY, componentBased=false, componentData=null), damage=0, count=0, tag=null, canPlace=[], canBreak=[], blockingTicks=0, blockDefinition=null, usingNetId=false, netId=0), stackNetworkId=0)], transactionType=ITEM_USE, actionType=0, runtimeEntityId=0, blockPosition=(11, 110, 0), blockFace=1, hotbarSlot=3, itemInHand=BaseItemData(definition=SimpleItemDefinition(identifier=minecraft:dirt, runtimeId=3, version=LEGACY, componentBased=false, componentData=null), damage=0, count=1, tag=null, canPlace=[], canBreak=[], blockingTicks=0, blockDefinition=UnknownDefinition[runtimeId=-2108756090], usingNetId=false, netId=0), playerPosition=(12.311914, 112.62001, -1.25097), clickPosition=(0.6086874, 1.0, 0.49341834), headPosition=null, usingNetIds=false, blockDefinition=UnknownDefinition[runtimeId=-2108756090], triggerType=PLAYER_INPUT, clientInteractPrediction=SUCCESS)
