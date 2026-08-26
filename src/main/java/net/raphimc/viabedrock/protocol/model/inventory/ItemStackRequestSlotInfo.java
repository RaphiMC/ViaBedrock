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

import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.model.FullContainerName;

/**
 * Identifies a single slot within a Bedrock container for an {@link ItemStackRequest}.
 *
 * @param containerName    The container the slot belongs to
 * @param slot             The slot index within the container
 * @param stackNetworkId   The net id of the item which is currently expected to be in that slot
 */
public record ItemStackRequestSlotInfo(FullContainerName containerName, int slot, int stackNetworkId) {

    public ItemStackRequestSlotInfo(final ContainerEnumName containerName, final int slot, final int stackNetworkId) {
        this(new FullContainerName(containerName, null), slot, stackNetworkId);
    }

}
