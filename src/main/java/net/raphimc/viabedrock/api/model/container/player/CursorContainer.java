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
package net.raphimc.viabedrock.api.model.container.player;

import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;

public class CursorContainer extends InventorySubContainer {

    public CursorContainer(final UserConnection user) {
        super(user, (byte) ContainerID.CONTAINER_ID_NONE.getValue(), ContainerType.NONE, 1);
    }

    public FullContainerName getFullContainerName() {
        return this.getFullContainerName(0);
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        return new FullContainerName(ContainerEnumName.CursorContainer, null);
    }

    public void setItem(BedrockItem item) {
        super.setItem(0, item);
    }

    public BedrockItem getCursorItem() {
        return this.getItem(0);
    }

    @Override
    public int javaSlot(final int slot) {
        return -1;
    }

    @Override
    public int bedrockSlot(final int slot) {
        return 0;
    }
}
