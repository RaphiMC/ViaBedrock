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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.EntityLink;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class EntityLinkType extends Type<EntityLink> {

    public EntityLinkType() {
        super(EntityLink.class);
    }

    @Override
    public EntityLink read(ByteBuf buffer) throws Exception {
        return new EntityLink(BedrockTypes.VAR_LONG.read(buffer), BedrockTypes.VAR_LONG.read(buffer), buffer.readUnsignedByte(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void write(ByteBuf buffer, EntityLink value) throws Exception {
        BedrockTypes.VAR_LONG.write(buffer, value.from());
        BedrockTypes.VAR_LONG.write(buffer, value.to());
        buffer.writeByte(value.type());
        buffer.writeBoolean(value.immediate());
        buffer.writeBoolean(value.riderInitiated());
    }

}
