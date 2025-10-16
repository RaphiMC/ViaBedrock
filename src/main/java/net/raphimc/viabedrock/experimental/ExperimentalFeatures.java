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
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ComplexInventoryTransaction_Type;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemUseInventoryTransaction_PredictedResult;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerActionType;
import net.raphimc.viabedrock.protocol.data.enums.java.InteractionHand;
import net.raphimc.viabedrock.experimental.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.List;

/**
 * This class is used to register experimental features that are not yet stable/tested enough to be included in the main protocol.
 * These features may be subject to change or removal in future versions.
 */
public class ExperimentalFeatures {

    public static void registerPacketTranslators(final BedrockProtocol protocol) {

        //Block Placing
        protocol.registerServerbound(ServerboundPackets1_21_6.USE_ITEM_ON, ServerboundBedrockPackets.PLAYER_ACTION, wrapper -> {
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
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
            BedrockInventoryTransaction transaction = new BedrockInventoryTransaction(
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
                    null, //TODO
                    ItemUseInventoryTransaction_TriggerType.PlayerInput,
                    ItemUseInventoryTransaction_PredictedResult.Success
            );
            final PacketWrapper inventoryTransactionPacket = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper.user());
            inventoryTransactionPacket.write(BedrockTypes.INVENTORY_TRANSACTION, transaction);
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
