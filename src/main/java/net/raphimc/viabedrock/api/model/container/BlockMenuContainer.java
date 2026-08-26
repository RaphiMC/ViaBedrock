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

/**
 * A menu whose contents the server sends with a dedicated container id, like chests, furnaces or hoppers.
 *
 * <p>The slots of this container are indexed the way Bedrock Edition sends them, which is not always the order the
 * Java Edition menu expects them in.</p>
 */
public class BlockMenuContainer extends MenuContainer {

    private final ContainerEnumName[] slotNames;
    private final int[] javaSlots;

    /**
     * @param slotNames The Bedrock container name of every slot, indexed by Bedrock slot
     * @param javaSlots The Java menu slot of every slot, indexed by Bedrock slot, or null if both use the same order
     */
    public BlockMenuContainer(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final ContainerEnumName[] slotNames, final int[] javaSlots, final String... validBlockTags) {
        super(user, containerId, type, title, position, slotNames.length, validBlockTags);

        this.slotNames = slotNames;
        this.javaSlots = javaSlots;
    }

    /**
     * Creates a container whose slots all share the same Bedrock container name and use the same order as the Java menu.
     */
    public BlockMenuContainer(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final int size, final ContainerEnumName slotName, final String... validBlockTags) {
        this(user, containerId, type, title, position, repeat(slotName, size), null, validBlockTags);
    }

    @Override
    public ContainerEnumName bedrockContainerName(final int slot) {
        return this.slotNames[slot];
    }

    @Override
    public int javaSlot(final int slot) {
        return this.javaSlots != null ? this.javaSlots[slot] : slot;
    }

    private static ContainerEnumName[] repeat(final ContainerEnumName slotName, final int size) {
        final ContainerEnumName[] slotNames = new ContainerEnumName[size];
        java.util.Arrays.fill(slotNames, slotName);
        return slotNames;
    }

}
