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
package net.raphimc.viabedrock.api.inventory;

import net.raphimc.viabedrock.api.model.container.ContainerSlot;

/**
 * A protocol independent description of a single change the player made to their inventory. Depending on whether the
 * server uses client or server authoritative inventories, these are translated to either an InventoryTransaction or
 * an ItemStackRequest.
 */
public sealed interface InventoryOperation {

    /**
     * Moves {@code count} items from {@code source} to {@code destination}.
     */
    record Transfer(ContainerSlot source, ContainerSlot destination, int count) implements InventoryOperation {
    }

    /**
     * Exchanges the contents of two slots.
     */
    record Swap(ContainerSlot slot1, ContainerSlot slot2) implements InventoryOperation {
    }

    /**
     * Throws {@code count} items out of {@code source} into the world.
     */
    record Drop(ContainerSlot source, int count) implements InventoryOperation {
    }

    /**
     * Deletes {@code count} items from {@code source}. Only possible in creative mode.
     */
    record Destroy(ContainerSlot source, int count) implements InventoryOperation {
    }

}
