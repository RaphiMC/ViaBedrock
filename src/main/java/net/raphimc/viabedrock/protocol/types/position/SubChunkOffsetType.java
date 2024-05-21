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
package net.raphimc.viabedrock.protocol.types.position;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class SubChunkOffsetType extends Type<BlockPosition> {

    public SubChunkOffsetType() {
        super("SubChunkOffset", BlockPosition.class);
    }

    @Override
    public BlockPosition read(ByteBuf buffer) {
        final int x = Types.BYTE.readPrimitive(buffer);
        final int y = Types.BYTE.readPrimitive(buffer);
        final int z = Types.BYTE.readPrimitive(buffer);

        return new BlockPosition(x, y, z);
    }

    @Override
    public void write(ByteBuf buffer, BlockPosition value) {
        Types.BYTE.writePrimitive(buffer, (byte) value.x());
        Types.BYTE.writePrimitive(buffer, (byte) value.y());
        Types.BYTE.writePrimitive(buffer, (byte) value.z());
    }

}
