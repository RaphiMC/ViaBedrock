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

import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.DataItemType;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public enum EntityDataTypesBedrock implements EntityDataType {

    BYTE(DataItemType.Byte, Types.BYTE),
    SHORT(DataItemType.Short, BedrockTypes.SHORT_LE),
    INT(DataItemType.Int, BedrockTypes.VAR_INT),
    FLOAT(DataItemType.Float, BedrockTypes.FLOAT_LE),
    STRING(DataItemType.String, BedrockTypes.STRING),
    TAG(DataItemType.CompoundTag, BedrockTypes.NETWORK_TAG),
    POSITION_3I(DataItemType.Pos, BedrockTypes.POSITION_3I),
    LONG(DataItemType.Int64, BedrockTypes.VAR_LONG),
    POSITION_3F(DataItemType.Vec3, BedrockTypes.POSITION_3F);

    private final DataItemType dataItemType;
    private final Type<?> type;

    EntityDataTypesBedrock(final DataItemType dataItemType, final Type<?> type) {
        this.dataItemType = dataItemType;
        this.type = type;
    }

    public static EntityDataTypesBedrock byDataItemType(final DataItemType dataItemType) {
        return values()[dataItemType.getValue()];
    }

    public DataItemType dataItemType() {
        return this.dataItemType;
    }

    @Override
    public int typeId() {
        return this.dataItemType.getValue();
    }

    @Override
    public Type<?> type() {
        return this.type;
    }

}
