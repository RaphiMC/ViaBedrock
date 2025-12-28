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
package net.raphimc.viabedrock.experimental.model.recipe;

import java.util.UUID;

public class SmithingRecipe extends Recipe {

    private final RecipeIngredient template;
    private final RecipeIngredient baseIngredient;
    private final RecipeIngredient additionIngredient;
    private final NetworkBedrockItem result;

    public SmithingRecipe(String uniqueId, UUID recipeId, String recipeTag, int priority, RecipeIngredient template, RecipeIngredient baseIngredient, RecipeIngredient additionIngredient, NetworkBedrockItem result) {
        super(uniqueId, recipeId, recipeTag, priority);
        this.template = template;
        this.baseIngredient = baseIngredient;
        this.additionIngredient = additionIngredient;
        this.result = result;
    }

}
