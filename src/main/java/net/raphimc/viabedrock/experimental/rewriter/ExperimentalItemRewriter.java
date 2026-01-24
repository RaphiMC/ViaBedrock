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
package net.raphimc.viabedrock.experimental.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.experimental.model.map.MapObject;
import net.raphimc.viabedrock.experimental.storage.MapTracker;
import net.raphimc.viabedrock.protocol.model.BedrockItem;

import java.util.logging.Level;

public class ExperimentalItemRewriter {

    // BedrockTag can be null
    public static void handleItem(final UserConnection user, final BedrockItem bedrockItem, final CompoundTag bedrockTag, final Item javaItem) {
        if (bedrockTag != null && bedrockTag.get("map_uuid") instanceof LongTag uuidTag) {
            MapTracker mapTracker = user.get(MapTracker.class);
            final long uuid = uuidTag.asLong();

            MapObject map = mapTracker.getMapObjects().get(uuid);
            if (map == null) {
                final int mapId = mapTracker.getNextMapId();
                map = new MapObject(uuid, mapId);
                mapTracker.getMapObjects().put(uuid, map);
                //ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Registered new map with id " + mapId + " and uuid " + uuid);
            }

            javaItem.dataContainer().set(StructuredDataKey.MAP_ID, map.getJavaId());
        }

    }

}
