/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.block.AnvilContainer;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.experimental.model.inventory.*;
import net.raphimc.viabedrock.experimental.model.recipe.*;
import net.raphimc.viabedrock.experimental.rewriter.InventoryTransactionRewriter;
import net.raphimc.viabedrock.experimental.storage.CraftingDataStorage;
import net.raphimc.viabedrock.experimental.storage.CraftingDataTracker;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestStorage;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestTracker;
import net.raphimc.viabedrock.experimental.types.ExperimentalBedrockTypes;
import net.raphimc.viabedrock.experimental.util.ProtocolUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.Direction;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.data.enums.java.InteractionHand;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.GameMode;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.PlayerActionAction;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This class is used to register experimental features that are not yet stable/tested enough to be included in the main protocol.
 * These features may be subject to change or removal in future versions.
 */
public class ExperimentalFeatures {

    public static void registerPacketTranslators(final BedrockProtocol protocol) {
        ProtocolUtil.prependServerbound(protocol, ServerboundPackets1_21_6.PLAYER_ACTION, wrapper -> {
            final InventoryTransactionRewriter inventoryTransactionRewriter = wrapper.user().get(InventoryTransactionRewriter.class);
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);

            final PlayerActionAction action = PlayerActionAction.values()[wrapper.passthrough(Types.VAR_INT)]; // action
            wrapper.passthrough(Types.BLOCK_POSITION1_14); // block position
            wrapper.passthrough(Types.UNSIGNED_BYTE); // face
            wrapper.passthrough(Types.VAR_INT); // sequence number

            if (action == PlayerActionAction.RELEASE_USE_ITEM) {
                final InventoryContainer inventoryContainer = inventoryTracker.getInventoryContainer();

                wrapper.clearPacket();
                wrapper.setPacketType(ServerboundBedrockPackets.INVENTORY_TRANSACTION);

                BedrockInventoryTransaction inventoryTransaction = new BedrockInventoryTransaction(
                        0, // legacy request id
                        null,
                        null,
                        ComplexInventoryTransaction_Type.ItemReleaseTransaction,
                        new InventoryTransactionData.ReleaseItemTransactionData(
                                ItemReleaseInventoryTransaction_ActionType.Release,
                                inventoryContainer.getSelectedHotbarSlot(),
                                inventoryContainer.getSelectedHotbarItem(),
                                wrapper.user().get(EntityTracker.class).getClientPlayer().position()
                        )
                );
                wrapper.write(inventoryTransactionRewriter.getInventoryTransactionType(), inventoryTransaction);

                wrapper.sendToServer(BedrockProtocol.class);
                wrapper.cancel();
            } else if (action == PlayerActionAction.DROP_ITEM || action == PlayerActionAction.DROP_ALL_ITEMS) {
                final BedrockItem currentItem = inventoryTracker.getInventoryContainer().getSelectedHotbarItem();

                wrapper.cancel();
                if (currentItem.isEmpty()) {
                    return;
                }

                BedrockItem predictedAmount = currentItem.copy();
                if (action == PlayerActionAction.DROP_ITEM) {
                    predictedAmount.setAmount(1); // Drop a single item
                }

                BedrockItem predictedToItem = currentItem.copy();
                if (action == PlayerActionAction.DROP_ITEM) {
                    if (predictedToItem.amount() > 1) {
                        predictedToItem.setAmount(currentItem.amount() - 1);
                    } else {
                        predictedToItem = BedrockItem.empty();
                    }
                } else {
                    predictedToItem = BedrockItem.empty();
                }

                final PacketWrapper transactionPacket = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper.user());

                BedrockInventoryTransaction inventoryTransaction = new BedrockInventoryTransaction(
                        0,
                        null,
                        List.of(
                                new InventoryActionData(
                                        new InventorySource(InventorySourceType.WorldInteraction, ContainerID.CONTAINER_ID_NONE.getValue(), InventorySource_InventorySourceFlags.NoFlag),
                                        0,
                                        BedrockItem.empty(),
                                        predictedAmount
                                ),
                                new InventoryActionData(
                                        new InventorySource(InventorySourceType.ContainerInventory, ContainerID.CONTAINER_ID_INVENTORY.getValue(), InventorySource_InventorySourceFlags.NoFlag),
                                        inventoryTracker.getInventoryContainer().getSelectedHotbarSlot(),
                                        currentItem,
                                        predictedToItem
                                )
                        ),

                        ComplexInventoryTransaction_Type.NormalTransaction,
                        new InventoryTransactionData.NormalTransactionData()
                );

                transactionPacket.write(inventoryTransactionRewriter.getInventoryTransactionType(), inventoryTransaction);

                transactionPacket.sendToServer(BedrockProtocol.class);

                //TODO: I think vanilla client also sends these and im not sure what their purposes are but it works without them
                    /*final PacketWrapper interactPacket = PacketWrapper.create(ServerboundBedrockPackets.INTERACT, wrapper.user());

                    interactPacket.write(Types.BYTE, (byte) InteractPacket_Action.InteractUpdate.getValue());
                    interactPacket.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId());
                    interactPacket.write(BedrockTypes.POSITION_3F, new Position3f(0, 0, 0));

                    interactPacket.sendToServer(BedrockProtocol.class);

                    final PacketWrapper mobEquipPacket = PacketWrapper.create(ServerboundBedrockPackets.MOB_EQUIPMENT, wrapper.user());

                    mobEquipPacket.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId());
                    mobEquipPacket.write(itemRewriter.itemType(), predictedToItem);
                    mobEquipPacket.write(Types.BYTE, inventoryTracker.getInventoryContainer().getSelectedHotbarSlot());
                    mobEquipPacket.write(Types.BYTE, inventoryTracker.getInventoryContainer().getSelectedHotbarSlot());
                    mobEquipPacket.write(Types.BYTE, (byte) ContainerID.CONTAINER_ID_INVENTORY.getValue());

                    mobEquipPacket.sendToServer(BedrockProtocol.class);*/
            }


        });

        // TODO: Track when the player start using item and send the StartUsingItem input data to the server.
        protocol.registerServerbound(ServerboundPackets1_21_6.USE_ITEM, ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();
            final InventoryTransactionRewriter inventoryTransactionRewriter = wrapper.user().get(InventoryTransactionRewriter.class);

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

            BedrockInventoryTransaction inventoryTransaction = new BedrockInventoryTransaction(
                    0, // legacy request id
                    null,
                    null,
                    ComplexInventoryTransaction_Type.ItemUseTransaction,
                    new InventoryTransactionData.UseItemTransactionData(
                            ItemUseInventoryTransaction_ActionType.Use,
                            ItemUseInventoryTransaction_TriggerType.Unknown,
                            new BlockPosition(0, 0, 0), // block position
                            255, // block face
                            inventoryContainer.getSelectedHotbarSlot(),
                            inventoryContainer.getSelectedHotbarItem(),
                            entityTracker.getClientPlayer().position(),
                            Position3f.ZERO, // click position
                            0, // block runtime id
                            ItemUseInventoryTransaction_PredictedResult.Failure
                    )
            );
            wrapper.write(inventoryTransactionRewriter.getInventoryTransactionType(), inventoryTransaction);
        });

        protocol.registerServerbound(ServerboundPackets1_21_6.USE_ITEM_ON, null, wrapper -> {
            wrapper.cancel();

            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            final InventoryTransactionRewriter inventoryTransactionRewriter = wrapper.user().get(InventoryTransactionRewriter.class);

            final InteractionHand hand = InteractionHand.values()[wrapper.read(Types.VAR_INT)]; // hand

            BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_14); // block position
            int faceInt = wrapper.read(Types.UNSIGNED_BYTE); // face
            Direction direction = Direction.getFromVerticalId(faceInt);
            if (direction == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown block face id: " + faceInt);
                return;
            }
            BlockFace face = direction.blockFace();
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
            ExperimentalPacketFactory.sendBedrockPlayerAction(
                    wrapper.user(),
                    clientPlayer.runtimeId(),
                    PlayerActionType.StartItemUseOn,
                    position,
                    insideBlock ? position : position.getRelative(face),
                    faceInt
            );

            // This is the main packet that the bedrock client use to interact with block.The rest of the
            final PacketWrapper transactionPacket = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper.user());

            BedrockItem predictedToItem = inventoryTracker.getInventoryContainer().getSelectedHotbarItem().copy();
            // This is not entirely correct, but at least it's more accurate than not sending actions or sending the original item data.
            if (predictedToItem.blockRuntimeId() != 0 && clientPlayer.javaGameMode() != GameMode.CREATIVE) {
                predictedToItem.setAmount(predictedToItem.amount() - 1);
            }
            if (predictedToItem.amount() <= 0) {
                predictedToItem = BedrockItem.empty();
            }

            BedrockInventoryTransaction inventoryTransaction = new BedrockInventoryTransaction(
                    0, // legacy request id
                    null,
                    List.of(
                            new InventoryActionData(
                                    new InventorySource(InventorySourceType.ContainerInventory, ContainerID.CONTAINER_ID_INVENTORY.getValue(), InventorySource_InventorySourceFlags.NoFlag),
                                    inventoryTracker.getInventoryContainer().getSelectedHotbarSlot(),
                                    inventoryTracker.getInventoryContainer().getSelectedHotbarItem(),
                                    predictedToItem
                            )
                    ),
                    ComplexInventoryTransaction_Type.ItemUseTransaction,
                    new InventoryTransactionData.UseItemTransactionData(
                            ItemUseInventoryTransaction_ActionType.Place,
                            ItemUseInventoryTransaction_TriggerType.PlayerInput,
                            position,
                            faceInt,
                            inventoryTracker.getInventoryContainer().getSelectedHotbarSlot(),
                            inventoryTracker.getInventoryContainer().getSelectedHotbarItem(),
                            clientPlayer.position(),
                            clickPosition,
                            chunkTracker.getBlockState(position),
                            ItemUseInventoryTransaction_PredictedResult.Success
                    )
            );
            transactionPacket.write(inventoryTransactionRewriter.getInventoryTransactionType(), inventoryTransaction);

            transactionPacket.sendToServer(BedrockProtocol.class);

            // Bedrock sends a stop item use on after the transaction packet
            ExperimentalPacketFactory.sendBedrockPlayerAction(
                    wrapper.user(),
                    clientPlayer.runtimeId(),
                    PlayerActionType.StopItemUseOn,
                    position,
                    new BlockPosition(0, 0, 0),
                    0
            );
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.SET_BEACON, null, wrapper -> {
            wrapper.cancel();
            final int primaryPower = wrapper.read(Types.OPTIONAL_VAR_INT);
            final int secondaryPower = wrapper.read(Types.OPTIONAL_VAR_INT);
            // TODO
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.RENAME_ITEM, null, wrapper -> {
            wrapper.cancel();
            InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final String newName = wrapper.read(Types.STRING);

            if (inventoryTracker.isContainerOpen() && inventoryTracker.getCurrentContainer() instanceof AnvilContainer anvilContainer) {
                anvilContainer.setRenameText(newName);
            }
        });

        protocol.registerClientbound(ClientboundBedrockPackets.CRAFTING_DATA, null, wrapper -> {
            wrapper.cancel();
            CraftingDataTracker craftingDataTracker = wrapper.user().get(CraftingDataTracker.class);
            ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);

            List<CraftingDataStorage> recipes = new ArrayList<>();
            final int craftingDataSize = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);
            for (int i = 0; i < craftingDataSize; i++) {
                final RecipeType recipeType = RecipeType.getByValue(wrapper.read(BedrockTypes.VAR_INT));
                switch (recipeType) {
                    case SHAPELESS, USER_DATA_SHAPELESS, SHAPELESS_CHEMISTRY -> {
                        final String recipeId = wrapper.read(BedrockTypes.STRING);
                        final List<ItemDescriptor> ingredients = List.of(wrapper.read(ExperimentalBedrockTypes.ITEM_DESCRIPTORS));
                        final List<BedrockItem> results = List.of(wrapper.read(itemRewriter.itemInstanceArrayType()));
                        final UUID recipeUuid = wrapper.read(BedrockTypes.UUID);
                        final String recipeTag = wrapper.read(BedrockTypes.STRING);
                        final int priority = wrapper.read(BedrockTypes.VAR_INT);

                        // TODO: Sync unlocking recipes
                        if (recipeType == RecipeType.SHAPELESS || recipeType == RecipeType.USER_DATA_SHAPELESS) {
                            final byte unlock = wrapper.read(Types.BYTE);
                            if (unlock == 0) {
                                wrapper.read(ExperimentalBedrockTypes.ITEM_DESCRIPTORS);
                            }
                        }

                        final int netId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);

                        CraftingDataStorage recipe = new CraftingDataStorage(
                                recipeType,
                                netId,
                                new ShapelessRecipe(recipeId, recipeUuid, recipeTag, priority, ingredients, results)
                        );
                        recipes.add(recipe);
                    }
                    case SHAPED, SHAPED_CHEMISTRY -> {
                        final String recipeId = wrapper.read(BedrockTypes.STRING);
                        final int width = wrapper.read(BedrockTypes.VAR_INT);
                        final int height = wrapper.read(BedrockTypes.VAR_INT);
                        final ItemDescriptor[][] ingredients = new ItemDescriptor[height][width];
                        for (int row = 0; row < height; row++) {
                            for (int col = 0; col < width; col++) {
                                ingredients[row][col] = wrapper.read(ExperimentalBedrockTypes.ITEM_DESCRIPTOR_TYPE);
                            }
                        }

                        final List<BedrockItem> results = List.of(wrapper.read(itemRewriter.itemInstanceArrayType()));
                        final UUID recipeUuid = wrapper.read(BedrockTypes.UUID);
                        final String recipeTag = wrapper.read(BedrockTypes.STRING);
                        final int priority = wrapper.read(BedrockTypes.VAR_INT);
                        final boolean assumeSymmetric = wrapper.read(Types.BOOLEAN);

                        // TODO: Sync unlocking recipes
                        if (recipeType == RecipeType.SHAPED) {
                            final byte unlock = wrapper.read(Types.BYTE);
                            if (unlock == 0) {
                                wrapper.read(ExperimentalBedrockTypes.ITEM_DESCRIPTORS);
                            }
                        }

                        final int netId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);

                        CraftingDataStorage recipe = new CraftingDataStorage(
                                recipeType,
                                netId,
                                new ShapedRecipe(recipeId, recipeUuid, recipeTag, priority, ingredients, results, assumeSymmetric)
                        );
                        recipes.add(recipe);
                    }
                    case UNKNOWN_1, UNKNOWN_2 -> { // TODO: What is this for
                        final int itemData = wrapper.read(BedrockTypes.VAR_INT);

                        if (recipeType == RecipeType.UNKNOWN_2) {
                            final int itemAuxData = wrapper.read(BedrockTypes.VAR_INT);
                        }

                        BedrockItem result = wrapper.read(itemRewriter.itemInstanceType());

                        final String recipeTag = wrapper.read(BedrockTypes.STRING);
                    }
                    case MULTI -> { // TODO: What is this for
                        final UUID recipeUuid = wrapper.read(BedrockTypes.UUID);
                        final int netId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);
                    }
                    case SMITHING_TRANSFORM, SMITHING_TRIM -> {
                        final String recipeId = wrapper.read(BedrockTypes.STRING);
                        final ItemDescriptor template = wrapper.read(ExperimentalBedrockTypes.ITEM_DESCRIPTOR_TYPE);
                        final ItemDescriptor base = wrapper.read(ExperimentalBedrockTypes.ITEM_DESCRIPTOR_TYPE);
                        final ItemDescriptor addition = wrapper.read(ExperimentalBedrockTypes.ITEM_DESCRIPTOR_TYPE);
                        BedrockItem result = BedrockItem.empty();
                        if (recipeType == RecipeType.SMITHING_TRANSFORM) {
                            result = wrapper.read(itemRewriter.itemInstanceType());
                        }
                        final String recipeTag = wrapper.read(BedrockTypes.STRING);
                        final int netId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);

                        CraftingDataStorage recipe = new CraftingDataStorage(
                                recipeType,
                                netId,
                                new SmithingRecipe(recipeId, UUID.randomUUID() /*TODO: Could potentially be clashes*/, recipeTag, 0, template, base, addition, result)
                        );
                        recipes.add(recipe);
                    }
                    default -> {
                        ViaBedrock.getPlatform().getLogger().warning("Received unsupported recipe type: " + recipeType);
                    }
                }
            }
            craftingDataTracker.updateCraftingDataList(recipes);

            wrapper.clearPacket();

            // TODO: Potion Mixes

            // TODO: Container Mixes

            // TODO: Material Reducers

            // TODO: Clear recipes
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ITEM_STACK_RESPONSE, null, wrapper -> {
            wrapper.cancel();
            InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            InventoryRequestTracker inventoryRequestTracker = wrapper.user().get(InventoryRequestTracker.class);
            ItemStackResponseInfo[] infoList = wrapper.read(ExperimentalBedrockTypes.ITEM_STACK_RESPONSES);

            // Resync the inventory content based on the response
            for (ItemStackResponseInfo info : infoList) {

                InventoryRequestStorage requestInfo = inventoryRequestTracker.getRequest(info.requestId());
                if (requestInfo == null) {
                    ViaBedrock.getPlatform().getLogger().warning("Received item stack response for unknown request ID: " + info.requestId());
                    continue;
                }
                inventoryRequestTracker.removeRequest(info.requestId());

                if (info.result() != ItemStackNetResult.Success) {
                    ViaBedrock.getPlatform().getLogger().warning("Received unsuccessful item stack response: " + info.result());
                    inventoryTracker.getHudContainer().setItems(requestInfo.prevCursorContainer().getItems().clone());
                    for (Container container : requestInfo.prevContainers()) {
                        Container newContainer = inventoryTracker.getContainerClientbound(container.containerId(), container.getFullContainerName(0), null);
                        newContainer.setItems(container.getItems().clone());
                        PacketFactory.sendJavaContainerSetContent(wrapper.user(), newContainer);  // Resync the container content on Java side
                    }
                    continue;
                }

                //TODO: This is required for crafting so that the cursor item net id is updated properly
                //TODO: Check that the items match the request, if not resync the container (We probably should do this anyway to be safe)
                List<Container> mismatchedContainers = new ArrayList<>();
                for (ItemStackResponseContainerInfo containerInfo : info.containers()) {
                    for (ItemStackResponseSlotInfo slotInfo : containerInfo.slots()) {
                        Container container = inventoryTracker.getContainerFromName(containerInfo.containerName(), slotInfo.slot());
                        if (container == null) {
                            ViaBedrock.getPlatform().getLogger().warning("Received item stack response for unknown container: " + containerInfo.containerName());
                            continue;
                        }

                        int javaSlot = container.javaSlot(slotInfo.slot());

                        if (javaSlot < 0 || javaSlot >= container.getItems().length) {
                            ViaBedrock.getPlatform().getLogger().warning("Received item stack response with out of bounds slot: " + slotInfo.slot());
                            continue;
                        }

                        // Check if the item matches the expected item
                        BedrockItem expectedItem = container.getItem(javaSlot);
                        if (expectedItem.isEmpty()) continue; //TODO
                        if (expectedItem.netId() == null || expectedItem.netId() != slotInfo.itemNetId() || expectedItem.amount() != slotInfo.amount()) {
                            ViaBedrock.getPlatform().getLogger().warning("Received item stack response with mismatch: expected " + expectedItem + " but got itemId=" + slotInfo.itemNetId() + ", amount=" + slotInfo.amount());
                            BedrockItem newItem = expectedItem.copy();
                            newItem.setNetId(slotInfo.itemNetId());
                            newItem.setAmount(slotInfo.amount());
                            container.setItem(javaSlot, newItem);
                            if (container.getFullContainerName(slotInfo.slot()).name() != ContainerEnumName.CursorContainer) {
                                mismatchedContainers.add(container);
                            }
                        }
                        // TODO:  Handle custom name and durability
                    }
                }

                for (Container container : mismatchedContainers) {
                    PacketFactory.sendJavaContainerSetContent(wrapper.user(), container);  // Resync the container content on Java side
                }
                // Force resync cursor
                PacketFactory.sendJavaContainerSetContent(wrapper.user(), inventoryTracker.getInventoryContainer());
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CONTAINER_SET_DATA, ClientboundPackets1_21_11.CONTAINER_SET_DATA, wrapper -> {
            byte containerId = wrapper.read(Types.BYTE);
            int id = wrapper.read(BedrockTypes.VAR_INT);
            int value = wrapper.read(BedrockTypes.VAR_INT);

            Container container = wrapper.user().get(InventoryTracker.class).getContainerClientbound(containerId, null, null);
            if (container == null) {
                // TODO: This throws every time we open a container
                // Unknown container, ignore
                wrapper.cancel();
                ViaBedrock.getPlatform().getLogger().warning("Received ContainerSetData packet for unknown container: containerId=" + containerId + ", id=" + id + ", value=" + value);
                return;
            }
            int windowId = container.javaContainerId();
            if (windowId == -1) {
                // Unknown container, ignore
                wrapper.cancel();
                ViaBedrock.getPlatform().getLogger().warning("Received ContainerSetData packet for unknown container: containerId=" + containerId + ", id=" + id + ", value=" + value);
                return;
            }

            short javaId = container.translateContainerData(id);
            if (javaId == -1) {
                ViaBedrock.getPlatform().getLogger().warning("Received ContainerSetData packet with unknown id: containerId=" + containerId + ", id=" + id + ", value=" + value);
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, windowId);
            wrapper.write(Types.SHORT, javaId);
            wrapper.write(Types.SHORT, (short) value);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_ENCHANT_OPTIONS, ClientboundPackets1_21_11.CONTAINER_SET_DATA, wrapper -> {
            wrapper.cancel();
            // TODO
        });
    }

    public static void registerTasks() {
    }

    public static void registerStorages(final UserConnection user) {
        user.put(new InventoryTransactionRewriter(user));
        user.put(new InventoryRequestTracker(user));
        user.put(new CraftingDataTracker(user));
    }
}
