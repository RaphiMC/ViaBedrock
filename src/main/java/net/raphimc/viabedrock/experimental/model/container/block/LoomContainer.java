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
package net.raphimc.viabedrock.experimental.model.container.block;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.experimental.model.container.ExperimentalContainer;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;

public class LoomContainer extends ExperimentalContainer {

    public LoomContainer(final UserConnection user, final byte containerId, final TextComponent title, final BlockPosition position) {
        super(user, containerId, ContainerType.LOOM, title, position, 4, "loom");
    }

    @Override
    public FullContainerName getFullContainerName(final int slot) {
        return switch (slot) {
            case 9 -> new FullContainerName(ContainerEnumName.LoomInputContainer, null);
            case 10 -> new FullContainerName(ContainerEnumName.LoomDyeContainer, null);
            case 11 -> new FullContainerName(ContainerEnumName.LoomMaterialContainer, null);
            case 50 -> new FullContainerName(ContainerEnumName.CreatedOutputContainer, null);
            default -> throw new IllegalArgumentException("Invalid slot for Loom Container: " + slot);
        };
    }

    @Override
    public int javaSlot(final int slot) {
        return switch (slot) {
            case 9 -> 0;
            case 10 -> 1;
            case 11 -> 2;
            case 50 -> 3;
            default -> super.javaSlot(slot);
        };
    }

    @Override
    public int bedrockSlot(final int slot) {
        return switch (slot) {
            case 0 -> 9;
            case 1 -> 10;
            case 2 -> 11;
            case 3 -> 50;
            default -> super.bedrockSlot(slot);
        };
    }

    @Override
    public BedrockItem getItem(final int bedrockSlot) {
        return switch (bedrockSlot) {
            case 9 -> super.getItem(0);
            case 10 -> super.getItem(1);
            case 11 -> super.getItem(2);
            case 50 -> super.getItem(3);
            default -> throw new IllegalArgumentException("Invalid slot for Loom Container: " + bedrockSlot);
        };
    }

    @Override
    public boolean setItem(final int bedrockSlot, final BedrockItem item) {
        return switch (bedrockSlot) {
            case 9 -> super.setItem(0, item);
            case 10 -> super.setItem(1, item);
            case 11 -> super.setItem(2, item);
            case 50 -> super.setItem(3, item);
            default -> throw new IllegalArgumentException("Invalid slot for Loom Container: " + bedrockSlot);
        };
    }

}
