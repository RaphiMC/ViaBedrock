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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class ShortLEType extends Type<Short> implements TypeConverter<Short> {

    public ShortLEType() {
        super("ShortLE", Short.class);
    }

    public short readPrimitive(final ByteBuf buffer) {
        return buffer.readShortLE();
    }

    public void writePrimitive(final ByteBuf buffer, final short value) {
        buffer.writeShortLE(value);
    }

    @Override
    public Short read(ByteBuf buffer) {
        return this.readPrimitive(buffer);
    }

    @Override
    public void write(ByteBuf buffer, Short value) {
        this.writePrimitive(buffer, value);
    }

    @Override
    public Short from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).shortValue();
        } else if (o instanceof Boolean) {
            return (short) (((Boolean) o) ? 1 : 0);
        }
        return (Short) o;
    }

}
