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
package net.raphimc.viabedrock.protocol.types.metadata;

import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.types.metadata.MetaTypeTemplate;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class MetadataType extends MetaTypeTemplate {

    @Override
    public Metadata read(ByteBuf buffer) throws Exception {
        final int index = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        final MetaType type = MetaTypeBedrock.byId(BedrockTypes.UNSIGNED_VAR_INT.read(buffer));
        return new Metadata(index, type, type.type().read(buffer));
    }

    @Override
    public void write(ByteBuf buffer, Metadata value) throws Exception {
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.id());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.metaType().typeId());
        value.metaType().type().write(buffer, value.value());
    }

}
