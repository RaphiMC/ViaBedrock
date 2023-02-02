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
package net.raphimc.viabedrock.protocol.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class VarLongType extends Type<Long> implements TypeConverter<Long> {

    public VarLongType() {
        super("VarLong", Long.class);
    }

    public long readPrimitive(final ByteBuf buffer) {
        final long l = Type.VAR_LONG.readPrimitive(buffer);
        return (l >>> 1) ^ -(l & 1);
    }

    public void writePrimitive(final ByteBuf buffer, long value) {
        Type.VAR_LONG.writePrimitive(buffer, (value << 1) ^ (value >> 63));
    }

    @Override
    public Long read(ByteBuf buffer) throws Exception {
        return this.readPrimitive(buffer);
    }

    @Override
    public void write(ByteBuf buffer, Long value) {
        this.writePrimitive(buffer, value);
    }

    @Override
    public Long from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        } else if (o instanceof Boolean) {
            return ((Boolean) o) ? 1L : 0L;
        }
        return (Long) o;
    }

}
