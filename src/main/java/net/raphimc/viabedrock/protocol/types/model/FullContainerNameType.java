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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.model.FullContainerName;

public class FullContainerNameType extends Type<FullContainerName> {

    public FullContainerNameType() {
        super(FullContainerName.class);
    }

    @Override
    public FullContainerName read(ByteBuf buffer) {
        return new FullContainerName(ContainerEnumName.getByValue(buffer.readByte()), buffer.readBoolean() ? buffer.readIntLE() : null);
    }

    @Override
    public void write(ByteBuf buffer, FullContainerName value) {
        buffer.writeByte(value.name().getValue());
        buffer.writeBoolean(value.dynamicId() != null);
        if (value.dynamicId() != null) {
            buffer.writeIntLE(value.dynamicId());
        }
    }

}
