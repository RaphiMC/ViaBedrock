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

import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackNetResult;
import net.raphimc.viabedrock.protocol.model.FullContainerName;

import java.util.List;

/**
 * The server's answer to a single {@link ItemStackRequest}.
 *
 * <p>Note that the response only contains the resulting stack sizes and net ids, not the resulting items themselves.
 * The item types have to be predicted by the client.</p>
 */
public record ItemStackResponse(ItemStackNetResult result, int requestId, List<ContainerInfo> containerInfos) {

    public record ContainerInfo(FullContainerName containerName, List<SlotInfo> slots) {
    }

    public record SlotInfo(int slot, int hotbarSlot, int count, int stackNetworkId, String customName, String filteredCustomName, int durabilityCorrection) {
    }

}
