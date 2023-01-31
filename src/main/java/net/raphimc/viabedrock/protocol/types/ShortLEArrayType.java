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
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Array;

public class ShortLEArrayType<T> extends Type<T[]> {

    private final Type<T> elementType;

    public ShortLEArrayType(final Type<T> type) {
        super(type.getTypeName() + " ShortLEArray", (Class<T[]>) ArrayType.getArrayClass(type.getOutputClass()));

        this.elementType = type;
    }

    @Override
    public T[] read(ByteBuf buffer) throws Exception {
        final int length = BedrockTypes.UNSIGNED_SHORT_LE.readPrimitive(buffer);
        final T[] array = (T[]) Array.newInstance(this.elementType.getOutputClass(), length);

        for (int i = 0; i < length; i++) {
            array[i] = this.elementType.read(buffer);
        }
        return array;
    }

    @Override
    public void write(ByteBuf buffer, T[] value) throws Exception {
        BedrockTypes.UNSIGNED_SHORT_LE.writePrimitive(buffer, value.length);
        for (T v : value) {
            this.elementType.write(buffer, v);
        }
    }

}
