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
package net.raphimc.viabedrock.api.model.inventory.fake;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundPackets1_20_5;
import net.raphimc.viabedrock.api.model.inventory.Container;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.MenuType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

public abstract class FakeContainer extends Container {

    protected final UserConnection user;
    protected final ATextComponent title;

    public FakeContainer(final UserConnection user, final MenuType menuType, final ATextComponent title) {
        super(user.get(InventoryTracker.class).getNextFakeWindowId(), menuType, 0);

        this.user = user;
        this.title = title;
    }

    @Override
    public void setItems(final BedrockItem[] items) {
        throw new UnsupportedOperationException("Fake containers cannot have bedrock items");
    }

    @Override
    public BedrockItem[] items() {
        throw new UnsupportedOperationException("Fake containers cannot have bedrock items");
    }

    public void onAnvilRename(final String name) {
    }

    public void onClosed() throws Exception {
    }

    protected void close() throws Exception {
        final PacketWrapper closeWindow = PacketWrapper.create(ClientboundPackets1_20_5.CLOSE_WINDOW, this.user);
        closeWindow.write(Type.UNSIGNED_BYTE, (short) this.windowId); // window id
        closeWindow.send(BedrockProtocol.class);

        if (this.user.get(InventoryTracker.class).markPendingClose(false)) {
            throw new IllegalStateException("Couldn't close fake container, because a real one was open");
        }
    }

    public ATextComponent title() {
        return this.title;
    }

}
