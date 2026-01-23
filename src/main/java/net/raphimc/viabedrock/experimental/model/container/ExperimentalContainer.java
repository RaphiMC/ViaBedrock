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
package net.raphimc.viabedrock.experimental.model.container;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.experimental.ExperimentalPacketFactory;
import net.raphimc.viabedrock.experimental.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestInfo;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestSlotInfo;
import net.raphimc.viabedrock.experimental.storage.ExperimentalInventoryTracker;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestStorage;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestTracker;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.TextProcessingEventOrigin;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.ClickType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public abstract class ExperimentalContainer {

    protected final UserConnection user;
    protected final byte containerId;
    protected final ContainerType type;
    protected final TextComponent title;
    protected final BlockPosition position;
    protected final BedrockItem[] items;
    protected final Set<String> validBlockTags;

    public ExperimentalContainer(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final int size, final String... validBlockTags) {
        this.user = user;
        this.containerId = containerId;
        this.type = type;
        this.title = title;
        this.position = position;
        this.items = BedrockItem.emptyArray(size);
        this.validBlockTags = Set.of(validBlockTags);
    }

    protected ExperimentalContainer(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final BedrockItem[] items, final Set<String> validBlockTags) {
        this.user = user;
        this.containerId = containerId;
        this.type = type;
        this.title = title;
        this.position = position;
        this.items = items;
        this.validBlockTags = validBlockTags;
    }

    public abstract FullContainerName getFullContainerName(int slot);

    public boolean handleClick(final int revision, final short javaSlot, final byte button, final ClickType action) {
        ExperimentalContainer container = this;
        ExperimentalInventoryTracker inventoryTracker = user.get(ExperimentalInventoryTracker.class);
        InventoryRequestTracker inventoryRequestTracker = user.get(InventoryRequestTracker.class);
        int bedrockSlot = container.bedrockSlot(javaSlot);

        /* TODO: Could potentially lead to a race condition if we receive a inventory update before the response for the request,
         *  a better solution would be to store the specific changes made in the request. From my testing this doesnt seem to happen though
         */
        List<ExperimentalContainer> prevContainers = new ArrayList<>(); // Store previous state of all involved containers to be able to rollback if needed
        prevContainers.add(container.copy()); // Store previous state of the container

        ExperimentalContainer prevCursorContainer = inventoryTracker.getHudContainer().copy(); // Store previous state of the cursor item

        ItemStackRequestAction itemAction = switch (action) {
            case PICKUP -> {
                BedrockItem cursorItem = inventoryTracker.getHudContainer().getItem(0);

                if (javaSlot == -999) {
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
                } else if (!(container instanceof InventoryContainer) && (javaSlot < 0 || javaSlot >= container.getItems().length)) {
                    ExperimentalContainer inventoryContainer = inventoryTracker.getInventoryContainer();
                    int invSlot = inventoryContainer.bedrockSlot(javaSlot - container.getItems().length + 9); // Map to inventory slot
                    if (invSlot < 0 || invSlot >= inventoryContainer.getItems().length) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to handle click for " + container.type() + ", but slot was out of bounds (" + javaSlot + ")");
                        yield null;
                    } else {
                        bedrockSlot = invSlot;
                        container = inventoryContainer;

                        prevContainers.add(container.copy()); // Store previous state of the inventory container
                    }
                }

                BedrockItem item = container.getItem(bedrockSlot);
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
                    container.setItem(bedrockSlot, finalContainerItem);

                    yield new ItemStackRequestAction.TakeAction(
                            amountToTake,
                            new ItemStackRequestSlotInfo(container.getFullContainerName(bedrockSlot), (byte) bedrockSlot, finalCursorItem.netId()),
                            new ItemStackRequestSlotInfo(inventoryTracker.getHudContainer().getFullContainerName(0), (byte) 0, 0)
                    );
                } else {
                    if (item.isEmpty() || (!item.isDifferent(cursorItem) && item.amount() < 64)) { // TODO: Mostly accounts for stackability but not fully (shouldnt be an issue with server side inventory)
                        // Place item
                        // TODO: Broken
                        int amt = button == 0 ? cursorItem.amount() : 1;
                        int amountToPlace = item.isDifferent(cursorItem) ? amt : Math.min(64 - item.amount(), cursorItem.amount());

                        BedrockItem finalContainerItem = item.copy();
                        if (item.isDifferent(cursorItem)) {
                            finalContainerItem = cursorItem.copy();
                            finalContainerItem.setAmount(amountToPlace);
                        } else {
                            finalContainerItem.setAmount(item.amount() + amountToPlace);
                        }
                        container.setItem(bedrockSlot, finalContainerItem);

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
                                new ItemStackRequestSlotInfo(container.getFullContainerName(bedrockSlot), (byte) bedrockSlot, 0)
                        );
                    } else {
                        // Swap item

                        BedrockItem cursorCopy = cursorItem.copy();
                        BedrockItem itemCopy = item.copy();

                        container.setItem(bedrockSlot, cursorCopy);
                        inventoryTracker.getHudContainer().setItem(0, itemCopy);

                        yield new ItemStackRequestAction.SwapAction(
                                new ItemStackRequestSlotInfo(inventoryTracker.getHudContainer().getFullContainerName(0), (byte) 0, cursorItem.netId()),
                                new ItemStackRequestSlotInfo(container.getFullContainerName(bedrockSlot), (byte) bedrockSlot, item.netId())
                        );
                    }
                }
            }
            case SWAP -> {
                if (button < 0 || button > 8) {
                    // TODO: Handle offhand
                    yield null;
                }

                ExperimentalContainer hotbarContainer = inventoryTracker.getInventoryContainer();

                if (javaSlot < 0 || javaSlot >= container.getItems().length) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to handle swap for " + container.type() + ", but slot was out of bounds (" + javaSlot + ")");
                    yield null;
                }

                BedrockItem item = container.getItem(bedrockSlot).copy();
                BedrockItem hotbarItem = hotbarContainer.getItem(button).copy();

                if (item.isEmpty() && hotbarItem.isEmpty()) {
                    yield null;
                }

                container.setItem(bedrockSlot, hotbarItem);
                hotbarContainer.setItem(button, item);

                if (hotbarItem.isEmpty()) {
                    yield new ItemStackRequestAction.PlaceAction(
                            item.amount(),
                            new ItemStackRequestSlotInfo(container.getFullContainerName(bedrockSlot), (byte) bedrockSlot, item.netId()),
                            new ItemStackRequestSlotInfo(hotbarContainer.getFullContainerName(button), button, 0)
                    );
                } else if (item.isEmpty()) {
                    yield new ItemStackRequestAction.PlaceAction(
                            hotbarItem.amount(),
                            new ItemStackRequestSlotInfo(hotbarContainer.getFullContainerName(button), button, hotbarItem.netId()),
                            new ItemStackRequestSlotInfo(container.getFullContainerName(bedrockSlot), (byte) bedrockSlot, 0)
                    );
                } else {
                    yield new ItemStackRequestAction.SwapAction(
                            new ItemStackRequestSlotInfo(hotbarContainer.getFullContainerName(button), (byte) button, hotbarItem.netId()),
                            new ItemStackRequestSlotInfo(container.getFullContainerName(bedrockSlot), (byte) bedrockSlot, item.netId())
                    );
                }
            }
            case QUICK_MOVE -> {
                if (true) yield null; // Disable for now
                // TODO: Broken
                // TODO: Inventory -> Hotbar/Armor/Offhand
                // TODO: Container Limited Slots (e.g. Furnace Fuel/Input/Output) Note: this might not be needed as the server will reject invalid moves anyway
                ExperimentalContainer inventoryContainer = inventoryTracker.getInventoryContainer();
                prevContainers.add(inventoryContainer.copy()); // Store previous state of the inventory container
                if (javaSlot < 0 || javaSlot >= container.getItems().length) {
                    int invSlot = inventoryContainer.bedrockSlot(javaSlot - container.getItems().length + 9); // Map to inventory slot
                    if (invSlot < 0 || invSlot >= inventoryContainer.getItems().length) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to handle quick move for " + container.type() + ", but slot was out of bounds (" + javaSlot + ")");
                        yield null;
                    } else {
                        // Moving from inventory to container

                        BedrockItem item = inventoryContainer.getItem(invSlot).copy();
                        if (item.isEmpty()) {
                            yield null;
                        }

                        // Try to move the item to the container
                        int remaining = item.amount();
                        int emptySlot = -1;
                        // TODO: Prioritize stacking over empty slots
                        // TODO: Some containers start at 1 (e.g. Crafting table)
                        for (int i = 0; i < container.getItems().length; i++) {
                            int b = container.bedrockSlot(i);
                            BedrockItem containerItem = container.getItem(i);
                            if (containerItem.isEmpty()) {
                                // Empty slot, move item here
                                BedrockItem toMove = item.copy();
                                toMove.setAmount(remaining);
                                container.setItem(b, toMove);
                                emptySlot = b;
                                remaining = 0;
                                break;
                            } else if (!containerItem.isDifferent(item) && containerItem.amount() < 64) {
                                // TODO: This needs proper implementation
                                // Same item, try to stack
                                int space = 64 - containerItem.amount();
                                int toTransfer = Math.min(space, remaining);
                                containerItem.setAmount(containerItem.amount() + toTransfer);
                                container.setItem(b, containerItem);
                                remaining -= toTransfer;
                                if (remaining <= 0) {
                                    break;
                                } else {
                                    // Add a new Place Action to the list for the partial transfer
                                }
                            }
                        }

                        BedrockItem finalInventoryItem = item.copy();
                        if (remaining <= 0) {
                            finalInventoryItem = BedrockItem.empty();
                        } else {
                            finalInventoryItem.setAmount(remaining);
                        }
                        inventoryContainer.setItem(invSlot, finalInventoryItem);
                        yield new ItemStackRequestAction.PlaceAction(
                                item.amount() - remaining,
                                new ItemStackRequestSlotInfo(inventoryContainer.getFullContainerName(invSlot), (byte) invSlot, item.netId()),
                                new ItemStackRequestSlotInfo(container.getFullContainerName(emptySlot), (byte) emptySlot, 0)
                        );
                    }
                } else {
                    // Moving from container to inventory
                    BedrockItem item = container.getItem(bedrockSlot);

                    if (item.isEmpty()) {
                        yield null;
                    }

                    // Try to move the item to the inventory
                    // TODO: Check hotbar first
                    // TODO: Prioritize stacking over empty slots
                    int remaining = item.amount();
                    int emptySlot = -1;
                    for (int i = 0; i < inventoryContainer.getItems().length; i++) {
                        BedrockItem invItem = inventoryContainer.getItem(i);
                        if (invItem.isEmpty()) {
                            // Empty slot, move item here
                            BedrockItem toMove = item.copy();
                            toMove.setAmount(remaining);
                            inventoryContainer.setItem(i, toMove);
                            emptySlot = i;
                            remaining = 0;
                            break;
                        } else if (!invItem.isDifferent(item) && invItem.amount() < 64) {
                            // TODO: This needs proper implementation
                            // Same item, try to stack
                            int space = 64 - invItem.amount();
                            int toTransfer = Math.min(space, remaining);
                            invItem.setAmount(invItem.amount() + toTransfer);
                            inventoryContainer.setItem(i, invItem);
                            remaining -= toTransfer;
                            if (remaining <= 0) {
                                break;
                            } else {
                                // Add a new Place Action to the list for the partial transfer
                            }
                        }
                    }

                    BedrockItem finalContainerItem = item.copy();
                    if (remaining <= 0) {
                        finalContainerItem = BedrockItem.empty();
                    } else {
                        finalContainerItem.setAmount(remaining);
                    }
                    container.setItem(bedrockSlot, finalContainerItem);
                    yield new ItemStackRequestAction.PlaceAction(
                            item.amount() - remaining,
                            new ItemStackRequestSlotInfo(container.getFullContainerName(bedrockSlot), (byte) bedrockSlot, item.netId()),
                            new ItemStackRequestSlotInfo(inventoryContainer.getFullContainerName(emptySlot), (byte) emptySlot, 0)
                    );
                }
            }
            case THROW -> {
                if (javaSlot < 0 || javaSlot >= container.getItems().length) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to handle throw for " + container.type() + ", but slot was out of bounds (" + javaSlot + ")");
                    yield null;
                }

                BedrockItem item = container.getItem(bedrockSlot);

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
                container.setItem(bedrockSlot, finalContainerItem);

                yield new ItemStackRequestAction.DropAction(
                        amountToDrop,
                        new ItemStackRequestSlotInfo(container.getFullContainerName(bedrockSlot), (byte) bedrockSlot, item.netId()),
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

    public boolean handleButtonClick(final int button) {
        return false;
    }

    public void clearItems() {
        for (int i = 0; i < this.items.length; i++) {
            this.items[i] = BedrockItem.empty();
        }
    }

    public Item getJavaItem(final int slot) {
        return this.user.get(ItemRewriter.class).javaItem(this.getItem(slot));
    }

    public Item[] getJavaItems() {
        return this.user.get(ItemRewriter.class).javaItems(this.items);
    }

    public BedrockItem getItem(final int bedrockSlot) {
        return this.items[bedrockSlot];
    }

    public BedrockItem[] getItems() {
        return Arrays.copyOf(this.items, this.items.length);
    }

    public boolean setItem(final int bedrockSlot, final BedrockItem item) {
        if (bedrockSlot < 0 || bedrockSlot >= this.items.length) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to set item for " + this.type + ", but slot was out of bounds (" + bedrockSlot + ")");
            return false;
        }

        final BedrockItem oldItem = this.items[bedrockSlot];
        this.items[bedrockSlot] = item;
        this.onSlotChanged(bedrockSlot, oldItem, item);
        return true;
    }

    public boolean setItems(final BedrockItem[] items) {
        if (items.length != this.items.length) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to set items for " + this.type + ", but items array length was not correct (" + items.length + " != " + this.items.length + ")");
            return false;
        }

        for (int i = 0; i < items.length; i++) {
            this.setItem(i, items[i]);
        }
        return true;
    }

    public int javaSlot(final int bedrockSlot) {
        return bedrockSlot;
    }

    public int bedrockSlot(final int javaSlot) {
        return javaSlot;
    }

    public byte javaContainerId() {
        return this.containerId();
    }

    public int size() {
        return this.items.length;
    }

    public byte containerId() {
        return this.containerId;
    }

    public ContainerType type() {
        return this.type;
    }

    public TextComponent title() {
        return this.title;
    }

    public BlockPosition position() {
        return this.position;
    }

    public boolean isValidBlockTag(final String tag) {
        if (tag == null) {
            return false;
        } else {
            return this.validBlockTags.contains(tag);
        }
    }

    protected void onSlotChanged(final int javaSlot, final BedrockItem oldItem, final BedrockItem newItem) {
    }

    public ExperimentalContainer copy() { // TODO: This probably isnt the best way to do this
        BedrockItem[] itemsCopy = Arrays.copyOf(this.items, this.items.length);
        return new ExperimentalContainer(this.user, this.containerId, this.type, this.title, this.position, itemsCopy, this.validBlockTags) {
            @Override
            public FullContainerName getFullContainerName(int slot) {
                return ExperimentalContainer.this.getFullContainerName(slot);
            }
        };
    }

    public short translateContainerData(int containerData) {
        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "translateContainerData not implemented for container type: " + this.type);
        return -1;
    }
}
