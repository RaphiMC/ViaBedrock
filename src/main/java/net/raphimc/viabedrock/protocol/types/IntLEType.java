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

public class IntLEType extends Type<Integer> implements TypeConverter<Integer> {

    public IntLEType() {
        super("IntLE", Integer.class);
    }

    public int readPrimitive(final ByteBuf buffer) {
        return buffer.readIntLE();
    }

    public void writePrimitive(final ByteBuf buffer, final int value) {
        buffer.writeIntLE(value);
    }

    @Override
    public Integer read(ByteBuf buffer) {
        return this.readPrimitive(buffer);
    }

    @Override
    public void write(ByteBuf buffer, Integer value) {
        this.writePrimitive(buffer, value);
    }

    @Override
    public Integer from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        return (Integer) o;
    }

}
