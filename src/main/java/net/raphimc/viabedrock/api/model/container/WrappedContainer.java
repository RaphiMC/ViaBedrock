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
import net.raphimc.viabedrock.protocol.data.enums.java.ClickType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;

public class WrappedContainer extends Container {

    private final Container delegate;

    public WrappedContainer(final UserConnection user, final byte windowId, final BlockPosition position, final ATextComponent title, final Container delegate) {
        super(user, windowId, delegate.menuType, title, position, 0);
        this.delegate = delegate;
    }

    @Override
    public boolean handleContainerClick(final int revision, final short slot, final byte button, final ClickType action) {
        return this.delegate.handleContainerClick(revision, slot, button, action);
    }

    @Override
    public Item[] getJavaItems() {
        return this.delegate.getJavaItems();
    }

    @Override
    public Item getJavaCursorItem() {
        return this.delegate.getJavaCursorItem();
    }

    @Override
    public BedrockItem getItem(final int slot) {
        return this.delegate.getItem(slot);
    }

    @Override
    public void setItems(final BedrockItem[] items) {
        this.delegate.setItems(items);
    }

    @Override
    public void setItem(final int slot, final BedrockItem item) {
        this.delegate.setItem(slot, item);
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public BedrockItem cursorItem() {
        return this.delegate.cursorItem();
    }

    public Container delegate() {
        return this.delegate;
    }

    @Override
    protected void onSlotChanged(final int slot, final BedrockItem oldItem, final BedrockItem newItem) {
        this.delegate.onSlotChanged(slot, oldItem, newItem);
    }

}
