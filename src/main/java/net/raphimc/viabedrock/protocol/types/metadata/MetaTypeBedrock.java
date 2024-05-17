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

import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public enum MetaTypeBedrock implements EntityDataType {

    BYTE(0, Types.BYTE),
    SHORT(1, BedrockTypes.SHORT_LE),
    INT(2, BedrockTypes.VAR_INT),
    FLOAT(3, BedrockTypes.FLOAT_LE),
    STRING(4, BedrockTypes.STRING),
    TAG(5, BedrockTypes.NETWORK_TAG),
    POSITION_3I(6, BedrockTypes.POSITION_3I),
    LONG(7, BedrockTypes.VAR_LONG),
    POSITION_3F(8, BedrockTypes.POSITION_3F);

    private final int typeID;
    private final Type<?> type;

    MetaTypeBedrock(final int typeID, final Type<?> type) {
        this.typeID = typeID;
        this.type = type;
    }

    public static MetaTypeBedrock byId(final int id) {
        return values()[id];
    }

    public int typeId() {
        return this.typeID;
    }

    public Type<?> type() {
        return this.type;
    }

}
