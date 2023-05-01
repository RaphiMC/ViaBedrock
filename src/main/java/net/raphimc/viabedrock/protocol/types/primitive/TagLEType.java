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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.TagRegistry;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.api.io.LittleEndianByteBufInputStream;
import net.raphimc.viabedrock.api.io.LittleEndianByteBufOutputStream;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class TagLEType extends Type<Tag> {

    public TagLEType() {
        super(Tag.class);
    }

    @Override
    public Tag read(ByteBuf buffer) throws Exception {
        final short id = buffer.readUnsignedByte();
        BedrockTypes.UTF8_STRING.read(buffer);
        if (id == 0) return null;

        final Tag tag = TagRegistry.createInstance(id);
        tag.read(new LittleEndianByteBufInputStream(buffer));

        return tag;
    }

    @Override
    public void write(ByteBuf buffer, Tag value) throws Exception {
        if (value == null) {
            buffer.writeByte(0);
            BedrockTypes.UTF8_STRING.write(buffer, "");
            return;
        }

        buffer.writeByte(value.getTagId());
        BedrockTypes.UTF8_STRING.write(buffer, "");
        value.write(new LittleEndianByteBufOutputStream(buffer));
    }

}
