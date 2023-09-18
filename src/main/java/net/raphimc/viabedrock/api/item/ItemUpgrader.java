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
package net.raphimc.viabedrock.api.item;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.FileSystemUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ItemUpgrader {

    private final Map<String, Int2ObjectMap<String>> remappedMetas = new HashMap<>();

    public ItemUpgrader() {
        try {
            for (byte[] data : FileSystemUtil.getFilesInDirectory("assets/viabedrock/item_upgrade_schema").values()) {
                final JsonObject json = GsonUtil.getGson().fromJson(new String(data, StandardCharsets.UTF_8), JsonObject.class);
                if (json.has("remappedMetas")) {
                    final JsonObject remappedMetas = json.getAsJsonObject("remappedMetas");
                    for (Map.Entry<String, JsonElement> entry : remappedMetas.entrySet()) {
                        final Int2ObjectMap<String> metaMap = this.remappedMetas.computeIfAbsent(entry.getKey(), k -> new Int2ObjectOpenHashMap<>());
                        for (Map.Entry<String, JsonElement> metaEntry : entry.getValue().getAsJsonObject().entrySet()) {
                            metaMap.put(Integer.parseInt(metaEntry.getKey()), metaEntry.getValue().getAsString());
                        }
                    }
                }
            }
        } catch (Throwable e) {
            ViaBedrock.getPlatform().getLogger().log(Level.SEVERE, "Failed to load item upgrade schema", e);
            this.remappedMetas.clear();
        }
    }

    public String upgradeMetaItem(final String identifier, final int data) {
        final Int2ObjectMap<String> metas = this.remappedMetas.get(identifier);
        if (metas == null) {
            return null;
        }
        return metas.get(data);
    }

}
