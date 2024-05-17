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
package net.raphimc.viabedrock.protocol.types.metadata;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class IntPropertiesType extends Type<Int2IntMap> {

    public IntPropertiesType() {
        super("IntProperties", Int2IntMap.class);
    }

    @Override
    public Int2IntMap read(ByteBuf buffer) {
        final int length = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
        final Int2IntMap properties = new Int2IntOpenHashMap(length);
        for (int i = 0; i < length; i++) {
            final int index = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
            final int value = BedrockTypes.VAR_INT.readPrimitive(buffer);
            properties.put(index, value);
        }
        return properties;
    }

    @Override
    public void write(ByteBuf buffer, Int2IntMap value) {
        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.size());
        value.forEach((i, v) -> {
            BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, i);
            BedrockTypes.VAR_INT.writePrimitive(buffer, v);
        });
    }

}
