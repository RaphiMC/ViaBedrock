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
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.experimental.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.experimental.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.experimental.model.inventory.InventorySource;
import net.raphimc.viabedrock.experimental.model.inventory.InventoryTransactionData;
import net.raphimc.viabedrock.experimental.model.map.MapDecoration;
import net.raphimc.viabedrock.experimental.model.map.MapObject;
import net.raphimc.viabedrock.experimental.model.map.MapTrackedObject;
import net.raphimc.viabedrock.experimental.rewriter.InventoryTransactionRewriter;
import net.raphimc.viabedrock.experimental.util.JavaMapPaletteUtil;
import net.raphimc.viabedrock.experimental.storage.MapTracker;
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
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This class is used to register experimental features that are not yet stable/tested enough to be included in the main protocol.
 * These features may be subject to change or removal in future versions.
 */
public class ExperimentalFeatures {

    private static final int MAP_FLAGS_ALL = ClientboundMapItemDataPacket_Type.Creation.getValue() | ClientboundMapItemDataPacket_Type.DecorationUpdate.getValue() | ClientboundMapItemDataPacket_Type.TextureUpdate.getValue();

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

        protocol.registerClientbound(ClientboundBedrockPackets.MAP_ITEM_DATA, ClientboundPackets1_21_11.MAP_ITEM_DATA, wrapper -> {
            MapTracker mapTracker = wrapper.user().get(MapTracker.class);

            final long mapId = wrapper.read(BedrockTypes.VAR_LONG); // map id
            final int typeFlags = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // type flags
            final byte dimension = wrapper.read(Types.BYTE); // dimension
            final boolean locked = wrapper.read(Types.BOOLEAN); // locked
            final BlockPosition origin = wrapper.read(BedrockTypes.BLOCK_POSITION); // origin

            final List<Long> trackedEntities = new ArrayList<>();
            if ((typeFlags & ClientboundMapItemDataPacket_Type.Creation.getValue()) != 0) {
                final int length = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // length
                for (int i = 0; i < length; i++) {
                    trackedEntities.add(wrapper.read(BedrockTypes.VAR_LONG));
                }
            }

            byte scale = 0;
            if ((typeFlags & MAP_FLAGS_ALL) != 0) {
                scale = wrapper.read(Types.BYTE); // scale
            }

            final List<MapDecoration> decorations = new ArrayList<>();
            final List<MapTrackedObject> trackedObjects = new ArrayList<>();
            if ((typeFlags & ClientboundMapItemDataPacket_Type.DecorationUpdate.getValue()) != 0) {
                final int length = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // length
                for (int i = 0; i < length; i++) {
                    MapTrackedObject.Type objectType = MapTrackedObject.Type.values()[wrapper.read(BedrockTypes.INT_LE)]; //TODO: Error logging
                    switch (objectType) {
                        case BLOCK:
                            trackedObjects.add(new MapTrackedObject(wrapper.read(BedrockTypes.BLOCK_POSITION)));
                            break;
                        case ENTITY:
                            trackedObjects.add(new MapTrackedObject(wrapper.read(BedrockTypes.VAR_LONG)));
                            break;
                    }
                }

                final int decorLength = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // length
                for (int i = 0; i < decorLength; i++) {
                    final byte iconType = wrapper.read(Types.BYTE);
                    final byte rotation = wrapper.read(Types.BYTE);
                    final byte x = wrapper.read(Types.BYTE);
                    final byte y = wrapper.read(Types.BYTE);
                    final String name = wrapper.read(BedrockTypes.STRING); // name
                    final int color = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // color

                    decorations.add(new MapDecoration(iconType, rotation, x, y, name, color));
                }
            }

            int width = 0;
            int height = 0;
            int xOffset = 0;
            int yOffset = 0;
            int[] colors = new int[0];
            if ((typeFlags & ClientboundMapItemDataPacket_Type.TextureUpdate.getValue()) != 0) {
                width = wrapper.read(BedrockTypes.VAR_INT); // width
                height = wrapper.read(BedrockTypes.VAR_INT); // height
                xOffset = wrapper.read(BedrockTypes.VAR_INT); // x offset
                yOffset = wrapper.read(BedrockTypes.VAR_INT); // y offset

                final int colorsLength = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // colors length
                colors = new int[colorsLength];
                for (int i = 0; i < colorsLength; i++) {
                    colors[i] = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);
                }
            }

            //TODO: Clean this up
            int nextJavaId = mapTracker.getNextMapId();
            if ((typeFlags & ClientboundMapItemDataPacket_Type.Creation.getValue()) != 0) {
                MapObject existingMap = mapTracker.getMapObjects().get(mapId);
                if (existingMap != null) {
                    existingMap.getTrackedEntities().clear();
                    existingMap.getTrackedEntities().addAll(trackedEntities);
                } else {
                    MapObject mapObject = new MapObject(
                            mapId,
                            dimension,
                            locked,
                            origin,
                            trackedEntities,
                            scale,
                            trackedObjects,
                            decorations,
                            width,
                            height,
                            xOffset,
                            yOffset,
                            colors,
                            nextJavaId
                    );
                    mapTracker.getMapObjects().put(mapId, mapObject);
                }
            }
            if ((typeFlags & ClientboundMapItemDataPacket_Type.DecorationUpdate.getValue()) != 0) {
                MapObject existingMap = mapTracker.getMapObjects().get(mapId);
                if (existingMap != null) {
                    existingMap.getTrackedObjects().clear();
                    existingMap.getTrackedObjects().addAll(trackedObjects);
                    existingMap.getDecorations().clear();
                    existingMap.getDecorations().addAll(decorations);
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received map decoration update for unknown map id: " + mapId);
                    MapObject mapObject = new MapObject(
                            mapId,
                            dimension,
                            locked,
                            origin,
                            trackedEntities,
                            scale,
                            trackedObjects,
                            decorations,
                            0,
                            0,
                            0,
                            0,
                            new int[0],
                            nextJavaId
                    );
                    mapTracker.getMapObjects().put(mapId, mapObject);
                }
            }
            if ((typeFlags & ClientboundMapItemDataPacket_Type.TextureUpdate.getValue()) != 0) {
                MapObject existingMap = mapTracker.getMapObjects().get(mapId);
                if (existingMap != null) {
                    existingMap.setWidth(width);
                    existingMap.setHeight(height);
                    existingMap.setXOffset(xOffset);
                    existingMap.setYOffset(yOffset);
                    existingMap.setColors(colors);
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received map texture update for unknown map id: " + mapId);
                    MapObject mapObject = new MapObject(
                            mapId,
                            dimension,
                            locked,
                            origin,
                            trackedEntities,
                            scale,
                            new ArrayList<>(),
                            new ArrayList<>(),
                            width,
                            height,
                            xOffset,
                            yOffset,
                            colors,
                            nextJavaId
                    );
                    mapTracker.getMapObjects().put(mapId, mapObject);
                }
            }

            MapObject mapObject = mapTracker.getMapObjects().get(mapId);
            if (mapObject == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received map item data for unknown map id: " + mapId);
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, mapObject.getJavaId()); // map id
            wrapper.write(Types.BYTE, mapObject.getScale()); // scale
            wrapper.write(Types.BOOLEAN, mapObject.isLocked()); // locked

            wrapper.write(Types.BOOLEAN, false); // Icons (Prefixed Optional, TODO: Implement)
            wrapper.write(Types.UNSIGNED_BYTE, (short) mapObject.getWidth()); // width
            if (mapObject.getWidth() > 0) {
                wrapper.write(Types.UNSIGNED_BYTE, (short) mapObject.getHeight()); // height
                wrapper.write(Types.BYTE, (byte) mapObject.getXOffset()); // xOffset
                wrapper.write(Types.BYTE, (byte) mapObject.getYOffset()); // yOffset

                wrapper.write(Types.VAR_INT, mapObject.getColors().length);
                for (short color : JavaMapPaletteUtil.convertToJavaPalette(mapObject.getColors())) {
                    wrapper.write(Types.UNSIGNED_BYTE, color);
                }

            } else {
                //ViaBedrock.getPlatform().getLogger().warning("Sent empty map data for map id: " + mapId);
                //TODO: Bedrock requests map data if it doesnt have it, so we need to send something
            }
        });
    }

    public static void registerTasks() {
    }

    public static void registerStorages(final UserConnection user) {
        user.put(new InventoryTransactionRewriter(user));
        user.put(new MapTracker(user));
    }
}
