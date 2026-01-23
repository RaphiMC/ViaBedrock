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
import com.viaversion.nbt.tag.Tag;

import java.util.Map;

public class RegistryUtil {

    public static int getRegistryIndex(final CompoundTag registry, final CompoundTag entry) {
        int index = 0;
        for (Map.Entry<String, Tag> tagEntry : registry.entrySet()) {
            if (tagEntry.getValue().equals(entry)) {
                return index;
            }
            index++;
        }

        throw new IllegalArgumentException("Entry not found in registry");
    }

}
