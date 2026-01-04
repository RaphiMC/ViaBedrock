/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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

import com.viaversion.nbt.io.TagRegistry;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.api.io.NetworkByteBufInputStream;
import net.raphimc.viabedrock.api.io.NetworkByteBufOutputStream;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.io.IOException;

public class TagType extends Type<Tag> {

    public TagType() {
        super(Tag.class);
    }

    @Override
    public Tag read(ByteBuf buffer) {
        final byte id = buffer.readByte();
        if (id == 0) return null;

        try {
            BedrockTypes.STRING.read(buffer);
            return TagRegistry.read(id, new NetworkByteBufInputStream(buffer), TagLimiter.noop(), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(ByteBuf buffer, Tag value) {
        if (value == null) {
            buffer.writeByte(0);
            return;
        }

        buffer.writeByte(value.getTagId());
        BedrockTypes.STRING.write(buffer, "");
        try {
            value.write(new NetworkByteBufOutputStream(buffer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
