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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.data.generated.bedrock.CustomBlockTags;

/**
 * The crafter is the only menu where Java Edition puts the player inventory before the last container slot, because its
 * result slot is appended after it.
 *
 * <p>The 3x3 grid is sent through the crafter's own container, while the preview of what it would craft comes through
 * the player UI container.</p>
 */
public class CrafterContainer extends MenuContainer {

    private static final int GRID_SIZE = 9;
    private static final int JAVA_SIZE = 46;
    private static final int JAVA_RESULT_SLOT = 45;
    /**
     * The slot of the player UI container which holds the crafter's result preview.
     */
    private static final int UI_RESULT_SLOT = 50;

    public CrafterContainer(final UserConnection user, final byte containerId, final TextComponent title, final BlockPosition position) {
        super(user, containerId, ContainerType.CRAFTER, title, position, GRID_SIZE, CustomBlockTags.CRAFTER);
    }

    @Override
    public ContainerEnumName bedrockContainerName(final int slot) {
        return ContainerEnumName.LevelEntityContainer;
    }

    @Override
    protected int playerInventoryStart() {
        return GRID_SIZE;
    }

    @Override
    public int javaSize() {
        return JAVA_SIZE;
    }

    @Override
    public int javaSlotForUiSlot(final int uiSlot) {
        return uiSlot == UI_RESULT_SLOT ? JAVA_RESULT_SLOT : -1;
    }

    @Override
    public boolean isIgnoredJavaSlot(final int javaSlot) {
        // The result slot only previews what the crafter would craft, the Java client can't take it out
        return javaSlot == JAVA_RESULT_SLOT;
    }

}
