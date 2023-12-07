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
package net.raphimc.viabedrock.api.util;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import net.lenni0451.mcstructs.nbt.INbtTag;

import java.util.Map;

public class NbtUtil {

    public static Tag createTag(final Object obj) {
        if (obj instanceof Byte) {
            return new ByteTag((Byte) obj);
        } else if (obj instanceof Integer) {
            return new IntTag((Integer) obj);
        } else if (obj instanceof String) {
            return new StringTag((String) obj);
        } else if (obj instanceof Short) {
            return new ShortTag((Short) obj);
        } else {
            throw new IllegalArgumentException("Unknown value type: " + obj.getClass());
        }
    }

    public static Tag mcStructsToVia(final INbtTag nbtTag) {
        if (nbtTag == null) return null;

        if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.ByteTag) {
            return new ByteTag(((net.lenni0451.mcstructs.nbt.tags.ByteTag) nbtTag).getValue());
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.ShortTag) {
            return new ShortTag(((net.lenni0451.mcstructs.nbt.tags.ShortTag) nbtTag).getValue());
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.IntTag) {
            return new IntTag(((net.lenni0451.mcstructs.nbt.tags.IntTag) nbtTag).getValue());
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.LongTag) {
            return new LongTag(((net.lenni0451.mcstructs.nbt.tags.LongTag) nbtTag).getValue());
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.FloatTag) {
            return new FloatTag(((net.lenni0451.mcstructs.nbt.tags.FloatTag) nbtTag).getValue());
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.DoubleTag) {
            return new DoubleTag(((net.lenni0451.mcstructs.nbt.tags.DoubleTag) nbtTag).getValue());
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.ByteArrayTag) {
            return new ByteArrayTag(((net.lenni0451.mcstructs.nbt.tags.ByteArrayTag) nbtTag).getValue());
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.StringTag) {
            return new StringTag(((net.lenni0451.mcstructs.nbt.tags.StringTag) nbtTag).getValue());
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.ListTag<?>) {
            final ListTag list = new ListTag();
            for (INbtTag t : ((net.lenni0451.mcstructs.nbt.tags.ListTag<?>) nbtTag).getValue()) {
                list.add(mcStructsToVia(t));
            }
            return list;
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.CompoundTag) {
            final Map<String, INbtTag> values = ((net.lenni0451.mcstructs.nbt.tags.CompoundTag) nbtTag).getValue();
            final CompoundTag compound = new CompoundTag();
            for (String key : values.keySet()) {
                compound.put(key, mcStructsToVia(values.get(key)));
            }
            return compound;
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.IntArrayTag) {
            return new IntArrayTag(((net.lenni0451.mcstructs.nbt.tags.IntArrayTag) nbtTag).getValue());
        } else if (nbtTag instanceof net.lenni0451.mcstructs.nbt.tags.LongArrayTag) {
            return new LongArrayTag(((net.lenni0451.mcstructs.nbt.tags.LongArrayTag) nbtTag).getValue());
        }

        throw new IllegalArgumentException("Unsupported tag type: " + nbtTag.getClass().getName());
    }


}
