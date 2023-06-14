/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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

import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;

public class BlockEntityWithBlockState implements BlockEntity {

    private BlockEntity blockEntity;
    private final Integer blockState;

    public BlockEntityWithBlockState(final BlockEntity blockEntity) {
        this(blockEntity, null);
    }

    public BlockEntityWithBlockState(final byte packedXZ, final short y, final Integer blockState) {
        this(new BlockEntityImpl(packedXZ, y, -1, null), blockState);
    }

    public BlockEntityWithBlockState(final BlockEntity blockEntity, final Integer blockState) {
        this.blockEntity = blockEntity;
        this.blockState = blockState;
    }

    @Override
    public byte packedXZ() {
        return this.blockEntity.packedXZ();
    }

    @Override
    public short y() {
        return this.blockEntity.y();
    }

    @Override
    public int typeId() {
        return this.blockEntity.typeId();
    }

    @Override
    public CompoundTag tag() {
        return this.blockEntity.tag();
    }

    @Override
    public BlockEntity withTypeId(final int typeId) {
        this.blockEntity = this.blockEntity.withTypeId(typeId);
        return this;
    }

    public boolean hasBlockState() {
        return this.blockState != null;
    }

    public int blockState() {
        if (!this.hasBlockState()) {
            throw new IllegalStateException("BlockState is not set!");
        }
        return this.blockState;
    }

}
