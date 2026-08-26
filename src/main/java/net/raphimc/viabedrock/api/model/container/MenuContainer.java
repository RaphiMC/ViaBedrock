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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

/**
 * A container which is displayed as a dedicated screen on the Java client. Java menus always contain the player
 * inventory after the container specific slots, so those slots have to be filled in as well.
 */
public abstract class MenuContainer extends Container {

    /**
     * The amount of player inventory slots (27 main inventory slots + 9 hotbar slots) which are appended to every Java menu.
     */
    public static final int PLAYER_INVENTORY_SIZE = 36;

    public MenuContainer(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final int size, final String... validBlockTags) {
        super(user, containerId, type, title, position, size, validBlockTags);
    }

    @Override
    public Item[] getJavaItems() {
        final Item[] javaItems = StructuredItem.emptyArray(this.javaSize());
        for (int slot = 0; slot < this.size(); slot++) {
            final int javaSlot = this.javaSlot(slot);
            if (javaSlot >= 0 && javaSlot < javaItems.length) {
                javaItems[javaSlot] = this.getJavaItem(slot);
            }
        }

        final InventoryContainer inventoryContainer = this.user.get(InventoryTracker.class).getInventoryContainer();
        final int playerInventoryStart = this.playerInventoryStart();
        for (int i = 0; i < 27; i++) { // Main inventory
            javaItems[playerInventoryStart + i] = inventoryContainer.getJavaItem(9 + i);
        }
        for (int i = 0; i < 9; i++) { // Hotbar
            javaItems[playerInventoryStart + 27 + i] = inventoryContainer.getJavaItem(i);
        }
        return javaItems;
    }

    /**
     * @return The Java menu slot the player inventory starts at
     */
    protected int playerInventoryStart() {
        return this.size();
    }

    /**
     * Maps a slot of the player inventory container to the corresponding slot in this menu.
     *
     * @param inventorySlot The Bedrock player inventory slot (0-8 hotbar, 9-35 main inventory)
     * @return The Java menu slot
     */
    public int javaPlayerInventorySlot(final int inventorySlot) {
        if (inventorySlot < 9) { // Hotbar
            return this.playerInventoryStart() + 27 + inventorySlot;
        } else { // Main inventory
            return this.playerInventoryStart() + (inventorySlot - 9);
        }
    }

    /**
     * Maps a slot of this menu back to the corresponding slot of the player inventory container.
     *
     * @param javaSlot The Java menu slot
     * @return The Bedrock player inventory slot or -1 if the slot doesn't belong to the player inventory
     */
    public int bedrockPlayerInventorySlot(final int javaSlot) {
        final int relativeSlot = javaSlot - this.playerInventoryStart();
        if (relativeSlot < 0 || relativeSlot >= PLAYER_INVENTORY_SIZE) {
            return -1;
        } else if (relativeSlot < 27) { // Main inventory
            return 9 + relativeSlot;
        } else { // Hotbar
            return relativeSlot - 27;
        }
    }

    /**
     * Some menus keep part or all of their contents in the player UI container instead of a container of their own.
     *
     * @param uiSlot The slot of the player UI container
     * @return The Java menu slot showing that item or -1 if this menu doesn't use the slot
     */
    public int javaSlotForUiSlot(final int uiSlot) {
        return -1;
    }

    /**
     * @param javaSlot The Java menu slot
     * @return Whether the Java client may report changes to this slot which don't correspond to anything on Bedrock
     */
    public boolean isIgnoredJavaSlot(final int javaSlot) {
        return false;
    }

    /**
     * @param javaSlot The Java menu slot
     * @return Whether the given Java menu slot belongs to this container instead of the player inventory
     */
    public boolean isContainerSlot(final int javaSlot) {
        return javaSlot >= 0 && javaSlot < this.javaSize() && this.bedrockPlayerInventorySlot(javaSlot) == -1;
    }

    /**
     * Maps a Java menu slot of this container back to the Bedrock slot it belongs to.
     *
     * @param javaSlot The Java menu slot
     * @return The Bedrock slot or -1 if this container doesn't have that slot
     */
    public int bedrockSlot(final int javaSlot) {
        for (int slot = 0; slot < this.size(); slot++) {
            if (this.javaSlot(slot) == javaSlot) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    public int javaSize() {
        return this.playerInventoryStart() + PLAYER_INVENTORY_SIZE;
    }

}
