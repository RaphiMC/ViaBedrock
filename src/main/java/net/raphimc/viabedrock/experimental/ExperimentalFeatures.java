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

import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.experimental.model.inventory.InventorySource;
import net.raphimc.viabedrock.experimental.types.ExperimentalTypes;
import net.raphimc.viabedrock.experimental.util.ProtocolUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.data.enums.java.InteractionHand;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerActionAction;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

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
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_ActionType.Place.getValue()); // action type
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

            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_ActionType.Use.getValue()); // action type
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

        protocol.registerServerbound(ServerboundPackets1_21_6.USE_ITEM_ON, null, wrapper -> {
            wrapper.cancel();

            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);

            final InteractionHand hand = InteractionHand.values()[wrapper.read(Types.VAR_INT)]; // hand

            BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_14); // block position
            int faceInt = wrapper.read(Types.UNSIGNED_BYTE); // face
            BlockFace face = getBlockFace(faceInt);
            //BlockFace face = BlockFace.values()[wrapper.read(Types.VAR_INT)]; // face
            Position3f clickPosition = new Position3f(
                    wrapper.read(Types.FLOAT), // x
                    wrapper.read(Types.FLOAT), // y
                    wrapper.read(Types.FLOAT)  // z
            );
            boolean insideBlock = wrapper.read(Types.BOOLEAN); // inside block
            wrapper.read(Types.BOOLEAN); // world border, this doesn't exist on Bedrock.

            // Send back block changed ack with the sequence, this will help with ghost blocks.
            PacketFactory.sendJavaBlockChangedAck(wrapper.user(), wrapper.read(Types.VAR_INT));

            // The player can only interact using the main hand on Bedrock!
            if (hand != InteractionHand.MAIN_HAND) {
                return;
            }

            // The bedrock client will send a start item use on action to the server first.
            final PacketWrapper startItemUseOn = PacketWrapper.create(ServerboundBedrockPackets.PLAYER_ACTION, wrapper.user());
            startItemUseOn.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
            startItemUseOn.write(BedrockTypes.VAR_INT, PlayerActionType.StartItemUseOn.getValue()); // action type
            startItemUseOn.write(BedrockTypes.BLOCK_POSITION, position); // block position
            startItemUseOn.write(BedrockTypes.BLOCK_POSITION, insideBlock ? position : position.getRelative(face)); // result position
            startItemUseOn.write(BedrockTypes.VAR_INT, faceInt); // face
            startItemUseOn.sendToServer(BedrockProtocol.class);

            // This is the main packet that the bedrock client use to interact with block.
            final PacketWrapper transactionPacket = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper.user());
            transactionPacket.write(BedrockTypes.VAR_INT, 0); // legacy request id
            transactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemUseTransaction.getValue()); // transaction type
            transactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, 1); // actions count

            // This the action to tell the server what item changed.
            transactionPacket.write(ExperimentalTypes.INVENTORY_SOURCE, new InventorySource(InventorySourceType.ContainerInventory, ContainerID.CONTAINER_ID_INVENTORY.getValue(), InventorySource_InventorySourceFlags.NoFlag));
            transactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, (int) inventoryTracker.getInventoryContainer().getSelectedHotbarSlot()); // slot
            transactionPacket.write(itemRewriter.itemType(), inventoryTracker.getInventoryContainer().getSelectedHotbarItem()); // from item

            BedrockItem predictedToItem = inventoryTracker.getInventoryContainer().getSelectedHotbarItem().copy();
            // This is not entirely correct, but at least it's more accurate than not sending actions or sending the original item data.
            if (predictedToItem.blockRuntimeId() != 0) {
                predictedToItem.setAmount(predictedToItem.amount() - 1);
            }
            if (predictedToItem.amount() <= 0) {
                predictedToItem = BedrockItem.empty();
            }

            transactionPacket.write(itemRewriter.itemType(), predictedToItem); // to item

            transactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_ActionType.Place.getValue()); // action type
            transactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_TriggerType.PlayerInput.getValue()); // trigger type
            transactionPacket.write(BedrockTypes.BLOCK_POSITION, position); // block position
            transactionPacket.write(BedrockTypes.VAR_INT, faceInt); // block face
            transactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, (int) inventoryTracker.getInventoryContainer().getSelectedHotbarSlot()); // hotbar slot
            transactionPacket.write(itemRewriter.itemType(), inventoryTracker.getInventoryContainer().getSelectedHotbarItem()); // hand item
            transactionPacket.write(BedrockTypes.POSITION_3F, clientPlayer.position()); // player position
            transactionPacket.write(BedrockTypes.POSITION_3F, clickPosition); // click position

            transactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, chunkTracker.getBlockState(position)); // block runtime id
            transactionPacket.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_PredictedResult.Success.getValue()); // predicted result

            transactionPacket.sendToServer(BedrockProtocol.class);

            // Bedrock sends a stop item use on after the transaction packet
            final PacketWrapper stopItemUseOn = PacketWrapper.create(ServerboundBedrockPackets.PLAYER_ACTION, wrapper.user());
            stopItemUseOn.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId());
            stopItemUseOn.write(BedrockTypes.VAR_INT, PlayerActionType.StopItemUseOn.getValue());
            stopItemUseOn.write(BedrockTypes.BLOCK_POSITION, position);
            stopItemUseOn.write(BedrockTypes.BLOCK_POSITION, new BlockPosition(0, 0, 0)); // result position (Origin is sent by the bedrock client)
            stopItemUseOn.write(BedrockTypes.VAR_INT, 0); // face (0 is sent by the bedrock client)
            stopItemUseOn.sendToServer(BedrockProtocol.class);
        });

    }

    public static void registerTasks() {
    }

    //TODO: Viaversion should fix the BlockFace ordinals
    private static BlockFace getBlockFace(int face) {
        return switch (face) {
            case 0 -> BlockFace.BOTTOM;
            case 1 -> BlockFace.TOP;
            case 2 -> BlockFace.NORTH;
            case 3 -> BlockFace.SOUTH;
            case 4 -> BlockFace.WEST;
            case 5 -> BlockFace.EAST;
            default -> {
                ViaBedrock.getPlatform().getLogger().warning("Unknown block face: " + face);
                yield BlockFace.TOP;
            }
        };
    }

}