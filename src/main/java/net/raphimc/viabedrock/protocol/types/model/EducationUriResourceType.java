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
import net.raphimc.viabedrock.protocol.model.EducationUriResource;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class EducationUriResourceType extends Type<EducationUriResource> {

    public EducationUriResourceType() {
        super(EducationUriResource.class);
    }

    @Override
    public EducationUriResource read(ByteBuf buffer) {
        return new EducationUriResource(BedrockTypes.STRING.read(buffer), BedrockTypes.STRING.read(buffer));
    }

    @Override
    public void write(ByteBuf buffer, EducationUriResource value) {
        BedrockTypes.STRING.write(buffer, value.buttonName());
        BedrockTypes.STRING.write(buffer, value.linkUri());
    }

}
