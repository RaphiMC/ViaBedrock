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
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.BlockProperties;

public class BlockPropertiesType extends Type<BlockProperties> {

    public BlockPropertiesType() {
        super(BlockProperties.class);
    }

    @Override
    public BlockProperties read(ByteBuf buffer) throws Exception {
        return new BlockProperties(BedrockTypes.STRING.read(buffer), (CompoundTag) BedrockTypes.TAG.read(buffer));
    }

    @Override
    public void write(ByteBuf buffer, BlockProperties value) throws Exception {
        BedrockTypes.STRING.write(buffer, value.name());
        BedrockTypes.TAG.write(buffer, value.properties());
    }

}
