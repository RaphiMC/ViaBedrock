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
package net.raphimc.viabedrock.protocol.rewriter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.ItemEntry;
import net.raphimc.viabedrock.protocol.types.BedrockItemType;

public class ItemRewriter extends StoredObject {

    private final BiMap<String, Integer> items;
    private final Type<BedrockItem> itemType;

    public ItemRewriter(final UserConnection user, final ItemEntry[] itemEntries) {
        super(user);

        this.items = HashBiMap.create(BedrockProtocol.MAPPINGS.getBedrockItems());
        for (ItemEntry itemEntry : itemEntries) {
            this.items.inverse().remove(itemEntry.id());
            this.items.put(Key.namespaced(itemEntry.identifier()), itemEntry.id());
        }
        this.itemType = new BedrockItemType(this.items.getOrDefault("minecraft:shield", -1));
    }

    public CompoundTag javaTag(final CompoundTag bedrockTag) {
        return new CompoundTag(); // TODO
    }

    public BiMap<String, Integer> getItems() {
        return this.items;
    }

    public Type<BedrockItem> itemType() {
        return this.itemType;
    }

}
