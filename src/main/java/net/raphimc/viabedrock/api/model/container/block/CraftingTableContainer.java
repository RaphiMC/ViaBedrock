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
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.experimental.ExperimentalPacketFactory;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestInfo;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestSlotInfo;
import net.raphimc.viabedrock.experimental.model.recipe.ItemDescriptor;
import net.raphimc.viabedrock.experimental.model.recipe.Recipe;
import net.raphimc.viabedrock.experimental.model.recipe.ShapedRecipe;
import net.raphimc.viabedrock.experimental.model.recipe.ShapelessRecipe;
import net.raphimc.viabedrock.experimental.storage.CraftingDataStorage;
import net.raphimc.viabedrock.experimental.storage.CraftingDataTracker;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestStorage;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestTracker;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.TextProcessingEventOrigin;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.ClickType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.model.ItemEntry;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class CraftingTableContainer extends Container {

    public CraftingTableContainer(UserConnection user, byte containerId, TextComponent title, BlockPosition position) {
        super(user, containerId, ContainerType.WORKBENCH, title, position, 10, "crafting_table", "workbench");
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        return switch (slot) {
            case 32, 33, 34, 35, 36, 37, 38, 39, 40 ->
                    new FullContainerName(ContainerEnumName.CraftingInputContainer, null);
            case 50 -> new FullContainerName(ContainerEnumName.CreatedOutputContainer, null);
            default -> {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Invalid slot " + slot + " in CraftingTableContainer");
                yield FullContainerName.EMPTY;
            }
        };
    }

    @Override
    public int javaSlot(final int bedrockSlot) {
        return switch (bedrockSlot) {
            case 32, 33, 34, 35, 36, 37, 38, 39, 40 -> bedrockSlot - 31;
            case 50 -> 0;
            default -> super.javaSlot(bedrockSlot);
        };
    }

    @Override
    public int bedrockSlot(final int javaSlot) {
        return switch (javaSlot) {
            case 1, 2, 3, 4, 5, 6, 7, 8, 9 -> javaSlot + 31;
            case 0 -> 50;
            default -> super.bedrockSlot(javaSlot);
        };
    }

    @Override
    public boolean handleClick(final int revision, final short javaSlot, final byte button, final ClickType action) {
        if (javaSlot == 0) {
            if (ViaBedrock.getConfig().shouldEnableExperimentalFeatures()) {
                //TODO: This is experimental code...
                InventoryTracker inventoryTracker = user.get(InventoryTracker.class);
                InventoryRequestTracker inventoryRequestTracker = user.get(InventoryRequestTracker.class);
                ItemRewriter itemRewriter = user.get(ItemRewriter.class);

                List<Container> prevContainers = new ArrayList<>();
                prevContainers.add(this.copy());
                prevContainers.add(inventoryTracker.getInventoryContainer().copy());
                Container prevCursorContainer = inventoryTracker.getHudContainer().copy();

                final CraftingDataStorage craftingDataStorage = this.getRecipeData();
                if (craftingDataStorage == null) {
                    // No valid recipe found
                    return false;
                }
                List<BedrockItem> resultItems = switch (craftingDataStorage.type()) { // This is a list but crafting results only have one item
                    case SHAPELESS -> ((ShapelessRecipe) craftingDataStorage.recipe()).getResults();
                    case SHAPED -> ((ShapedRecipe) craftingDataStorage.recipe()).getResults();
                    default -> {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown recipe type for crafting: " + craftingDataStorage.type());
                        yield Collections.emptyList();
                    }
                };

                int craftableAmount = 1;//this.getCraftableAmount(craftingDataStorage);
                if (craftableAmount <= 0) {
                    return false;
                }
                int bedrockSlot = this.bedrockSlot(javaSlot);
                craftableAmount = button == 0 ? 1 : craftableAmount; // Left click = 1, right click = max, shift click = max to inventory

                List<ItemStackRequestAction> actions = new ArrayList<>();
                actions.add(new ItemStackRequestAction.CraftRecipeAction(craftingDataStorage.networkId(), craftableAmount));
                //actions.add(new ItemStackRequestAction.CraftResultsDeprecatedAction(resultItems, 1)); //TODO: Deprecated action, hopefully removed in the future

                for (int i = 1; i <= 9; i++) {
                    int inputSlot = i + 31; // Crafting grid slots in bedrock
                    BedrockItem item = this.getItem(i);
                    if (!item.isEmpty() && item.amount() > 0) {
                        int toConsume = Math.min(item.amount(), craftableAmount);
                        actions.add(new ItemStackRequestAction.ConsumeAction(
                                toConsume,
                                new ItemStackRequestSlotInfo(
                                        this.getFullContainerName(inputSlot),
                                        (byte) inputSlot,
                                        this.getItem(i).netId()
                                )
                        ));
                    }
                }

                int nextRequestId = inventoryRequestTracker.nextRequestId();
                actions.add(
                        new ItemStackRequestAction.TakeAction(
                                craftableAmount * resultItems.get(0).amount(), // Total amount to take
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

                inventoryTracker.getHudContainer().setItem(0, resultItems.get(0)); // Update cursor to the crafted item
                this.setItems(BedrockItem.emptyArray(10)); // TODO: Clear crafting grid and output (Handle amount to remove)
                PacketFactory.sendJavaContainerSetContent(user, this);

                return true;
            } else {
                // Prevent interacting with the output slot
                return false;
            }
        } else {
            //TODO: Handle output recipe showing properly (idk if java handles this client side)
            return super.handleClick(revision, javaSlot, button, action);
        }
    }

    private CraftingDataStorage getRecipeData() {
        CraftingDataTracker craftingDataTracker = user.get(CraftingDataTracker.class);

        for (CraftingDataStorage craftingData : craftingDataTracker.getCraftingDataList()) {
            if (craftingData.recipe() == null || !craftingData.recipe().getRecipeTag().equals("crafting_table")) {
                continue;
            }

            switch (craftingData.type()) {
                case SHAPELESS -> {
                    if (matchShapelessRecipe((ShapelessRecipe) craftingData.recipe())) {
                        return craftingData;
                    }
                }
                case SHAPED -> {
                    if (matchShapedRecipe((ShapedRecipe) craftingData.recipe())) {
                        return craftingData;
                    }
                }
                case USER_DATA_SHAPELESS -> {
                    // TODO: Not supported yet
                }
                default -> {
                    ViaBedrock.getPlatform().getLogger().warning(
                            "Unknown recipe type for crafting: " + craftingData.type() + " in recipe " + craftingData.recipe().getUniqueId()
                    );
                }
            }
        }
        return null;
    }

    private boolean matchShapelessRecipe(ShapelessRecipe recipe) {
        boolean[] used = new boolean[10];
        for (ItemDescriptor descriptor : recipe.getIngredients()) {
            if (!findMatchingSlot(descriptor, used)) {
                return false;
            }
        }
        return noExtraItems(used);
    }

    private boolean matchShapedRecipe(ShapedRecipe recipe) {
        int width = recipe.getPattern().length;
        int height = recipe.getPattern()[0].length;

        for (int startX = 0; startX <= 3 - width; startX++) {
            for (int startY = 0; startY <= 3 - height; startY++) {
                if (checkPattern(recipe, startX, startY) && noExtraItemsOutsidePattern(startX, startY, width, height)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean findMatchingSlot(ItemDescriptor descriptor, boolean[] used) {
        for (int slot = 1; slot <= 9; slot++) {
            if (used[slot]) continue;
            BedrockItem item = this.getItem(slot);
            if (matchesDescriptor(descriptor, item)) {
                used[slot] = true;
                return true;
            }
        }
        return false;
    }

    private boolean matchesDescriptor(ItemDescriptor descriptor, BedrockItem item) {
        return switch (descriptor.getType()) {
            case DEFAULT -> {
                int itemId = ((ItemDescriptor.DefaultDescriptor) descriptor).itemId();
                yield ((itemId == -1 || itemId == 0) && item.isEmpty()) || (!item.isEmpty() && item.identifier() == itemId);
            }
            case ITEM_TAG -> {
                //TODO
                String tag = ((ItemDescriptor.ItemTagDescriptor) descriptor).itemTag();
                yield !item.isEmpty() && item.tag() != null && item.tag().contains(tag);
            }
            case INVALID -> item.isEmpty();
            default -> false;
        };
    }

    private boolean noExtraItems(boolean[] used) {
        for (int slot = 1; slot <= 9; slot++) {
            if (!used[slot] && !this.getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPattern(ShapedRecipe recipe, int startX, int startY) {
        int width = recipe.getPattern().length;
        int height = recipe.getPattern()[0].length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                ItemDescriptor descriptor = recipe.getPattern()[x][y];
                BedrockItem item = this.getItem((startY + y) * 3 + (startX + x) + 1);
                if (!matchesDescriptor(descriptor, item)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean noExtraItemsOutsidePattern(int startX, int startY, int width, int height) {
        for (int gx = 0; gx < 3; gx++) {
            for (int gy = 0; gy < 3; gy++) {
                if (gx >= startX && gx < startX + width && gy >= startY && gy < startY + height) {
                    continue;
                }
                if (!this.getItem(gy * 3 + gx + 1).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }



}