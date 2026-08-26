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
package net.raphimc.viabedrock.api.model.container.player;

import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.viabedrock.api.model.container.MenuContainer;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

public class HudContainer extends InventoryRedirectContainer {

    /**
     * The slot holding the item the player is currently dragging around.
     */
    public static final int CURSOR = 0;
    /**
     * The first slot of the 2x2 crafting grid of the player inventory screen.
     */
    public static final int CRAFTING_INPUT_START = 28;
    /**
     * The last slot of the 2x2 crafting grid of the player inventory screen.
     */
    public static final int CRAFTING_INPUT_END = 31;
    /**
     * The slot holding the result of the currently previewed crafting recipe.
     */
    public static final int CRAFTING_RESULT = 50;

    public HudContainer(final UserConnection user) {
        super(user, (byte) ContainerID.CONTAINER_ID_PLAYER_ONLY_UI.getValue(), ContainerType.HUD, 54);
    }

    @Override
    public boolean setItem(final int slot, final BedrockItem item) {
        if (!super.setItem(slot, item)) {
            return false;
        }
        if (slot == CURSOR) { // The cursor item is sent as a dedicated packet and is valid for every open screen
            return true;
        }

        final MenuContainer menuContainer = this.getMenuContainer();
        if (menuContainer != null) { // Menus like the anvil or the enchanting table keep their items in this container
            return menuContainer.javaSlotForUiSlot(slot) != -1;
        }
        // The crafting grid and its result only exist in the player inventory screen
        return (slot >= CRAFTING_INPUT_START && slot <= CRAFTING_INPUT_END) || slot == CRAFTING_RESULT;
    }

    @Override
    public int javaSlot(final int slot) {
        final MenuContainer menuContainer = this.getMenuContainer();
        if (menuContainer != null) {
            final int javaSlot = menuContainer.javaSlotForUiSlot(slot);
            if (javaSlot != -1) {
                return javaSlot;
            }
        }

        if (slot >= CRAFTING_INPUT_START && slot <= CRAFTING_INPUT_END) {
            return slot - (CRAFTING_INPUT_START - 1);
        } else if (slot == CRAFTING_RESULT) {
            return 0;
        } else {
            return super.javaSlot(slot);
        }
    }

    /**
     * @return The currently open menu, if there is one
     */
    private MenuContainer getMenuContainer() {
        return this.user.get(InventoryTracker.class).getCurrentMenuContainer();
    }

    @Override
    public ContainerEnumName bedrockContainerName(final int slot) {
        final MenuContainer menuContainer = this.getMenuContainer();
        if (menuContainer != null) {
            final int javaSlot = menuContainer.javaSlotForUiSlot(slot);
            if (javaSlot != -1) {
                return menuContainer.bedrockContainerName(javaSlot);
            }
        }

        if (slot == CURSOR) {
            return ContainerEnumName.CursorContainer;
        } else if (slot >= CRAFTING_INPUT_START && slot <= CRAFTING_INPUT_END) {
            return ContainerEnumName.CraftingInputContainer;
        } else if (slot == CRAFTING_RESULT) {
            return ContainerEnumName.CreatedOutputContainer;
        } else {
            return null; // The remaining slots of the UI container are not addressable
        }
    }

}
