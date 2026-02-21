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
package net.raphimc.viabedrock.experimental.model.container.block;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.experimental.ExperimentalPacketFactory;
import net.raphimc.viabedrock.experimental.model.container.ExperimentalContainer;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestInfo;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestSlotInfo;
import net.raphimc.viabedrock.experimental.model.recipe.RecipeType;
import net.raphimc.viabedrock.experimental.model.recipe.SmithingRecipe;
import net.raphimc.viabedrock.experimental.storage.*;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.TextProcessingEventOrigin;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.ClickType;
import net.raphimc.viabedrock.protocol.data.generated.bedrock.CustomBlockTags;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SmithingContainer extends ExperimentalContainer {

    public SmithingContainer(UserConnection user, byte containerId, ContainerType type, TextComponent title, BlockPosition position) {
        super(user, containerId, type, title, position, 4, CustomBlockTags.SMITHING_TABLE);
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        return switch (slot) {
            case 53 -> new FullContainerName(ContainerEnumName.SmithingTableTemplateContainer, null);
            case 51 -> new FullContainerName(ContainerEnumName.SmithingTableInputContainer, null);
            case 52 -> new FullContainerName(ContainerEnumName.SmithingTableMaterialContainer, null);
            case 50 -> new FullContainerName(ContainerEnumName.SmithingTableResultPreviewContainer, null);
            default -> throw new IllegalArgumentException("Invalid slot for Smithing Container: " + slot);
        };
    }

    @Override
    public int javaSlot(final int slot) {
        return switch (slot) {
            case 53 -> 0;
            case 51 -> 1;
            case 52 -> 2;
            case 50 -> 3;
            default -> super.javaSlot(slot);
        };
    }

    @Override
    public int bedrockSlot(final int slot) {
        return switch (slot) {
            case 0 -> 53;
            case 1 -> 51;
            case 2 -> 52;
            case 3 -> 50;
            default -> super.bedrockSlot(slot);
        };
    }

    @Override
    public BedrockItem getItem(int bedrockSlot) {
        // Fix magic offset
        bedrockSlot -= 50;
        return this.items[bedrockSlot];
    }

    @Override
    public boolean setItem(final int bedrockSlot, final BedrockItem item) {
        // Fix magic offset
        return super.setItem(bedrockSlot - 50, item);
    }

    @Override
    public boolean setItems(final BedrockItem[] items) {
        //TODO: Fix magic offset?
        if (items.length != this.items.length) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to set items for " + this.type + ", but items array length was not correct (" + items.length + " != " + this.items.length + ")");
            return false;
        }

        for (int i = 0; i < items.length; i++) {
            this.setItem(i, items[i]);
        }
        return true;
    }

    @Override
    public boolean handleClick(int revision, short javaSlot, byte button, ClickType action) {
        boolean result = false;
        if (javaSlot != 3) {
            // Handle click first so we update the crafting grid before checking for a recipe
            result = super.handleClick(revision, javaSlot, button, action);
        }
        if (!ViaBedrock.getConfig().shouldEnableExperimentalFeatures()) {
            return result;
        }
        //TODO: This is experimental code...

        ItemRewriter itemRewriter = user.get(ItemRewriter.class);
        final CraftingDataStorage craftingDataStorage = this.getRecipeData();
        BedrockItem resultItem = BedrockItem.empty();
        if (craftingDataStorage != null) {

        }

        //this.setItem(0, resultItem);
        PacketWrapper containerSlot = PacketWrapper.create(ClientboundPackets1_21_11.CONTAINER_SET_SLOT, user);
        containerSlot.write(Types.VAR_INT, (int) this.containerId());
        containerSlot.write(Types.VAR_INT, revision);
        containerSlot.write(Types.SHORT, (short) 0); // Output slot
        containerSlot.write(VersionedTypes.V1_21_11.item, itemRewriter.javaItem(resultItem));
        containerSlot.send(BedrockProtocol.class);

        if (craftingDataStorage == null) {
            // No valid recipe found
            return result;
        }

        if (javaSlot != 3) {
            // Handle click first so we update the crafting grid before checking for a recipe
            return result;
        }

        ExperimentalInventoryTracker inventoryTracker = user.get(ExperimentalInventoryTracker.class);
        InventoryRequestTracker inventoryRequestTracker = user.get(InventoryRequestTracker.class);

        List<ExperimentalContainer> prevContainers = new ArrayList<>();
        prevContainers.add(this.copy());
        prevContainers.add(inventoryTracker.getInventoryContainer().copy());
        ExperimentalContainer prevCursorContainer = inventoryTracker.getHudContainer().copy();

        int bedrockSlot = this.bedrockSlot(javaSlot);

        List<ItemStackRequestAction> actions = new ArrayList<>();
        actions.add(new ItemStackRequestAction.CraftRecipeAction(craftingDataStorage.networkId(), 1));
        actions.add(new ItemStackRequestAction.ConsumeAction(
                1,
                new ItemStackRequestSlotInfo(
                        this.getFullContainerName(53),
                        (byte) 53,
                        this.getItem(53).netId()
                )
        ));
        actions.add(new ItemStackRequestAction.ConsumeAction(
                1,
                new ItemStackRequestSlotInfo(
                        this.getFullContainerName(51),
                        (byte) 51,
                        this.getItem(51).netId()
                )
        ));
        actions.add(new ItemStackRequestAction.ConsumeAction(
                1,
                new ItemStackRequestSlotInfo(
                        this.getFullContainerName(52),
                        (byte) 52,
                        this.getItem(52).netId()
                )
        ));

        int nextRequestId = inventoryRequestTracker.nextRequestId();

        actions.add(
                new ItemStackRequestAction.TakeAction(
                        1, // Total amount to take
                        new ItemStackRequestSlotInfo(
                                this.getFullContainerName(bedrockSlot),
                                (byte) bedrockSlot,
                                nextRequestId // TODO
                        ),
                        new ItemStackRequestSlotInfo(
                                new FullContainerName(ContainerEnumName.CursorContainer, null),
                                (byte) 0,
                                0 // The stackNetworkId is not known yet
                        )
                )
        );

        ItemStackRequestInfo request = new ItemStackRequestInfo(
                nextRequestId,
                actions,
                List.of(),
                TextProcessingEventOrigin.unknown
        );

        inventoryRequestTracker.addRequest(new InventoryRequestStorage(request, revision, prevCursorContainer, prevContainers)); // Store the request to track it later
        ExperimentalPacketFactory.sendBedrockInventoryRequest(user, new ItemStackRequestInfo[]{request});

        inventoryTracker.getHudContainer().setItem(0, resultItem); // Update cursor to the crafted item

        BedrockItem templateItem = this.getItem(53).copy();
        if (templateItem.amount() - 1 != 0) {
            templateItem.setAmount(templateItem.amount() - 1);
        } else {
            templateItem = BedrockItem.empty();
        }
        this.setItem(53, templateItem);

        BedrockItem inputItem = this.getItem(51).copy();
        if (inputItem.amount() - 1 != 0) {
            inputItem.setAmount(inputItem.amount() - 1);
        } else {
            inputItem = BedrockItem.empty();
        }
        this.setItem(51, inputItem);

        BedrockItem materialItem = this.getItem(52).copy();
        if (materialItem.amount() - 1 != 0) {
            materialItem.setAmount(materialItem.amount() - 1);
        } else {
            materialItem = BedrockItem.empty();
        }
        this.setItem(52, materialItem);

        ExperimentalPacketFactory.sendJavaContainerSetContent(user, this);

        //TODO: Re-Update the output slot
        return true;
    }

    private CraftingDataStorage getRecipeData() {
        CraftingDataTracker craftingDataTracker = user.get(CraftingDataTracker.class);

        for (CraftingDataStorage craftingData : craftingDataTracker.getCraftingDataList()) {
            if (craftingData.recipe() == null || !craftingData.recipe().getRecipeTag().equals("smithing_table")) {
                continue;
            }

            switch (craftingData.type()) {
                case SMITHING_TRIM, SMITHING_TRANSFORM -> {
                    SmithingRecipe smithingRecipe = (SmithingRecipe) craftingData.recipe();
                    if (smithingRecipe.getTemplate().matchesItem(user, this.getItem(53)) &&
                            smithingRecipe.getBaseIngredient().matchesItem(user, this.getItem(51)) &&
                            smithingRecipe.getAdditionIngredient().matchesItem(user, this.getItem(52))) {
                        return craftingData;
                    }
                }
                default -> {
                    ViaBedrock.getPlatform().getLogger().warning(
                            "Unknown recipe type for smithing: " + craftingData.type() + " in recipe " + craftingData.recipe().getUniqueId()
                    );
                }
            }
        }
        return null;
    }

}

