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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.inventory.InventoryOperation;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.ContainerSlot;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackNetResult;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequest;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackResponse;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.array.ArrayType;
import net.raphimc.viabedrock.protocol.types.model.ItemStackRequestType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Handles the server authoritative inventory protocol (ItemStackRequest / ItemStackResponse).
 *
 * <p>Item stack responses only contain the resulting stack sizes and net ids, not the resulting items themselves, so
 * the outcome of every request has to be predicted locally and is only corrected once the server answers.</p>
 */
public class ItemStackRequestTracker extends StoredObject {

    /**
     * The maximum amount of requests which are kept around waiting for an answer before old ones are dropped.
     */
    private static final int MAX_PENDING_REQUESTS = 64;

    private final Map<Integer, Set<Container>> pendingRequests = new LinkedHashMap<>();
    private final Type<ItemStackRequest[]> itemStackRequestArrayType;
    private int nextRequestId = -1;

    public ItemStackRequestTracker(final UserConnection user) {
        super(user);

        this.itemStackRequestArrayType = new ArrayType<>(new ItemStackRequestType(user), BedrockTypes.UNSIGNED_VAR_INT);
    }

    /**
     * Translates the given operations into an item stack request and sends it to the server.
     *
     * <p>This has to be called before the predicted result is applied to the containers, because the request refers to
     * the items by the net ids they currently have.</p>
     *
     * @param operations The operations to send
     * @return The request id or 0 if nothing was sent
     */
    public int sendRequest(final List<InventoryOperation> operations) {
        if (operations.isEmpty()) {
            return 0;
        }

        final Set<Container> affectedContainers = new HashSet<>();
        final List<ItemStackRequestAction> actions = this.toActions(operations, affectedContainers, false);
        return this.sendRequest(actions, affectedContainers);
    }

    /**
     * Translates the given operations into item stack request actions.
     *
     * @param affectedContainers Is filled with the containers the operations touch
     * @param crafting           Whether these operations are part of taking an item out of a result slot. Items which
     *                           disappear are then consumed by the recipe instead of being destroyed.
     * @return The actions
     */
    public List<ItemStackRequestAction> toActions(final List<InventoryOperation> operations, final Set<Container> affectedContainers, final boolean crafting) {
        final List<ItemStackRequestAction> actions = new ArrayList<>(operations.size());
        for (InventoryOperation operation : operations) {
            if (operation instanceof InventoryOperation.Transfer transfer) {
                final boolean toCursor = this.isCursor(transfer.destination());
                if (toCursor) {
                    actions.add(new ItemStackRequestAction.Take(transfer.count(), transfer.source().requestSlotInfo(), transfer.destination().requestSlotInfo()));
                } else {
                    actions.add(new ItemStackRequestAction.Place(transfer.count(), transfer.source().requestSlotInfo(), transfer.destination().requestSlotInfo()));
                }
                affectedContainers.add(transfer.source().container());
                affectedContainers.add(transfer.destination().container());
            } else if (operation instanceof InventoryOperation.Swap swap) {
                actions.add(new ItemStackRequestAction.Swap(swap.slot1().requestSlotInfo(), swap.slot2().requestSlotInfo()));
                affectedContainers.add(swap.slot1().container());
                affectedContainers.add(swap.slot2().container());
            } else if (operation instanceof InventoryOperation.Drop drop) {
                actions.add(new ItemStackRequestAction.Drop(drop.count(), drop.source().requestSlotInfo(), false));
                affectedContainers.add(drop.source().container());
            } else if (operation instanceof InventoryOperation.Destroy destroy) {
                if (crafting) {
                    actions.add(new ItemStackRequestAction.Consume(destroy.count(), destroy.source().requestSlotInfo()));
                } else {
                    actions.add(new ItemStackRequestAction.Destroy(destroy.count(), destroy.source().requestSlotInfo()));
                }
                affectedContainers.add(destroy.source().container());
            } else {
                throw new IllegalArgumentException("Unhandled inventory operation: " + operation.getClass().getSimpleName());
            }
        }

        return actions;
    }

