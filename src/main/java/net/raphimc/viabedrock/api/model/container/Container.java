/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.api.model.container;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import net.raphimc.viabedrock.protocol.data.enums.MenuType;
import net.raphimc.viabedrock.protocol.data.enums.java.ClickType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;

public abstract class Container {

    protected final UserConnection user;
    protected final byte windowId;
    protected final MenuType menuType;
    protected final ATextComponent title;
    protected final BlockPosition position;
    private final BedrockItem[] items;
    protected BedrockItem cursorItem = BedrockItem.empty();

    public Container(final UserConnection user, final byte windowId, final MenuType menuType, final ATextComponent title, final BlockPosition position, final int size) {
        this.user = user;
        this.windowId = windowId;
        this.menuType = menuType;
        this.title = title;
        this.position = position;
        this.items = BedrockItem.emptyArray(size);
    }

    public boolean handleContainerClick(final int revision, final short slot, final byte button, final ClickType action) {
        return false;
    }

    public Item[] getJavaItems() {
        return this.user.get(ItemRewriter.class).javaItems(this.items);
    }

    public Item getJavaCursorItem() {
        return this.user.get(ItemRewriter.class).javaItem(this.cursorItem);
    }

    public BedrockItem getItem(final int slot) {
        return this.items[slot];
    }

    public void setItems(final BedrockItem[] items) {
        if (items.length != this.items.length) throw new IllegalArgumentException("Items length must be equal to container size");

        for (int i = 0; i < items.length; i++) {
            this.setItem(i, items[i]);
        }
    }

    public void setItem(final int slot, final BedrockItem item) {
        final BedrockItem oldItem = this.items[slot];
        this.items[slot] = item;
        this.onSlotChanged(slot, oldItem, item);
    }

    public int size() {
        return this.items.length;
    }

    public byte windowId() {
        return this.windowId;
    }

    public MenuType menuType() {
        return this.menuType;
    }

    public ATextComponent title() {
        return this.title;
    }

    public BlockPosition position() {
        return this.position;
    }

    public BedrockItem cursorItem() {
        return this.cursorItem;
    }

    protected void onSlotChanged(final int slot, final BedrockItem oldItem, final BedrockItem newItem) {
    }

}
