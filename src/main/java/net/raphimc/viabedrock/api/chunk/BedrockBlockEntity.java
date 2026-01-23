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
package net.raphimc.viabedrock.api.chunk;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;

public class BedrockBlockEntity implements BlockEntity {

    private final BlockPosition position;
    private final CompoundTag tag;

    public BedrockBlockEntity(final CompoundTag tag) {
        this.tag = tag;

        int x = 0;
        if (tag.get("x") instanceof IntTag) {
            x = ((IntTag) tag.get("x")).asInt();
        }
        int y = 0;
        if (tag.get("y") instanceof IntTag) {
            y = ((IntTag) tag.get("y")).asInt();
        }
        int z = 0;
        if (tag.get("z") instanceof IntTag) {
            z = ((IntTag) tag.get("z")).asInt();
        }
        this.position = new BlockPosition(x, y, z);

        // id value should be validated, but not strictly required
    }

    public BedrockBlockEntity(final BlockPosition position, final CompoundTag tag) {
        this.position = position;
        this.tag = tag;
    }

    @Override
    public byte packedXZ() {
        return BlockEntity.pack(this.position.x() & 15, this.position.z() & 15);
    }

    @Override
    public short y() {
        return (short) this.position.y();
    }

    public BlockPosition position() {
        return this.position;
    }

    @Override
    public CompoundTag tag() {
        return this.tag;
    }

    @Override
    public int typeId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockEntity withTypeId(int typeId) {
        throw new UnsupportedOperationException();
    }

}
