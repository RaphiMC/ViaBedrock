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
package net.raphimc.viabedrock.protocol.model;

import com.viaversion.viaversion.api.minecraft.Position;

public class BlockChangeEntry {

    private final Position position;
    private final int blockState;
    private final int flags;
    private final long messageEntityId;
    private final int messageType;

    public BlockChangeEntry(final Position position, final int blockState, final int flags, final long messageEntityId, final int messageType) {
        this.position = position;
        this.blockState = blockState;
        this.flags = flags;
        this.messageEntityId = messageEntityId;
        this.messageType = messageType;
    }

    public Position position() {
        return this.position;
    }

    public int blockState() {
        return this.blockState;
    }

    public int flags() {
        return this.flags;
    }

    public long messageEntityId() {
        return this.messageEntityId;
    }

    public int messageType() {
        return this.messageType;
    }

}
