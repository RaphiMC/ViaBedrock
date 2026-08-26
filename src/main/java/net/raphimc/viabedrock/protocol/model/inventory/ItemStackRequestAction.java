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
package net.raphimc.viabedrock.protocol.model.inventory;

import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackRequestActionType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;

import java.util.List;

/**
 * A single action of an {@link ItemStackRequest}. Only the actions which can be produced by translating Java Edition
 * inventory interactions are implemented.
 */
public sealed interface ItemStackRequestAction {

    ItemStackRequestActionType type();

    /**
     * Moves {@code count} items from {@code source} to {@code destination}.
     */
    record Take(int count, ItemStackRequestSlotInfo source, ItemStackRequestSlotInfo destination) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.Take;
        }
    }

    /**
     * Moves {@code count} items from {@code source} to {@code destination}. Functionally identical to {@link Take},
     * but the Bedrock client uses it when the cursor is the source of the transfer.
     */
    record Place(int count, ItemStackRequestSlotInfo source, ItemStackRequestSlotInfo destination) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.Place;
        }
    }

    record Swap(ItemStackRequestSlotInfo slot1, ItemStackRequestSlotInfo slot2) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.Swap;
        }
    }

    record Drop(int count, ItemStackRequestSlotInfo source, boolean randomly) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.Drop;
        }
    }

    record Destroy(int count, ItemStackRequestSlotInfo source) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.Destroy;
        }
    }

    record Consume(int count, ItemStackRequestSlotInfo source) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.Consume;
        }
    }

    record Create(int resultSlot) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.Create;
        }
    }

    record CraftCreative(int creativeItemNetworkId, int repetitions) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.CraftCreative;
        }
    }

    record CraftRecipe(int recipeNetworkId, int repetitions) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.CraftRecipe;
        }
    }

    /**
     * Used for menus which craft a single item without a fixed recipe, like the anvil. The filter string index refers
     * to the text the player entered, which the server has to check for profanity.
     */
    record CraftRecipeOptional(int recipeNetworkId, int filterStringIndex) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.CraftRecipeOptional;
        }
    }

    record CraftRepairAndDisenchant(int recipeNetworkId, int repairCost, int repetitions) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.CraftRepairAndDisenchant;
        }
    }

    record CraftLoom(String patternId, int repetitions) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.CraftLoom;
        }
    }

    record CraftRecipeAuto(int recipeNetworkId, int repetitions) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.CraftRecipeAuto;
        }
    }

    /**
     * Sent by the Bedrock client after a crafting or creative action to tell the server what it expects the result to
     * be. Deprecated, but still required by the vanilla protocol flow.
     */
    record CraftResultsDeprecated(List<BedrockItem> results, int iterations) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.CraftResults;
        }
    }

    record MineBlock(int hotbarSlot, int predictedDurability, int stackNetworkId) implements ItemStackRequestAction {
        @Override
        public ItemStackRequestActionType type() {
            return ItemStackRequestActionType.ScreenHUDMineBlock;
        }
    }

}
