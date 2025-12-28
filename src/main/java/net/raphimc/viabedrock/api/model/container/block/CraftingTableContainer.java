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
package net.raphimc.viabedrock.api.model.container.block;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.experimental.ExperimentalPacketFactory;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestInfo;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestSlotInfo;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestStorage;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestTracker;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.TextProcessingEventOrigin;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.ClickType;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CraftingTableContainer extends Container {

    public CraftingTableContainer(UserConnection user, byte containerId, TextComponent title, BlockPosition position) {
        super(user, containerId, ContainerType.WORKBENCH, title, position, 10, "crafting_table");
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        return switch (slot) {
            case 32, 33, 34, 35, 36, 37, 38, 39, 40 -> new FullContainerName(ContainerEnumName.CraftingInputContainer, null);
            case 50 -> new FullContainerName(ContainerEnumName.CreatedOutputContainer, null);
            default -> {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Invalid slot " + slot + " in CraftingTableContainer");
                yield FullContainerName.EMPTY;
            }
        };
    }

    @Override
    public int javaSlot(final int slot) {
        return switch (slot) {
            case 32, 33, 34, 35, 36, 37, 38, 39, 40 -> slot - 31;
            case 50 -> 0;
            default -> super.javaSlot(slot);
        };
    }

    @Override
    public int bedrockSlot(final int slot) {
        return switch (slot) {
            case 1, 2, 3, 4, 5, 6, 7, 8, 9 -> slot + 31;
            case 0 -> 50;
            default -> super.bedrockSlot(slot);
        };
    }

    /*@Override
    public boolean handleClick(final int revision, final short javaSlot, final byte button, final ClickType action) {
        if (javaSlot == 0) {
            if (ViaBedrock.getConfig().shouldEnableExperimentalFeatures()) {
                //TODO: This is experimental code...
                InventoryTracker inventoryTracker = user.get(InventoryTracker.class);
                InventoryRequestTracker inventoryRequestTracker = user.get(InventoryRequestTracker.class);

                List<Container> prevContainers = new ArrayList<>();
                prevContainers.add(this.copy());
                prevContainers.add(inventoryTracker.getInventoryContainer().copy());
                Container prevCursorContainer = inventoryTracker.getHudContainer().copy();

                int craftableAmount = this.getCraftableAmount(javaSlot);
                if (craftableAmount <= 0) {
                    return false;
                }
                craftableAmount = button == 0 ? 1 : craftableAmount; // Left click = 1, right click = max craftable

                List<ItemStackRequestAction> actions = new ArrayList<ItemStackRequestAction>(
                        new ItemStackRequestAction.CraftRecipeAction(this.getRecipeNetworkId(javaSlot), craftableAmount)
                        //new ItemStackRequestAction.CraftResultsDeprecatedAction(this.getResultItems(javaSlot, craftableAmount)) TODO: Deprecated action, hopefully removed in the future
                );

                for (int i = 0; i < 8; i++) {
                    int inputSlot = 34 + i;
                    int available = this.getItem(inputSlot).amount();
                    if (available > 0) {
                        int toConsume = Math.min(available, craftableAmount);
                        actions.add(new ItemStackRequestAction.ConsumeAction(
                                toConsume,
                                new ItemStackRequestSlotInfo(
                                        this.getFullContainerName(inputSlot),
                                        (byte) inputSlot,
                                        this.getItem(inputSlot).netId()
                                )
                        ));
                    }
                }

                actions.add(
                        new ItemStackRequestAction.TakeAction(
                                craftableAmount * this.getResultItems(javaSlot, 1).get(0).amount(), // Total amount to take
                                new ItemStackRequestSlotInfo(
                                        this.getFullContainerName(javaSlot),
                                        (byte) javaSlot,
                                        this.getResultItems(javaSlot, 1).get(0).netId()
                                ),
                                new ItemStackRequestSlotInfo(
                                        new FullContainerName(ContainerEnumName.CursorContainer, null),
                                        (byte) 0,
                                        0 // The stackNetworkId is not known yet
                                )
                        )
                );

                ItemStackRequestInfo request = new ItemStackRequestInfo(
                        inventoryRequestTracker.nextRequestId(),
                        actions,
                        List.of(),
                        TextProcessingEventOrigin.unknown
                );

                inventoryRequestTracker.addRequest(new InventoryRequestStorage(request, revision, prevCursorContainer, prevContainers)); // Store the request to track it later
                ExperimentalPacketFactory.sendBedrockInventoryRequest(user, new ItemStackRequestInfo[] {request});
            } else {
                // Prevent interacting with the output slot
                return false;
            }
        }
        return super.handleClick(revision, javaSlot, button, action);
    }*/

}

