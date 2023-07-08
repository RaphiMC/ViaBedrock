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
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.ItemEntry;
import net.raphimc.viabedrock.protocol.types.BedrockItemType;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.array.ArrayType;

public class ItemRewriter extends StoredObject {

    private final BiMap<String, Integer> items;
    private final Type<BedrockItem> itemType;
    private final Type<BedrockItem[]> itemArrayType;

    public ItemRewriter(final UserConnection user, final ItemEntry[] itemEntries) {
        super(user);

        this.items = HashBiMap.create(BedrockProtocol.MAPPINGS.getBedrockItems());
        for (ItemEntry itemEntry : itemEntries) {
            this.items.inverse().remove(itemEntry.id());
            this.items.put(Key.namespaced(itemEntry.identifier()), itemEntry.id());
        }
        this.itemType = new BedrockItemType(this.items.getOrDefault("minecraft:shield", -1));
        this.itemArrayType = new ArrayType<>(this.itemType, BedrockTypes.UNSIGNED_VAR_INT);
    }

    public Item javaItem(final BedrockItem bedrockItem) {
        if(bedrockItem == null) return null;

        return new DataItem(BedrockProtocol.MAPPINGS.getJavaItems().get(bedrockItem.identifier() == 1 ? "minecraft:stone" : "minecraft:grass_block"), (byte) bedrockItem.amount(), (short) 0, null); // TODO
    }

    public Item[] javaItems(final BedrockItem[] bedrockItems) {
        final Item[] javaItems = new Item[bedrockItems.length];
        for (int i = 0; i < bedrockItems.length; i++) {
            javaItems[i] = this.javaItem(bedrockItems[i]);
        }
        return javaItems;
    }

    public CompoundTag javaTag(final CompoundTag bedrockTag) {
        return new CompoundTag(); // TODO
    }

    public BedrockItem bedrockItem(final Item javaItem) {
        return null;
    }

    public BedrockItem[] bedrockItems(final Item[] javaItems) {
        final BedrockItem[] bedrockItems = new BedrockItem[javaItems.length];
        for (int i = 0; i < javaItems.length; i++) {
            bedrockItems[i] = this.bedrockItem(javaItems[i]);
        }
        return bedrockItems;
    }

    public BiMap<String, Integer> getItems() {
        return this.items;
    }

    public Type<BedrockItem> itemType() {
        return this.itemType;
    }

    public Type<BedrockItem[]> itemArrayType() {
        return this.itemArrayType;
    }

}
