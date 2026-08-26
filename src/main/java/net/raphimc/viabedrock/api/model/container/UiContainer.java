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
package net.raphimc.viabedrock.api.model.container;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.api.model.container.player.HudContainer;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

/**
 * A menu which doesn't have a container of its own. Anvils, enchanting tables, crafting tables and the like keep their
 * items in the player UI container, so this container is only a view onto specific slots of it.
 *
 * <p>Unlike the other containers, the slots of this one are indexed by Java menu slot, because that is the order the
 * layout is defined in.</p>
 */
public class UiContainer extends MenuContainer {

    private final UiContainerLayout layout;

    public UiContainer(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final UiContainerLayout layout, final String... validBlockTags) {
        super(user, containerId, type, title, position, layout.size(), validBlockTags);

        this.layout = layout;
    }

    public UiContainerLayout layout() {
        return this.layout;
    }

    @Override
    public int javaSlotForUiSlot(final int uiSlot) {
        return this.layout.javaSlot(uiSlot);
    }

    @Override
    public BedrockItem getItem(final int slot) {
        return this.hudContainer().getItem(this.layout.uiSlot(slot));
    }

    @Override
    public BedrockItem[] getItems() {
        final BedrockItem[] items = new BedrockItem[this.size()];
        for (int slot = 0; slot < items.length; slot++) {
            items[slot] = this.getItem(slot);
        }
        return items;
    }

    @Override
    public boolean setItem(final int slot, final BedrockItem item) {
        return this.hudContainer().setItem(this.layout.uiSlot(slot), item);
    }

    @Override
    public ContainerEnumName bedrockContainerName(final int slot) {
        return this.layout.slotName(slot);
    }

    @Override
    public int bedrockRequestSlot(final int slot) {
        return this.layout.uiSlot(slot);
    }

    private HudContainer hudContainer() {
        return this.user.get(InventoryTracker.class).getHudContainer();
    }

}
