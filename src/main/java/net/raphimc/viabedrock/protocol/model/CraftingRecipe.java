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
package net.raphimc.viabedrock.protocol.model;

import java.util.List;

/**
 * A recipe the server offers. Bedrock Edition expects the client to tell it which recipe it wants to craft, so these
 * have to be tracked to be able to translate taking an item out of a result slot.
 *
 * @param netId      The id used to request this recipe from the server
 * @param blockName  The block this recipe can be crafted at, for example "crafting_table" or "stonecutter"
 * @param width      The width of the crafting grid for shaped recipes, 0 otherwise
 * @param height     The height of the crafting grid for shaped recipes, 0 otherwise
 * @param ingredients The inputs of the recipe, row by row for shaped recipes
 * @param outputs    The items this recipe produces
 */
public record CraftingRecipe(int netId, String blockName, int width, int height, List<RecipeIngredient> ingredients, List<BedrockItem> outputs) {

    public boolean isShaped() {
        return this.width > 0 && this.height > 0;
    }

}
