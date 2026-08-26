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

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.CraftingRecipe;
import net.raphimc.viabedrock.protocol.model.RecipeIngredient;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Holds the recipes the server sent.
 *
 * <p>Bedrock Edition doesn't let the client simply take an item out of a result slot. It has to name the recipe it
 * wants to craft, so the recipe matching the current inputs has to be looked up.</p>
 */
public class RecipeTracker extends StoredObject {


    private List<CraftingRecipe> recipes = Collections.emptyList();

    public RecipeTracker(final UserConnection user) {
        super(user);
    }

    public List<CraftingRecipe> getRecipes() {
        return this.recipes;
    }

    /**
     * Finds the recipe which turns the given inputs into the given result.
     *
     * @param blockName The block the recipe is crafted at
     * @param result    The item the server put into the result slot
     * @param inputs    The current content of the crafting grid, row by row
     * @param width     The width of the crafting grid
     * @return The matching recipe or null if there is none
     */
    public CraftingRecipe findRecipe(final String blockName, final BedrockItem result, final List<BedrockItem> inputs, final int width) {
        CraftingRecipe fallback = null;
        for (CraftingRecipe recipe : this.recipes) {
            if (!recipe.blockName().equals(blockName)) continue;
            if (recipe.outputs().isEmpty() || recipe.outputs().get(0).identifier() != result.identifier()) continue;

            if (this.matchesInputs(recipe, inputs, width)) {
                return recipe;
            } else if (fallback == null) {
                // The result already tells us what the server crafted, so a recipe whose inputs can't be checked
                // (because they are described by a Molang expression or an alias) is still better than nothing
                fallback = recipe;
            }
        }
        return fallback;
    }

