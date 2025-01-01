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
package net.raphimc.viabedrock.protocol.types.entitydata;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.EntityProperties;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.HashMap;
import java.util.Map;

public class EntityPropertiesType extends Type<EntityProperties> {

    public EntityPropertiesType() {
        super("EntityProperties", EntityProperties.class);
    }

    @Override
    public EntityProperties read(ByteBuf buffer) {
        final int intPropertiesLength = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
        final Int2IntMap intProperties = new Int2IntOpenHashMap(intPropertiesLength);
        for (int i = 0; i < intPropertiesLength; i++) {
            final int index = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
            final int value = BedrockTypes.VAR_INT.readPrimitive(buffer);
            intProperties.put(index, value);
        }
        final int floatPropertiesLength = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
        final Map<Integer, Float> floatProperties = new HashMap<>(floatPropertiesLength);
        for (int i = 0; i < floatPropertiesLength; i++) {
            final int index = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
            final float value = BedrockTypes.FLOAT_LE.readPrimitive(buffer);
            floatProperties.put(index, value);
        }
        return new EntityProperties(intProperties, floatProperties);
    }

    @Override
    public void write(ByteBuf buffer, EntityProperties value) {
        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.intProperties().size());
        value.intProperties().forEach((i, v) -> {
            BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, i);
            BedrockTypes.VAR_INT.writePrimitive(buffer, v);
        });
        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.floatProperties().size());
        value.floatProperties().forEach((i, v) -> {
            BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, i);
            BedrockTypes.FLOAT_LE.writePrimitive(buffer, v);
        });
    }

}
