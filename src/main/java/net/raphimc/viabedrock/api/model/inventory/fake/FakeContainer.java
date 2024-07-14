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
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.api.model.inventory.Container;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.MenuType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

public abstract class FakeContainer extends Container {

    protected final UserConnection user;

    public FakeContainer(final UserConnection user, final MenuType menuType, final ATextComponent title) {
        super(user.get(InventoryTracker.class).getNextFakeWindowId(), menuType, title, null, 0);

        this.user = user;
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

    public void onClosed() {
    }

    public void close() {
        final PacketWrapper containerClose = PacketWrapper.create(ClientboundPackets1_21.CONTAINER_CLOSE, this.user);
        containerClose.write(Types.UNSIGNED_BYTE, (short) this.windowId); // window id
        containerClose.send(BedrockProtocol.class);
        this.user.get(InventoryTracker.class).markPendingClose(this);
    }

}