    private boolean matchesInputs(final CraftingRecipe recipe, final List<BedrockItem> inputs, final int width) {
        if (recipe.isShaped()) {
            final int height = (inputs.size() + width - 1) / width;
            if (recipe.width() > width || recipe.height() > height) {
                return false;
            }
            // The recipe can be placed anywhere in the grid, so every offset has to be tried
            for (int offsetY = 0; offsetY <= height - recipe.height(); offsetY++) {
                for (int offsetX = 0; offsetX <= width - recipe.width(); offsetX++) {
                    if (this.matchesShapedAt(recipe, inputs, width, height, offsetX, offsetY)) {
                        return true;
                    }
                }
            }
            return false;
        }

        final List<BedrockItem> remaining = new ArrayList<>();
        for (BedrockItem input : inputs) {
            if (!input.isEmpty()) {
                remaining.add(input);
            }
        }
        for (RecipeIngredient ingredient : recipe.ingredients()) {
            if (ingredient.isEmpty()) continue;
            boolean found = false;
            for (int i = 0; i < remaining.size(); i++) {
                if (this.matches(ingredient, remaining.get(i))) {
                    remaining.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    private boolean matchesShapedAt(final CraftingRecipe recipe, final List<BedrockItem> inputs, final int width, final int height, final int offsetX, final int offsetY) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final BedrockItem input = inputs.get(y * width + x);
                final int recipeX = x - offsetX;
                final int recipeY = y - offsetY;

                final RecipeIngredient ingredient;
                if (recipeX < 0 || recipeY < 0 || recipeX >= recipe.width() || recipeY >= recipe.height()) {
                    ingredient = RecipeIngredient.EMPTY;
                } else {
                    ingredient = recipe.ingredients().get(recipeY * recipe.width() + recipeX);
                }

                if (ingredient.isEmpty() != input.isEmpty()) {
                    return false;
                }
                if (!ingredient.isEmpty() && !this.matches(ingredient, input)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matches(final RecipeIngredient ingredient, final BedrockItem item) {
        if (item.isEmpty()) {
            return ingredient.isEmpty();
        }

        final ItemRewriter itemRewriter = this.user().get(ItemRewriter.class);
        return switch (ingredient.type()) {
            case EMPTY -> false;
            case STRING_ID_META -> {
                final Integer id = itemRewriter.getItems().get(ingredient.identifier());
                yield id != null && id == item.identifier() && this.matchesMeta(ingredient, item);
            }
            case TAG -> {
                final String identifier = itemRewriter.getItems().inverse().get(item.identifier());
                final Set<String> taggedItems = BedrockProtocol.MAPPINGS.getBedrockItemTags().get(ingredient.tag());
                yield identifier != null && taggedItems != null && taggedItems.contains(identifier);
            }
            // Neither Molang expressions nor aliases can be resolved here
            case MOLANG, COMPLEX_ALIAS -> true;
        };
    }

    private boolean matchesMeta(final RecipeIngredient ingredient, final BedrockItem item) {
        return ingredient.meta() == RecipeIngredient.WILDCARD_META || ingredient.meta() == (item.data() & 0xFFFF);
    }

    /**
     * Reads the crafting data packet. Recipe types which can't appear in a result slot are read but discarded.
     */
    public void readRecipes(final PacketWrapper wrapper) {
        final ItemRewriter itemRewriter = this.user().get(ItemRewriter.class);
        final List<CraftingRecipe> recipes = new ArrayList<>();

        // 1.26.40 sends one array per recipe type instead of a single array of tagged entries
        this.readArray(wrapper, () -> recipes.add(this.readShapedRecipe(wrapper, itemRewriter))); // shaped recipes
        this.readArray(wrapper, () -> recipes.add(this.readShapelessRecipe(wrapper, itemRewriter))); // shapeless recipes
        this.readArray(wrapper, () -> { // multi recipes
            wrapper.read(BedrockTypes.UUID); // uuid
            wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // recipe net id
        });
        this.readArray(wrapper, () -> recipes.add(this.readShapelessRecipe(wrapper, itemRewriter))); // shulker box recipes
        this.readArray(wrapper, () -> recipes.add(this.readShapelessRecipe(wrapper, itemRewriter))); // shapeless chemistry recipes
        this.readArray(wrapper, () -> recipes.add(this.readShapedRecipe(wrapper, itemRewriter))); // shaped chemistry recipes
        this.readArray(wrapper, () -> recipes.add(this.readSmithingTransformRecipe(wrapper, itemRewriter))); // smithing transform recipes
        this.readArray(wrapper, () -> recipes.add(this.readSmithingTrimRecipe(wrapper))); // smithing trim recipes

        this.readArray(wrapper, () -> { // potion type recipes
            for (int i = 0; i < 6; i++) {
                wrapper.read(BedrockTypes.VAR_INT); // input id/meta, ingredient id/meta, output id/meta
            }
        });
        this.readArray(wrapper, () -> { // potion container recipes
            for (int i = 0; i < 3; i++) {
                wrapper.read(BedrockTypes.VAR_INT); // input, ingredient, output
            }
        });
        this.readArray(wrapper, () -> { // material reducers
            wrapper.read(BedrockTypes.VAR_INT); // mix
            wrapper.read(BedrockTypes.VAR_INT); // output network id
            wrapper.read(BedrockTypes.VAR_INT); // output count
        });
        wrapper.read(Types.BOOLEAN); // clear recipes

        this.recipes = recipes;
        ViaBedrock.getPlatform().getLogger().log(Level.FINE, "Received " + recipes.size() + " usable recipes");
    }

    private void readArray(final PacketWrapper wrapper, final Runnable entryReader) {
        final int count = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // entry count
        for (int i = 0; i < count; i++) {
            entryReader.run();
        }
    }

    private CraftingRecipe readShapelessRecipe(final PacketWrapper wrapper, final ItemRewriter itemRewriter) {
        wrapper.read(BedrockTypes.ASCII_STRING); // recipe id
        final int ingredientCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // ingredient count
        final List<RecipeIngredient> ingredients = new ArrayList<>(ingredientCount);
        for (int i = 0; i < ingredientCount; i++) {
            ingredients.add(this.readIngredient(wrapper));
        }
        final List<BedrockItem> outputs = this.readOutputs(wrapper, itemRewriter);
        wrapper.read(BedrockTypes.UUID); // uuid
        final String blockName = wrapper.read(BedrockTypes.STRING); // block name
        wrapper.read(BedrockTypes.VAR_INT); // priority
        this.readUnlockingRequirement(wrapper);
        final int netId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // recipe net id

        return new CraftingRecipe(netId, blockName, 0, 0, ingredients, outputs);
    }

    private CraftingRecipe readShapedRecipe(final PacketWrapper wrapper, final ItemRewriter itemRewriter) {
        wrapper.read(BedrockTypes.ASCII_STRING); // recipe id
        final int width = wrapper.read(BedrockTypes.VAR_INT); // width
        final int height = wrapper.read(BedrockTypes.VAR_INT); // height
        final int ingredientCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // ingredient count
        final List<RecipeIngredient> ingredients = new ArrayList<>(ingredientCount);
        for (int i = 0; i < ingredientCount; i++) {
            ingredients.add(this.readIngredient(wrapper));
        }
        final List<BedrockItem> outputs = this.readOutputs(wrapper, itemRewriter);
        wrapper.read(BedrockTypes.UUID); // uuid
        final String blockName = wrapper.read(BedrockTypes.STRING); // block name
        wrapper.read(BedrockTypes.VAR_INT); // priority
        wrapper.read(Types.BOOLEAN); // assume symmetry
        this.readUnlockingRequirement(wrapper);
        final int netId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // recipe net id

        // The grid is sent as a flat list, so a mismatching size would make the offsets meaningless
        if (ingredients.size() != width * height) {
            return new CraftingRecipe(netId, blockName, 0, 0, ingredients, outputs);
        }
        return new CraftingRecipe(netId, blockName, width, height, ingredients, outputs);
    }

    private CraftingRecipe readSmithingTransformRecipe(final PacketWrapper wrapper, final ItemRewriter itemRewriter) {
        wrapper.read(BedrockTypes.ASCII_STRING); // recipe id
        final List<RecipeIngredient> ingredients = new ArrayList<>(3);
        ingredients.add(this.readIngredient(wrapper)); // template
        ingredients.add(this.readIngredient(wrapper)); // base
        ingredients.add(this.readIngredient(wrapper)); // addition
        final BedrockItem output = wrapper.read(itemRewriter.itemTypeWithoutNetId()); // result
        final String blockName = wrapper.read(BedrockTypes.STRING); // block name
        final int netId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // recipe net id

        return new CraftingRecipe(netId, blockName, 0, 0, ingredients, List.of(output));
    }

    private CraftingRecipe readSmithingTrimRecipe(final PacketWrapper wrapper) {
        wrapper.read(BedrockTypes.ASCII_STRING); // recipe id
        final List<RecipeIngredient> ingredients = new ArrayList<>(3);
        ingredients.add(this.readIngredient(wrapper)); // template
        ingredients.add(this.readIngredient(wrapper)); // input
        ingredients.add(this.readIngredient(wrapper)); // addition
        final String blockName = wrapper.read(BedrockTypes.STRING); // block name
        final int netId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // recipe net id

        // Trim recipes keep the input item, so the result can only be determined by the server
        return new CraftingRecipe(netId, blockName, 0, 0, ingredients, Collections.emptyList());
    }

    private List<BedrockItem> readOutputs(final PacketWrapper wrapper, final ItemRewriter itemRewriter) {
        final int outputCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // output count
        final List<BedrockItem> outputs = new ArrayList<>(outputCount);
        for (int i = 0; i < outputCount; i++) {
            outputs.add(wrapper.read(itemRewriter.itemTypeWithoutNetId()));
        }
        return outputs;
    }

    private void readUnlockingRequirement(final PacketWrapper wrapper) {
        if (!wrapper.read(Types.BOOLEAN)) { // has unlocking requirement
            return;
        }
        wrapper.read(BedrockTypes.VAR_INT); // unlocking context
        if (wrapper.read(Types.BOOLEAN)) { // has unlocking ingredients
            final int count = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // ingredient count
            for (int i = 0; i < count; i++) {
                this.readIngredient(wrapper);
            }
        }
    }

    /**
     * Reads a recipe ingredient. 1.26.40 replaced the numeric descriptor type with a string one.
     */
    private RecipeIngredient readIngredient(final PacketWrapper wrapper) {
        final int valid = wrapper.read(BedrockTypes.VAR_INT); // type
        if (valid == 0) { // invalid
            wrapper.read(BedrockTypes.VAR_INT); // metadata
            wrapper.read(BedrockTypes.VAR_INT); // count
            return RecipeIngredient.EMPTY;
        }

        final String descriptorType = wrapper.read(BedrockTypes.STRING); // descriptor type
        String identifier = null;
        String tag = null;
        int meta = 0;
        final RecipeIngredient.Type type;
        switch (descriptorType) {
            case "name" -> {
                type = RecipeIngredient.Type.STRING_ID_META;
                identifier = wrapper.read(BedrockTypes.STRING); // name
                meta = wrapper.read(BedrockTypes.VAR_INT); // metadata
            }
            case "molang" -> {
                type = RecipeIngredient.Type.MOLANG;
                wrapper.read(BedrockTypes.STRING); // expression
                wrapper.read(BedrockTypes.SHORT_LE); // version
            }
            case "item_tag" -> {
                type = RecipeIngredient.Type.TAG;
                tag = wrapper.read(BedrockTypes.STRING); // tag
                meta = wrapper.read(BedrockTypes.VAR_INT); // metadata
            }
            case "empty" -> {
                type = RecipeIngredient.Type.EMPTY;
                wrapper.read(BedrockTypes.VAR_INT); // metadata
            }
            default -> throw new IllegalStateException("Unhandled recipe ingredient descriptor: " + descriptorType);
        }
        final int count = wrapper.read(BedrockTypes.VAR_INT); // count

        if (type == RecipeIngredient.Type.EMPTY) {
            return RecipeIngredient.EMPTY;
        }
        return new RecipeIngredient(type, identifier, tag, meta, count);
    }

}
