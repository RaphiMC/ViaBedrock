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
package net.raphimc.viabedrock.api.model.inventory;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import net.raphimc.viabedrock.protocol.data.enums.java.ClickType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;

public class WrappedContainer extends Container {

    private final Container delegate;

    public WrappedContainer(final byte windowId, final BlockPosition position, final ATextComponent title, final Container delegate) {
        super(windowId, delegate.menuType, title, position, 0);
        this.delegate = delegate;
    }

    @Override
    public void setItems(final BedrockItem[] items) {
        this.delegate.setItems(items);
    }

    @Override
    public void setCursorItem(final BedrockItem cursorItem) {
        this.delegate.setCursorItem(cursorItem);
    }

    @Override
    public boolean handleContainerClick(final int revision, final short slot, final byte button, final ClickType action) {
        return this.delegate.handleContainerClick(revision, slot, button, action);
    }

    @Override
    public Item[] getJavaItems(final UserConnection user) {
        return this.delegate.getJavaItems(user);
    }

    @Override
    public Item getJavaCursorItem(final UserConnection user) {
        return this.delegate.getJavaCursorItem(user);
    }

    @Override
    public BedrockItem[] items() {
        return this.delegate.items();
    }

    @Override
    public BedrockItem cursorItem() {
        return this.delegate.cursorItem();
    }

    public Container delegate() {
        return this.delegate;
    }

}
