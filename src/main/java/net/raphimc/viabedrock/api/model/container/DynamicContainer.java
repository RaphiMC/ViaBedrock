/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
import net.raphimc.viabedrock.api.model.container.player.InventoryRedirectContainer;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.model.FullContainerName;

public class DynamicContainer extends InventoryRedirectContainer {

    private final FullContainerName containerName;

    public DynamicContainer(final UserConnection user, final FullContainerName containerName) {
        super(user, (byte) ContainerID.CONTAINER_ID_REGISTRY.getValue(), ContainerType.NONE, 1000 /* Seems to be a hardcoded cap */);
        this.containerName = containerName;
    }

    @Override
    public int javaSlot(final int slot) {
        throw new UnsupportedOperationException();
    }

}
