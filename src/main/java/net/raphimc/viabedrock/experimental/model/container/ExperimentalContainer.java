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
package net.raphimc.viabedrock.experimental.model.container;

import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.experimental.ExperimentalPacketFactory;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestInfo;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestSlotInfo;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestStorage;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestTracker;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.TextProcessingEventOrigin;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.ClickType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ExperimentalContainer {

    public static boolean handleClick(final UserConnection user, Container container, final int revision, final short javaSlot, final byte button, final ClickType action) {
        InventoryTracker inventoryTracker = user.get(InventoryTracker.class);
        InventoryRequestTracker inventoryRequestTracker = user.get(InventoryRequestTracker.class);
        int slot = container.bedrockSlot(javaSlot);

        /* TODO: Could potentially lead to a race condition if we receive a inventory update before the response for the request,
         *  a better solution would be to store the specific changes made in the request. From my testing this doesnt seem to happen though
        */
        List<Container> prevContainers = new ArrayList<>(); // Store previous state of all involved containers to be able to rollback if needed
        prevContainers.add(container.copy()); // Store previous state of the container
        // TODO: because bedrock is cringe, when doing shift clicks we need to add the container we are moving items to as well (e.g. Armour container)

        Container prevCursorContainer = inventoryTracker.getHudContainer().copy(); // Store previous state of the cursor item

        ItemStackRequestAction itemAction = switch (action) {
            case PICKUP -> {
                BedrockItem cursorItem = inventoryTracker.getHudContainer().getItem(0);

                if (slot == -999) {
                    // Drop item
                    if (cursorItem.isEmpty()) {
                        yield null;
                    }

                    int amountToDrop = button == 0 ? cursorItem.amount() : 1;

                    BedrockItem finalCursorItem = cursorItem.copy();
                    if (amountToDrop >= cursorItem.amount()) {
                        finalCursorItem = BedrockItem.empty();
                    } else {
                        finalCursorItem.setAmount(cursorItem.amount() - amountToDrop);
                    }
                    inventoryTracker.getHudContainer().setItem(0, finalCursorItem);

                    yield new ItemStackRequestAction.DropAction(
                            amountToDrop,
                            new ItemStackRequestSlotInfo(inventoryTracker.getHudContainer().getFullContainerName(0), (byte) 0, cursorItem.netId()),
                            false
                    );
                } else if (slot < 0 || slot >= container.getItems().length) {
                    Container inventoryContainer = inventoryTracker.getInventoryContainer();
                    int invSlot = inventoryContainer.bedrockSlot(javaSlot - container.getItems().length + 9); // Map to inventory slot
                    if (invSlot < 0 || invSlot >= inventoryContainer.getItems().length) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to handle click for " + container.type() + ", but slot was out of bounds (" + slot + ")");
                        yield null;
                    } else {
                        slot = invSlot;
                        container = inventoryContainer;

                        prevContainers.add(container.copy()); // Store previous state of the inventory container
                    }
                }

                BedrockItem item = container.getItem(slot);
                if (item.isEmpty() && cursorItem.isEmpty()) {
                    // Nothing to do
                    yield null;
                }

                if (cursorItem.isEmpty()) {
                    // Take item
                    int amountToTake = button == 0 ? item.amount() : (item.amount() + 1) / 2;

                    BedrockItem finalCursorItem = item.copy();
                    finalCursorItem.setAmount(amountToTake);
                    inventoryTracker.getHudContainer().setItem(0, finalCursorItem);
                    BedrockItem finalContainerItem = item.copy();
                    if (amountToTake >= item.amount()) {
                        finalContainerItem = BedrockItem.empty();
                    } else {
                        finalContainerItem.setAmount(item.amount() - amountToTake);
                    }
                    container.setItem(slot, finalContainerItem);

                    yield new ItemStackRequestAction.TakeAction(
                            amountToTake,
                            new ItemStackRequestSlotInfo(container.getFullContainerName(slot), (byte) slot, finalCursorItem.netId()),
                            new ItemStackRequestSlotInfo(inventoryTracker.getHudContainer().getFullContainerName(0), (byte) 0, 0)
                    );
                } else {
                    if (item.isEmpty() || (!item.isDifferent(cursorItem) && item.amount() < 64)) { // TODO: Mostly accounts for stackability but not fully (shouldnt be an issue with server side inventory)
                        // Place item
                        int amt = button == 0 ? cursorItem.amount() : 1;
                        int amountToPlace = item.isDifferent(cursorItem) ? amt : Math.min(64 - item.amount(), cursorItem.amount());

                        BedrockItem finalContainerItem = item.copy();
                        if (item.isDifferent(cursorItem)) {
                            finalContainerItem = cursorItem.copy();
                            finalContainerItem.setAmount(amountToPlace);
                        } else {
                            finalContainerItem.setAmount(item.amount() + amountToPlace);
                        }
                        container.setItem(slot, finalContainerItem);

                        BedrockItem finalCursorItem = cursorItem.copy();
                        if (amountToPlace >= cursorItem.amount()) {
                            finalCursorItem = BedrockItem.empty();
                        } else {
                            finalCursorItem.setAmount(cursorItem.amount() - amountToPlace);
                        }
                        inventoryTracker.getHudContainer().setItem(0, finalCursorItem);

                        yield new ItemStackRequestAction.PlaceAction(
                                amountToPlace,
                                new ItemStackRequestSlotInfo(inventoryTracker.getHudContainer().getFullContainerName(0), (byte) 0, finalContainerItem.netId()),
                                new ItemStackRequestSlotInfo(container.getFullContainerName(slot), (byte) slot, 0)
                        );
                    } else {
                        // Swap item

                        BedrockItem cursorCopy = cursorItem.copy();
                        BedrockItem itemCopy = item.copy();

                        container.setItem(slot, cursorCopy);
                        inventoryTracker.getHudContainer().setItem(0, itemCopy);

                        yield new ItemStackRequestAction.SwapAction(
                                new ItemStackRequestSlotInfo(inventoryTracker.getHudContainer().getFullContainerName(0), (byte) 0, cursorItem.netId()),
                                new ItemStackRequestSlotInfo(container.getFullContainerName(slot), (byte) slot, item.netId())
                        );
                    }
                }
            }
            case SWAP -> {
                if (button < 0 || button > 8) {
                    // TODO: Handle offhand
                    yield null;
                }

                Container hotbarContainer = inventoryTracker.getInventoryContainer();

                if (slot < 0 || slot >= container.getItems().length) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to handle swap for " + container.type() + ", but slot was out of bounds (" + slot + ")");
                    yield null;
                }

                BedrockItem item = container.getItem(slot).copy();
                BedrockItem hotbarItem = hotbarContainer.getItem(button).copy();

                if (item.isEmpty() && hotbarItem.isEmpty()) {
                    yield null;
                }

                container.setItem(slot, hotbarItem);
                hotbarContainer.setItem(button, item);

                if (hotbarItem.isEmpty()) {
                    yield new ItemStackRequestAction.PlaceAction(
                            item.amount(),
                            new ItemStackRequestSlotInfo(container.getFullContainerName(slot), (byte) slot, item.netId()),
                            new ItemStackRequestSlotInfo(hotbarContainer.getFullContainerName(button), button, 0)
                    );
                } else if (item.isEmpty()) {
                    yield new ItemStackRequestAction.PlaceAction(
                            hotbarItem.amount(),
                            new ItemStackRequestSlotInfo(hotbarContainer.getFullContainerName(button), button, hotbarItem.netId()),
                            new ItemStackRequestSlotInfo(container.getFullContainerName(slot), (byte) slot, 0)
                    );
                } else {
                    yield new ItemStackRequestAction.SwapAction(
                            new ItemStackRequestSlotInfo(hotbarContainer.getFullContainerName(button), (byte) button, hotbarItem.netId()),
                            new ItemStackRequestSlotInfo(container.getFullContainerName(slot), (byte) slot, item.netId())
                    );
                }
            }
            case THROW -> {
                if (slot < 0 || slot >= container.getItems().length) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to handle throw for " + container.type() + ", but slot was out of bounds (" + slot + ")");
                    yield null;
                }

                BedrockItem item = container.getItem(slot);

                if (item.isEmpty()) {
                    yield null;
                }

                int amountToDrop = button == 0 ? 1 : item.amount();

                BedrockItem finalContainerItem = item.copy();
                if (amountToDrop >= item.amount()) {
                    finalContainerItem = BedrockItem.empty();
                } else {
                    finalContainerItem.setAmount(item.amount() - amountToDrop);
                }
                container.setItem(slot, finalContainerItem);

                yield new ItemStackRequestAction.DropAction(
                        amountToDrop,
                        new ItemStackRequestSlotInfo(container.getFullContainerName(slot), (byte) slot, item.netId()),
                        false
                );
            }
            default -> null;
        };

        if (itemAction == null) {
            return false;
        }

        ItemStackRequestInfo request = new ItemStackRequestInfo(
                inventoryRequestTracker.nextRequestId(),
                List.of(
                        itemAction
                ),
                List.of(),
                TextProcessingEventOrigin.unknown
        );

        inventoryRequestTracker.addRequest(new InventoryRequestStorage(request, revision, prevCursorContainer, prevContainers)); // Store the request to track it later
        ExperimentalPacketFactory.sendBedrockInventoryRequest(user, new ItemStackRequestInfo[] {request});

        return true;
    }

}
