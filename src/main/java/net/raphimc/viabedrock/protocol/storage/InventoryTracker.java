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
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.inventory.Container;
import net.raphimc.viabedrock.api.model.inventory.InventoryContainer;
import net.raphimc.viabedrock.api.model.inventory.WrappedContainer;
import net.raphimc.viabedrock.api.model.inventory.fake.FakeContainer;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class InventoryTracker extends StoredObject {

    private static final int MIN_FAKE_ID = ContainerID.CONTAINER_ID_LAST.getValue() + 1;
    private static final int MAX_FAKE_ID = ContainerID.CONTAINER_ID_OFFHAND.getValue() - 1;
    private final AtomicInteger FAKE_ID_COUNTER = new AtomicInteger(MIN_FAKE_ID);

    private final InventoryContainer inventoryContainer = new InventoryContainer((byte) ContainerID.CONTAINER_ID_INVENTORY.getValue());

    private BlockPosition currentContainerPosition = null;
    private Container currentContainer = null;

    private Container pendingCloseContainer = null;
    private FakeContainer currentFakeContainer = null;

    public InventoryTracker(final UserConnection user) {
        super(user);
    }

    public Container getContainerClientbound(final byte windowId) {
        if (windowId == this.inventoryContainer.windowId()) return this.inventoryContainer;
        if (this.currentContainer != null && this.currentContainer.windowId() == windowId) return this.currentContainer;
        return null;
    }

    public Container getContainerServerbound(final byte windowId) {
        if (this.currentContainer != null && this.currentContainer.windowId() == windowId) return this.currentContainer;
        if (windowId == ContainerID.CONTAINER_ID_INVENTORY.getValue() && this.currentContainer instanceof WrappedContainer wrappedContainer && wrappedContainer.delegate() == this.inventoryContainer) {
            return this.currentContainer;
        }
        if (this.currentFakeContainer != null && this.currentFakeContainer.windowId() == windowId) return this.currentFakeContainer;
        return null;
    }

    public void setCurrentContainer(final BlockPosition position, final Container container) {
        this.currentContainerPosition = position;
        this.currentContainer = container;
    }

    public void openFakeContainer(final FakeContainer container) {
        this.currentFakeContainer = container;

        final PacketWrapper openScreen = PacketWrapper.create(ClientboundPackets1_21.OPEN_SCREEN, this.getUser());
        openScreen.write(Types.VAR_INT, (int) container.windowId()); // window id
        openScreen.write(Types.VAR_INT, container.menuType().javaMenuTypeId()); // type
        openScreen.write(Types.TAG, TextUtil.textComponentToNbt(container.title())); // title
        openScreen.send(BedrockProtocol.class);
        PacketFactory.sendContainerSetContent(this.getUser(), container);
    }

    public void setCurrentContainerClosed() {
        this.currentContainerPosition = null;
        this.currentContainer = null;
        this.pendingCloseContainer = null;

        if (this.currentFakeContainer != null) {
            this.openFakeContainer(this.currentFakeContainer);
        }
    }

    public boolean markPendingClose(final boolean callOnClosed) {
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

    public void closeCurrentContainer() {
        if (!this.markPendingClose(false)) {
            throw new IllegalStateException("There is no container to close");
        }

        final PacketWrapper closeWindow = PacketWrapper.create(ClientboundPackets1_21.CONTAINER_CLOSE, this.getUser());
        closeWindow.write(Types.UNSIGNED_BYTE, (short) this.pendingCloseContainer.windowId()); // window id
        closeWindow.send(BedrockProtocol.class);
        PacketFactory.sendContainerClose(this.getUser(), this.pendingCloseContainer.windowId(), ContainerType.NONE);
    }

    public void tick() {
        if (this.currentContainer != null && this.currentContainerPosition != null) {
            if (this.currentContainer.menuType().bedrockContainerType().equals(ContainerType.INVENTORY)) return;

            final ChunkTracker chunkTracker = this.getUser().get(ChunkTracker.class);
            final BlockStateRewriter blockStateRewriter = this.getUser().get(BlockStateRewriter.class);
            final int blockState = chunkTracker.getBlockState(this.currentContainerPosition);
            final String tag = blockStateRewriter.tag(blockState);
            if (!this.currentContainer.menuType().isAcceptedTag(tag)) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + this.currentContainer.menuType().bedrockContainerType() + " because block state is not valid for container type: " + blockState);
                this.closeCurrentContainer();
                return;
            }

            final EntityTracker entityTracker = this.getUser().get(EntityTracker.class);
            final Position3f containerPosition = new Position3f(this.currentContainerPosition.x() + 0.5F, this.currentContainerPosition.y() + 0.5F, this.currentContainerPosition.z() + 0.5F);
            final Position3f playerPosition = entityTracker.getClientPlayer().position();
            if (playerPosition.distanceTo(containerPosition) > 6) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + this.currentContainer.menuType().bedrockContainerType() + " because player is too far away (" + playerPosition.distanceTo(containerPosition) + " > 6)");
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

    public InventoryContainer getInventoryContainer() {
        return this.inventoryContainer;
    }

    public Container getCurrentContainer() {
        return this.currentContainer;
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
            return (byte) MIN_FAKE_ID;
        }
        return (byte) id;
    }

}
