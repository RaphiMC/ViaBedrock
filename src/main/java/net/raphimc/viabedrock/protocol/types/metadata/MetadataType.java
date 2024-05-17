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

import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataTypeTemplate;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class MetadataType extends EntityDataTypeTemplate {

    @Override
    public EntityData read(ByteBuf buffer) {
        final int index = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        final EntityDataType type = MetaTypeBedrock.byId(BedrockTypes.UNSIGNED_VAR_INT.read(buffer));
        return new EntityData(index, type, type.type().read(buffer));
    }

    @Override
    public void write(ByteBuf buffer, EntityData value) {
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.id());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.dataType().typeId());
        value.dataType().type().write(buffer, value.value());
    }

}
