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
package net.raphimc.viabedrock.api.model.container;

import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;

import static net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName.*;

/**
 * The slot layouts of the menus which don't have a container of their own. Their contents are sent through the player
 * UI container, so every slot of the Java menu maps to one slot of that container.
 *
 * <p>Both arrays are indexed by Java menu slot.</p>
 */
public enum UiContainerLayout {

    /**
     * The 3x3 crafting table. Java puts the result first, Bedrock keeps it in the shared created output slot.
     */
    WORKBENCH(
            new int[]{50, 32, 33, 34, 35, 36, 37, 38, 39, 40},
            new ContainerEnumName[]{CreatedOutputContainer, CraftingInputContainer, CraftingInputContainer, CraftingInputContainer, CraftingInputContainer, CraftingInputContainer, CraftingInputContainer, CraftingInputContainer, CraftingInputContainer, CraftingInputContainer},
            0, CraftType.RECIPE
    ),
    ANVIL(
            new int[]{1, 2, 50},
            new ContainerEnumName[]{AnvilInputContainer, AnvilMaterialContainer, AnvilResultPreviewContainer},
            2, CraftType.OPTIONAL
    ),
    ENCHANTMENT(
            new int[]{14, 15},
            new ContainerEnumName[]{EnchantingInputContainer, EnchantingMaterialContainer}
    ),
    LOOM(
            new int[]{9, 10, 11, 50},
            new ContainerEnumName[]{LoomInputContainer, LoomDyeContainer, LoomMaterialContainer, LoomResultPreviewContainer},
            3, CraftType.LOOM
    ),
    STONECUTTER(
            new int[]{3, 50},
            new ContainerEnumName[]{StonecutterInputContainer, StonecutterResultPreviewContainer},
            1, CraftType.RECIPE
    ),
    GRINDSTONE(
            new int[]{16, 17, 50},
            new ContainerEnumName[]{GrindstoneInputContainer, GrindstoneAdditionalContainer, GrindstoneResultPreviewContainer},
            2, CraftType.REPAIR_AND_DISENCHANT
    ),
    CARTOGRAPHY(
            new int[]{12, 13, 50},
            new ContainerEnumName[]{CartographyInputContainer, CartographyAdditionalContainer, CartographyResultPreviewContainer},
            2, CraftType.OPTIONAL
    ),
    /**
     * Java menu order: template, base, addition, result.
     */
    SMITHING_TABLE(
            new int[]{53, 51, 52, 50},
            new ContainerEnumName[]{SmithingTableTemplateContainer, SmithingTableInputContainer, SmithingTableMaterialContainer, SmithingTableResultPreviewContainer},
            3, CraftType.RECIPE
    ),
    BEACON(
            new int[]{27},
            new ContainerEnumName[]{BeaconPaymentContainer}
    ),
    TRADE(
            new int[]{4, 5, 50},
            new ContainerEnumName[]{Trade2Ingredient1Container, Trade2Ingredient2Container, Trade2ResultPreviewContainer},
            2, CraftType.TRADE
    ),
    ;

    private final int[] uiSlots;
    private final ContainerEnumName[] slotNames;
    private final int resultSlot;
    private final CraftType craftType;

    UiContainerLayout(final int[] uiSlots, final ContainerEnumName[] slotNames) {
        this(uiSlots, slotNames, -1, CraftType.NONE);
    }

    UiContainerLayout(final int[] uiSlots, final ContainerEnumName[] slotNames, final int resultSlot, final CraftType craftType) {
        if (uiSlots.length != slotNames.length) {
            throw new IllegalArgumentException("Slot arrays of " + this.name() + " have different lengths");
        }
        this.uiSlots = uiSlots;
        this.slotNames = slotNames;
        this.resultSlot = resultSlot;
        this.craftType = craftType;
    }

    /**
     * @return The Java menu slot holding the crafted item or -1 if this menu doesn't craft anything
     */
    public int resultSlot() {
        return this.resultSlot;
    }

    /**
     * @return The action the server expects before the crafted item may be taken
     */
    public CraftType craftType() {
        return this.craftType;
    }

    /**
     * Bedrock Edition doesn't let the client simply take an item out of a result slot. Every menu has its own way of
     * telling the server what should be crafted.
     */
    public enum CraftType {

        /**
         * Taking the result is not supported.
         */
        NONE,
        /**
         * The client names the recipe it wants to craft.
         */
        RECIPE,
        /**
         * The client asks for whatever the menu produces, optionally with a text it entered.
         */
        OPTIONAL,
        /**
         * The grindstone additionally reports the repair cost.
         */
        REPAIR_AND_DISENCHANT,
        /**
         * The loom names the banner pattern instead of a recipe.
         */
        LOOM,
        /**
         * The villager trade the player picked is named like a recipe.
         */
        TRADE,
        ;

    }

    public int size() {
        return this.uiSlots.length;
    }

    /**
     * @param javaSlot The Java menu slot
     * @return The slot of the player UI container holding that slot's item
     */
    public int uiSlot(final int javaSlot) {
        return this.uiSlots[javaSlot];
    }

    public ContainerEnumName slotName(final int javaSlot) {
        return this.slotNames[javaSlot];
    }

    /**
     * @param uiSlot The slot of the player UI container
     * @return The Java menu slot showing that item or -1 if this menu doesn't use the slot
     */
    public int javaSlot(final int uiSlot) {
        for (int javaSlot = 0; javaSlot < this.uiSlots.length; javaSlot++) {
            if (this.uiSlots[javaSlot] == uiSlot) {
                return javaSlot;
            }
        }
        return -1;
    }

}
