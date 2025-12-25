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

import java.util.logging.Level;

public class FurnaceContainer extends Container {

    public FurnaceContainer(UserConnection user, byte containerId, TextComponent title, BlockPosition position) {
        super(user, containerId, ContainerType.FURNACE, title, position, 3, "furnace");
    }

    public FurnaceContainer(UserConnection user, byte containerId, ContainerType type, TextComponent title, BlockPosition position, String... validBlockTags) {
        super(user, containerId, type, title, position, 3, validBlockTags);
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        return switch (slot) {
            case 0 -> new FullContainerName(ContainerEnumName.FurnaceIngredientContainer, null);
            case 1 -> new FullContainerName(ContainerEnumName.FurnaceFuelContainer, null);
            case 2 -> new FullContainerName(ContainerEnumName.FurnaceResultContainer, null);
            default -> {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Invalid slot " + slot + " for furnace container");
                yield new FullContainerName(ContainerEnumName.LevelEntityContainer, null);
            }
        };
    }

}