    /**
     * Sends the given actions as a single item stack request to the server.
     *
     * @param actions            The actions to send
     * @param affectedContainers The containers which have to be resynced if the server rejects the request
     * @return The request id
     */
    public int sendRequest(final List<ItemStackRequestAction> actions, final Set<Container> affectedContainers) {
        return this.sendRequest(actions, affectedContainers, new String[0]);
    }

    /**
     * @param filterStrings Texts the player entered which the server has to check for profanity, like an anvil name
     */
    public int sendRequest(final List<ItemStackRequestAction> actions, final Set<Container> affectedContainers, final String[] filterStrings) {
        final int requestId = this.nextRequestId;
        // Client generated request ids are negative and odd
        this.nextRequestId -= 2;
        if (this.nextRequestId > 0) { // Wrapped around
            this.nextRequestId = -1;
        }

        while (this.pendingRequests.size() >= MAX_PENDING_REQUESTS) {
            this.pendingRequests.remove(this.pendingRequests.keySet().iterator().next());
        }
        this.pendingRequests.put(requestId, affectedContainers);

        final PacketWrapper itemStackRequest = PacketWrapper.create(ServerboundBedrockPackets.ITEM_STACK_REQUEST, this.user());
        itemStackRequest.write(this.itemStackRequestArrayType, new ItemStackRequest[]{new ItemStackRequest(requestId, actions, filterStrings, 0)}); // requests
        itemStackRequest.sendToServer(BedrockProtocol.class);
        return requestId;
    }

    public void handleResponse(final ItemStackResponse response) {
        final Set<Container> affectedContainers = this.pendingRequests.remove(response.requestId());

        if (response.result() != ItemStackNetResult.Success) {
            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Server rejected item stack request " + response.requestId() + ": " + response.result());
            // The server doesn't tell us what the correct state is, so the best we can do is to resync the Java client
            // with what we know. Servers usually follow up with the corrected inventory content.
            this.resync(affectedContainers);
            return;
        }

        final InventoryTracker inventoryTracker = this.user().get(InventoryTracker.class);
        final Set<Container> changedContainers = new HashSet<>();
        for (ItemStackResponse.ContainerInfo containerInfo : response.containerInfos()) {
            for (ItemStackResponse.SlotInfo slotInfo : containerInfo.slots()) {
                final ContainerSlot containerSlot = inventoryTracker.resolveBedrockSlot(containerInfo.containerName(), slotInfo.slot());
                if (containerSlot == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received item stack response for unknown slot: " + containerInfo.containerName().name() + "[" + slotInfo.slot() + "]");
                    continue;
                }

                final BedrockItem currentItem = containerSlot.getItem();
                final BedrockItem newItem;
                if (slotInfo.count() <= 0) {
                    newItem = BedrockItem.empty();
                } else {
                    newItem = currentItem.copy();
                    newItem.setAmount(slotInfo.count());
                    newItem.setNetId(slotInfo.stackNetworkId());
                    if (slotInfo.durabilityCorrection() != 0) {
                        if (newItem.tag() == null) {
                            newItem.setTag(new CompoundTag());
                        }
                        newItem.tag().putInt("Damage", slotInfo.durabilityCorrection());
                    }
                }

                if (newItem.isDifferent(currentItem) || newItem.amount() != currentItem.amount()) {
                    changedContainers.add(containerSlot.container());
                }
                containerSlot.setItem(newItem);
            }
        }

        this.resync(changedContainers);
    }

    private void resync(final Set<Container> containers) {
        if (containers == null || containers.isEmpty()) {
            return;
        }
        for (Container container : containers) {
            PacketFactory.sendJavaContainerSetContent(this.user(), container);
        }
    }

    private boolean isCursor(final ContainerSlot containerSlot) {
        return containerSlot.equals(this.user().get(InventoryTracker.class).getCursorSlot());
    }

    public void clearPendingRequests() {
        this.pendingRequests.clear();
    }

    public Set<Container> getPendingContainers(final int requestId) {
        return this.pendingRequests.getOrDefault(requestId, Collections.emptySet());
    }

}
