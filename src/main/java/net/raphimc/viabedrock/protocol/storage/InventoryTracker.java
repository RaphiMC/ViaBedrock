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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import net.raphimc.viabedrock.api.model.inventory.Container;
import net.raphimc.viabedrock.api.model.inventory.InventoryContainer;
import net.raphimc.viabedrock.api.model.inventory.fake.FakeContainer;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.WindowIds;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;

import java.util.concurrent.atomic.AtomicInteger;

public class InventoryTracker extends StoredObject {

    private static final int MIN_FAKE_ID = 101;
    private static final int MAX_FAKE_ID = 111;
    private final AtomicInteger FAKE_ID_COUNTER = new AtomicInteger(MIN_FAKE_ID);

    private final Container inventoryContainer = new InventoryContainer(WindowIds.INVENTORY);

    private Position currentContainerPosition = null;
    private Container currentContainer = null;
    private BedrockItem currentCursorItem = null;

    private Container pendingCloseContainer = null;
    private FakeContainer currentFakeContainer = null;

    public InventoryTracker(final UserConnection user) {
        super(user);
    }

    public Container getContainer(final byte windowId) {
        if (windowId == WindowIds.INVENTORY) return this.inventoryContainer;
        if (this.currentContainer != null && this.currentContainer.windowId() == windowId) return this.currentContainer;

        return null;
    }

    public void trackContainer(final Position position, final Container container) {
        this.currentContainerPosition = position;
        this.currentContainer = container;
    }

    public void openFakeContainer(final FakeContainer container) throws Exception {
        this.currentFakeContainer = container;

        final PacketWrapper openWindow = PacketWrapper.create(ClientboundPackets1_20_3.OPEN_WINDOW, this.getUser());
        openWindow.write(Type.VAR_INT, (int) container.windowId()); // window id
        openWindow.write(Type.VAR_INT, container.menuType().javaMenuTypeId()); // type
        openWindow.write(Type.TAG, TextUtil.componentToNbt(container.title())); // title
        openWindow.scheduleSend(BedrockProtocol.class);

        final PacketWrapper windowItems = PacketWrapper.create(ClientboundPackets1_20_3.WINDOW_ITEMS, this.getUser());
        this.writeWindowItems(windowItems, container);
        windowItems.scheduleSend(BedrockProtocol.class);
    }

    public void setCurrentContainerClosed() throws Exception {
        this.currentContainerPosition = null;
        this.currentContainer = null;
        this.pendingCloseContainer = null;

        if (this.currentFakeContainer != null) {
            this.openFakeContainer(this.currentFakeContainer);
        }
    }

    public boolean markPendingClose(final boolean callOnClosed) throws Exception {
        if (this.currentContainer != null) {
            this.pendingCloseContainer = this.currentContainer;
            this.currentContainerPosition = null;
            this.currentContainer = null;
        } else if (this.currentFakeContainer != null) {
            final FakeContainer container = this.currentFakeContainer;
            this.currentFakeContainer = null;
            if (callOnClosed) {
                container.onClosed();
            }
            return false;
        }

        return true;
    }

    public void closeCurrentContainer() throws Exception {
        if (!this.markPendingClose(false)) {
            throw new IllegalStateException("There is no container to close");
        }

        final PacketWrapper closeWindow = PacketWrapper.create(ClientboundPackets1_20_3.CLOSE_WINDOW, this.getUser());
        closeWindow.write(Type.UNSIGNED_BYTE, (short) this.pendingCloseContainer.windowId()); // window id
        closeWindow.send(BedrockProtocol.class);

        final PacketWrapper containerClose = PacketWrapper.create(ServerboundBedrockPackets.CONTAINER_CLOSE, this.getUser());
        containerClose.write(Type.BYTE, this.pendingCloseContainer.windowId()); // window id
        containerClose.write(Type.BOOLEAN, false); // server initiated
        containerClose.sendToServer(BedrockProtocol.class);
    }

    public void handleWindowClick(final byte windowId, final int revision, final short slot, final byte button, final int action) throws Exception {
        if (this.pendingCloseContainer != null) return;
        final Container targetContainer = this.currentContainer != null ? this.currentContainer : this.currentFakeContainer;
        if (targetContainer == null) return;

        if (targetContainer.windowId() != windowId) return;
        if (!targetContainer.handleWindowClick(revision, slot, button, action)) {
            if (targetContainer != this.inventoryContainer) {
                final PacketWrapper windowItems = PacketWrapper.create(ClientboundPackets1_20_3.WINDOW_ITEMS, this.getUser());
                this.writeWindowItems(windowItems, this.inventoryContainer);
                windowItems.send(BedrockProtocol.class);
            }
            final PacketWrapper windowItems = PacketWrapper.create(ClientboundPackets1_20_3.WINDOW_ITEMS, this.getUser());
            this.writeWindowItems(windowItems, targetContainer);
            windowItems.send(BedrockProtocol.class);
        }
    }

    public void writeWindowItems(final PacketWrapper wrapper, final Container container) {
        wrapper.write(Type.UNSIGNED_BYTE, (short) container.windowId()); // window id
        wrapper.write(Type.VAR_INT, 0); // revision
        wrapper.write(Type.ITEM1_20_2_ARRAY, container.getJavaItems(this.getUser())); // items
        wrapper.write(Type.ITEM1_20_2, this.getUser().get(ItemRewriter.class).javaItem(this.currentCursorItem)); // cursor item
    }

    public void tick() throws Exception {
        if (this.currentContainer != null && this.currentContainerPosition != null) {
            final ChunkTracker chunkTracker = this.getUser().get(ChunkTracker.class);
            final BlockStateRewriter blockStateRewriter = this.getUser().get(BlockStateRewriter.class);
            final int blockState = chunkTracker.getBlockState(this.currentContainerPosition);
            final String tag = blockStateRewriter.tag(blockState);
            if (!this.currentContainer.menuType().isAcceptedTag(tag)) {
                this.closeCurrentContainer();
                return;
            }

            final EntityTracker entityTracker = this.getUser().get(EntityTracker.class);
            final Position3f containerPosition = new Position3f(this.currentContainerPosition.x() + 0.5F, this.currentContainerPosition.y() + 0.5F, this.currentContainerPosition.z() + 0.5F);
            final Position3f playerPosition = entityTracker.getClientPlayer().position();
            if (playerPosition.distanceTo(containerPosition) > 6) {
                this.closeCurrentContainer();
            }
        }
    }

    public boolean isContainerOpen() {
        return this.currentContainer != null || this.pendingCloseContainer != null;
    }

    public boolean isAnyContainerOpen() {
        return this.currentContainer != null || this.pendingCloseContainer != null || this.currentFakeContainer != null;
    }

    public Container getCurrentContainer() {
        return this.currentContainer;
    }

    public BedrockItem getCurrentCursorItem() {
        return this.currentCursorItem;
    }

    public Container getPendingCloseContainer() {
        return this.pendingCloseContainer;
    }

    public FakeContainer getCurrentFakeContainer() {
        return this.currentFakeContainer;
    }

    public byte getNextFakeWindowId() {
        final int id = this.FAKE_ID_COUNTER.getAndIncrement();
        if (id > MAX_FAKE_ID) {
            this.FAKE_ID_COUNTER.set(MIN_FAKE_ID);
            return MIN_FAKE_ID;
        }
        return (byte) id;
    }

}
