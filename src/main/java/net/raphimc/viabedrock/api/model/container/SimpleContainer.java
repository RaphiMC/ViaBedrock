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
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;

/**
 * A generic container backed by a Bedrock container with an identity slot mapping
 * (plus an optional constant slot offset for menus whose Java layout has a leading result slot).
 */
public class SimpleContainer extends Container {

    private final int slotOffset;

    public SimpleContainer(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final int size, final int slotOffset, final String... validBlockTags) {
        super(user, containerId, type, title, position, size, validBlockTags);
        this.slotOffset = slotOffset;
    }

    public SimpleContainer(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final int size, final String... validBlockTags) {
        this(user, containerId, type, title, position, size, 0, validBlockTags);
    }

    @Override
    public int javaSlot(final int slot) {
        return slot + this.slotOffset;
    }

    @Override
    public int bedrockSlot(final int javaSlot) {
        return javaSlot - this.slotOffset;
    }

}
