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

import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestSlotInfo;

/**
 * A reference to a single slot of a specific container.
 *
 * <p>Containers are compared by identity, so two {@link ContainerSlot}s are only equal if they refer to the same slot
 * of the same container instance.</p>
 */
public record ContainerSlot(Container container, int slot) {

    public BedrockItem getItem() {
        return this.container.getItem(this.slot);
    }

    public void setItem(final BedrockItem item) {
        this.container.setItem(this.slot, item);
    }

    public ItemStackRequestSlotInfo requestSlotInfo() {
        return this.container.requestSlotInfo(this.slot);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ContainerSlot other)) return false;
        return this.slot == other.slot && this.container == other.container;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this.container) * 31 + this.slot;
    }

    @Override
    public String toString() {
        return this.container.type() + "[" + this.slot + "]";
    }

}
