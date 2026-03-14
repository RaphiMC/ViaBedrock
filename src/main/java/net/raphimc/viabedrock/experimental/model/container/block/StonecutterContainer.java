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
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.experimental.model.container.ExperimentalContainer;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.data.generated.bedrock.CustomBlockTags;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;

import java.util.logging.Level;

public class StonecutterContainer extends ExperimentalContainer {

    public StonecutterContainer(UserConnection user, byte containerId, TextComponent title, BlockPosition position) {
        super(user, containerId, ContainerType.STONECUTTER, title, position, 2, "stonecutter_block", "stonecutter");
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        return switch (slot) {
            case 3 -> new FullContainerName(ContainerEnumName.StonecutterInputContainer, null);
            case 50 -> new FullContainerName(ContainerEnumName.StonecutterResultPreviewContainer, null); //TODO: CreatedOutputContainer?
            default -> throw new IllegalArgumentException("Invalid slot for Stonecutter Container: " + slot);
        };
    }

    @Override
    public int javaSlot(final int slot) {
        return switch (slot) {
            case 3 -> 0;
            case 50 -> 1;
            default -> super.javaSlot(slot);
        };
    }

    @Override
    public int bedrockSlot(final int slot) {
        return switch (slot) {
            case 0 -> 3;
            case 1 -> 50;
            default -> super.bedrockSlot(slot);
        };
    }

    @Override
    public BedrockItem getItem(int bedrockSlot) {
        if (bedrockSlot == 3) {
            return this.items[0];
        } else if (bedrockSlot == 50) {
            return this.items[1];
        } else {
            throw new IllegalArgumentException("Bedrock Slot out of bounds for stonecutter (getItem): " + bedrockSlot);
        }
    }

    @Override
    public boolean setItem(final int bedrockSlot, final BedrockItem item) {
        if (bedrockSlot == 3) {
            return super.setItem(0, item);
        } else if (bedrockSlot == 50) {
            return super.setItem(1, item);
        } else {
            throw new IllegalArgumentException("Bedrock Slot out of bounds for stonecutter (setItem): " + bedrockSlot);
        }
    }

}