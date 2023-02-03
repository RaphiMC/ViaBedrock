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

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.TagRegistry;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.raphimc.viabedrock.api.BedrockDataInput;
import net.raphimc.viabedrock.api.BedrockDataOutput;

import java.io.DataInput;
import java.io.DataOutput;

public class NBTType extends Type<Tag> {

    protected NBTType() {
        super(Tag.class);
    }

    @Override
    public Tag read(ByteBuf buffer) throws Exception {
        final DataInput in = new BedrockDataInput(new LittleEndianDataInputStream(new ByteBufInputStream(buffer)));
        final int tagId = in.readByte();
        in.readUTF();
        final Tag tag = TagRegistry.createInstance(tagId);
        tag.read(in);
        return tag;
    }

    @Override
    public void write(ByteBuf buffer, Tag value) throws Exception {
        final DataOutput out = new BedrockDataOutput(new LittleEndianDataOutputStream(new ByteBufOutputStream(buffer)));
        out.writeByte(value.getTagId());
        out.writeUTF("");
        value.write(out);
    }
}
