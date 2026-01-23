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
package net.raphimc.viabedrock.api.util;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.TreeMap;

public class BlockStateHasher {

    public static int hash(final CompoundTag blockStateTag) {
        final String name = Key.namespaced(blockStateTag.getStringTag("name").getValue());
        if (name.equals("minecraft:unknown")) {
            return -2;
        }

        final CompoundTag cleanedBlockStateTag = new CompoundTag();
        cleanedBlockStateTag.put("name", blockStateTag.get("name"));
        cleanedBlockStateTag.put("states", new CompoundTag(new TreeMap<>(blockStateTag.getCompoundTag("states").getValue())));

        final ByteBuf byteBuf = Unpooled.buffer();
        try {
            BedrockTypes.TAG_LE.write(byteBuf, cleanedBlockStateTag);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        final byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        byteBuf.release();

        return FNV1.fnv1a_32(bytes);
    }

}
