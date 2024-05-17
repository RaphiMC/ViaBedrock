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

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class BlockPositionType extends Type<Position> {

    public BlockPositionType() {
        super("BlockPosition", Position.class);
    }

    @Override
    public Position read(ByteBuf buffer) {
        final int x = BedrockTypes.VAR_INT.readPrimitive(buffer);
        final int y = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
        final int z = BedrockTypes.VAR_INT.readPrimitive(buffer);

        return new Position(x, y, z);
    }

    @Override
    public void write(ByteBuf buffer, Position value) {
        BedrockTypes.VAR_INT.writePrimitive(buffer, value.x());
        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.y());
        BedrockTypes.VAR_INT.writePrimitive(buffer, value.z());
    }

}
