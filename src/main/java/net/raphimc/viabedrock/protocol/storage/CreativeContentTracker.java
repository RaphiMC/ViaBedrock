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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the creative items the server sent. Creating an item in creative mode is not done by simply putting it into a
 * slot, but by referencing one of these entries by its net id.
 */
public class CreativeContentTracker extends StoredObject {

    private final Map<Integer, BedrockItem> creativeItems = new LinkedHashMap<>();

    public CreativeContentTracker(final UserConnection user) {
        super(user);
    }

    public void setCreativeItems(final Map<Integer, BedrockItem> creativeItems) {
        this.creativeItems.clear();
        this.creativeItems.putAll(creativeItems);
    }

    public void clear() {
        this.creativeItems.clear();
    }

    public boolean isEmpty() {
        return this.creativeItems.isEmpty();
    }

    public BedrockItem getCreativeItem(final int netId) {
        return this.creativeItems.get(netId);
    }

    /**
     * Finds the creative entry which produces the given Java item. Since the Java client only tells us which item it
     * wants, the matching Bedrock creative entry has to be looked up by translating the candidates back to Java.
     *
     * @param javaItem The item the Java client asked for
     * @return The net id of the matching creative entry or 0 if there is none
     */
    public int findCreativeNetId(final Item javaItem) {
        if (javaItem == null || javaItem.isEmpty()) {
            return 0;
        }

        final ItemRewriter itemRewriter = this.user().get(ItemRewriter.class);
        int fallback = 0;
        for (Map.Entry<Integer, BedrockItem> entry : this.creativeItems.entrySet()) {
            final Item translated = itemRewriter.javaItem(entry.getValue());
            if (translated.identifier() != javaItem.identifier()) continue;

            if (translated.dataContainer().equals(javaItem.dataContainer())) { // Exact match including all item data
                return entry.getKey();
            } else if (fallback == 0) {
                fallback = entry.getKey();
            }
        }
        return fallback;
    }

}
