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
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.HashMap;
import java.util.Map;

public class FloatPropertiesType extends Type<Map<Integer, Float>> {

    public FloatPropertiesType() {
        super("FloatProperties", Map.class);
    }

    @Override
    public Map<Integer, Float> read(ByteBuf buffer) {
        final int length = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
        final Map<Integer, Float> properties = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            final int index = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
            final float value = BedrockTypes.FLOAT_LE.readPrimitive(buffer);
            properties.put(index, value);
        }
        return properties;
    }

    @Override
    public void write(ByteBuf buffer, Map<Integer, Float> value) {
        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.size());
        value.forEach((i, v) -> {
            BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, i);
            BedrockTypes.FLOAT_LE.writePrimitive(buffer, v);
        });
    }

}
