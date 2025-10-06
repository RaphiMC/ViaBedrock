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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.nbt.io.TagRegistry;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.api.io.NetworkByteBufInputStream;
import net.raphimc.viabedrock.api.io.NetworkByteBufOutputStream;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.Tag_Type;

import java.io.IOException;

public class TagValueType extends Type<Tag> {

    private final Tag_Type tagType;

    public TagValueType(final Tag_Type tagType) {
        super(Tag.class);
        this.tagType = tagType;
    }

    @Override
    public Tag read(ByteBuf buffer) {
        try {
            return TagRegistry.read(this.tagType.getValue(), new NetworkByteBufInputStream(buffer), TagLimiter.noop(), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(ByteBuf buffer, Tag value) {
        if (value == null) {
            throw new IllegalArgumentException("Tag value cannot be null");
        } else if (value.getTagId() != this.tagType.getValue()) {
            throw new IllegalArgumentException("Tag value must be of type " + this.tagType);
        }

        try {
            value.write(new NetworkByteBufOutputStream(buffer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