//[22:24:41:011] [SERVER BOUND] - ItemStackRequestPacket(requests=[ItemStackRequest(requestId=-31, actions=[CraftRecipeAction(recipeNetworkId=821, numberOfRequestedCrafts=1), CraftResultsDeprecatedAction(resultItems=[BaseItemData(definition=SimpleItemDefinition(identifier=minecraft:golden_helmet, runtimeId=383, version=LEGACY, componentBased=false, componentData=null), damage=0, count=1, tag={
//        "Damage": 0i,
//        "Trim": {
//        "Material": "iron",
//        "Pattern": "wild"
//        }
//        }, canPlace=[], canBreak=[], blockingTicks=0, blockDefinition=UnknownDefinition[runtimeId=0], usingNetId=false, netId=0)], timesCrafted=1), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=SMITHING_TABLE_TEMPLATE, slot=53, stackNetworkId=11, containerName=FullContainerName(container=SMITHING_TABLE_TEMPLATE, dynamicId=null))), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=SMITHING_TABLE_INPUT, slot=51, stackNetworkId=10, containerName=FullContainerName(container=SMITHING_TABLE_INPUT, dynamicId=null))), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=SMITHING_TABLE_MATERIAL, slot=52, stackNetworkId=8, containerName=FullContainerName(container=SMITHING_TABLE_MATERIAL, dynamicId=null))), TakeAction(count=1, source=ItemStackRequestSlotData(container=CREATED_OUTPUT, slot=50, stackNetworkId=-31, containerName=FullContainerName(container=CREATED_OUTPUT, dynamicId=null)), destination=ItemStackRequestSlotData(container=CURSOR, slot=0, stackNetworkId=0, containerName=FullContainerName(container=CURSOR, dynamicId=null)))], filterStrings=[], textProcessingEventOrigin=null)])
// [22:24:41:055] [CLIENT BOUND] - ItemStackResponsePacket(entries=[ItemStackResponse(result=OK, requestId=-31, containers=[ItemStackResponseContainer(container=SMITHING_TABLE_TEMPLATE, items=[ItemStackResponseSlot(slot=53, hotbarSlot=53, count=63, stackNetworkId=11, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=SMITHING_TABLE_TEMPLATE, dynamicId=null)), ItemStackResponseContainer(container=SMITHING_TABLE_INPUT, items=[ItemStackResponseSlot(slot=51, hotbarSlot=51, count=0, stackNetworkId=0, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=SMITHING_TABLE_INPUT, dynamicId=null)), ItemStackResponseContainer(container=SMITHING_TABLE_MATERIAL, items=[ItemStackResponseSlot(slot=52, hotbarSlot=52, count=59, stackNetworkId=8, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=SMITHING_TABLE_MATERIAL, dynamicId=null)), ItemStackResponseContainer(container=CURSOR, items=[ItemStackResponseSlot(slot=0, hotbarSlot=0, count=1, stackNetworkId=12, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=CURSOR, dynamicId=null))])])