// [15:13:59:567] [SERVER BOUND] - ItemStackRequestPacket(requests=[ItemStackRequest(requestId=-177, actions=[CraftRecipeAction(recipeNetworkId=119, numberOfRequestedCrafts=1), CraftResultsDeprecatedAction(resultItems=[BaseItemData(definition=SimpleItemDefinition(identifier=minecraft:oak_stairs, runtimeId=53, version=LEGACY, componentBased=false, componentData=null), damage=0, count=4, tag=null, canPlace=[], canBreak=[], blockingTicks=0, blockDefinition=UnknownDefinition[runtimeId=-1054044407], usingNetId=false, netId=0)], timesCrafted=1), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=CRAFTING_INPUT, slot=34, stackNetworkId=51, containerName=FullContainerName(container=CRAFTING_INPUT, dynamicId=null))), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=CRAFTING_INPUT, slot=36, stackNetworkId=56, containerName=FullContainerName(container=CRAFTING_INPUT, dynamicId=null))), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=CRAFTING_INPUT, slot=37, stackNetworkId=55, containerName=FullContainerName(container=CRAFTING_INPUT, dynamicId=null))), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=CRAFTING_INPUT, slot=38, stackNetworkId=60, containerName=FullContainerName(container=CRAFTING_INPUT, dynamicId=null))), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=CRAFTING_INPUT, slot=39, stackNetworkId=58, containerName=FullContainerName(container=CRAFTING_INPUT, dynamicId=null))), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=CRAFTING_INPUT, slot=40, stackNetworkId=59, containerName=FullContainerName(container=CRAFTING_INPUT, dynamicId=null))), TakeAction(count=4, source=ItemStackRequestSlotData(container=CREATED_OUTPUT, slot=50, stackNetworkId=-177, containerName=FullContainerName(container=CREATED_OUTPUT, dynamicId=null)), destination=ItemStackRequestSlotData(container=CURSOR, slot=0, stackNetworkId=0, containerName=FullContainerName(container=CURSOR, dynamicId=null)))], filterStrings=[], textProcessingEventOrigin=null)])
// [15:13:59:583] [CLIENT BOUND] - ItemStackResponsePacket(entries=[ItemStackResponse(result=OK, requestId=-177, containers=[ItemStackResponseContainer(container=CRAFTING_INPUT, items=[ItemStackResponseSlot(slot=34, hotbarSlot=34, count=8, stackNetworkId=51, customName=, durabilityCorrection=0, filteredCustomName=), ItemStackResponseSlot(slot=36, hotbarSlot=36, count=8, stackNetworkId=56, customName=, durabilityCorrection=0, filteredCustomName=), ItemStackResponseSlot(slot=37, hotbarSlot=37, count=8, stackNetworkId=55, customName=, durabilityCorrection=0, filteredCustomName=), ItemStackResponseSlot(slot=38, hotbarSlot=38, count=8, stackNetworkId=60, customName=, durabilityCorrection=0, filteredCustomName=), ItemStackResponseSlot(slot=39, hotbarSlot=39, count=8, stackNetworkId=58, customName=, durabilityCorrection=0, filteredCustomName=), ItemStackResponseSlot(slot=40, hotbarSlot=40, count=8, stackNetworkId=59, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=CRAFTING_INPUT, dynamicId=null)), ItemStackResponseContainer(container=CURSOR, items=[ItemStackResponseSlot(slot=0, hotbarSlot=0, count=4, stackNetworkId=61, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=CURSOR, dynamicId=null))])])
