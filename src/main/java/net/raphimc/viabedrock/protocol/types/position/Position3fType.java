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
package net.raphimc.viabedrock.protocol.types.position;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.Position3f;

public class Position3fType extends Type<Position3f> {

    public Position3fType() {
        super("Position3f", Position3f.class);
    }

    @Override
    public Position3f read(ByteBuf buffer) throws Exception {
        final float x = buffer.readFloatLE();
        final float y = buffer.readFloatLE();
        final float z = buffer.readFloatLE();

        return new Position3f(x, y, z);
    }

    @Override
    public void write(ByteBuf buffer, Position3f value) {
        buffer.writeFloatLE(value.x());
        buffer.writeFloatLE(value.y());
        buffer.writeFloatLE(value.z());
    }

}
