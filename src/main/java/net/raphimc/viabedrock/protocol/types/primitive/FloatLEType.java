/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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

public class FloatLEType extends Type<Float> implements TypeConverter<Float> {

    public FloatLEType() {
        super("FloatLE", Float.class);
    }

    public float readPrimitive(final ByteBuf buffer) {
        return buffer.readFloatLE();
    }

    public void writePrimitive(final ByteBuf buffer, final float value) {
        buffer.writeFloatLE(value);
    }

    @Override
    public Float read(ByteBuf buffer) {
        return this.readPrimitive(buffer);
    }

    @Override
    public void write(ByteBuf buffer, Float value) {
        this.writePrimitive(buffer, value);
    }

    @Override
    public Float from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else if (o instanceof Boolean) {
            return ((Boolean) o) ? 1F : 0F;
        }
        return (Float) o;
    }

}
