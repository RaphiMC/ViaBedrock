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
package net.raphimc.viabedrock.api.model.container.block;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.model.FullContainerName;

public class BrewingStandContainer extends Container {

    public BrewingStandContainer(UserConnection user, byte containerId, TextComponent title, BlockPosition position) {
        super(user, containerId, ContainerType.BREWING_STAND, title, position, 5, "brewing_stand");
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        switch (slot) {
            case 0 -> {
                return new FullContainerName(ContainerEnumName.BrewingStandFuelContainer, null);
            }
            case 1, 2, 3 -> {
                return new FullContainerName(ContainerEnumName.BrewingStandResultContainer, null);
            }
            case 4 -> {
                return new FullContainerName(ContainerEnumName.BrewingStandInputContainer, null);
            }
            default -> {
                ViaBedrock.getPlatform().getLogger().warning("Invalid slot: " + slot);
                return new FullContainerName(ContainerEnumName.BrewingStandResultContainer, null);
            }
        }
    }

    @Override
    public int javaSlot(final int slot) {
        switch (slot) {
            case 0 -> {
                return 3;
            }
            case 1, 2, 3 -> {
                return slot - 1;
            }
            case 4 -> {
                return 4;
            }
            default -> {
                return super.javaSlot(slot);
            }
        }
    }

    @Override
    public int bedrockSlot(final int slot) {
        switch (slot) {
            case 3 -> {
                return 0;
            }
            case 0, 1, 2 -> {
                return slot + 1;
            }
            case 4 -> {
                return 4;
            }
            default -> {
                return super.bedrockSlot(slot);
            }
        }
    }

    @Override
    public short translateContainerData(int containerData) {
        return switch (containerData) {
            case 0 -> 0; // Progress arrow
            case 1 -> 1; // Fuel progress
            default -> -1; // Unknown
        };
    }
}
