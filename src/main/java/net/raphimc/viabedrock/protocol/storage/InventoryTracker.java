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
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.InventoryContainer;
import net.raphimc.viabedrock.api.model.container.WrappedContainer;
import net.raphimc.viabedrock.api.model.container.fake.FakeContainer;
import net.raphimc.viabedrock.api.model.container.fake.FormContainer;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class InventoryTracker extends StoredObject {

    private static final int MIN_FAKE_ID = ContainerID.CONTAINER_ID_LAST.getValue() + 1;
    private static final int MAX_FAKE_ID = ContainerID.CONTAINER_ID_OFFHAND.getValue() - 1;
    private final AtomicInteger FAKE_ID_COUNTER = new AtomicInteger(MIN_FAKE_ID);

    private final InventoryContainer inventoryContainer = new InventoryContainer(this.getUser(), (byte) ContainerID.CONTAINER_ID_INVENTORY.getValue());
    private final Stack<Container> containerStack = new Stack<>();
    private final List<Container> closeWhenTickedContainers = new ArrayList<>();

    private Container pendingCloseContainer = null;

    public InventoryTracker(final UserConnection user) {
        super(user);
    }

    public Container getContainerClientbound(final byte windowId) {
        if (windowId == this.inventoryContainer.windowId()) return this.inventoryContainer;
        for (int i = this.containerStack.size() - 1; i >= 0; i--) {
            final Container container = this.containerStack.get(i);
            if (container instanceof FakeContainer) continue;
            if (container.windowId() == windowId) return container;
        }
        return null;
    }

    public Container getContainerServerbound(final byte windowId) {
        if (windowId == ContainerID.CONTAINER_ID_INVENTORY.getValue() && this.isInventoryOpen()) {
            return this.getCurrentContainer();
        }
        for (int i = this.containerStack.size() - 1; i >= 0; i--) {
            final Container container = this.containerStack.get(i);
            if (container instanceof FakeContainer) continue;
            if (container.windowId() == windowId) return container;
        }
        for (int i = this.containerStack.size() - 1; i >= 0; i--) {
            final Container container = this.containerStack.get(i);
            if (container instanceof FakeContainer && container.windowId() == windowId) {
                return container;
            }
        }
        return null;
    }

    public void setCurrentContainer(final Container container) {
        this.containerStack.push(container);
    }

    public void openContainer(final Container container) {
        this.containerStack.push(container);

        final PacketWrapper openScreen = PacketWrapper.create(ClientboundPackets1_21.OPEN_SCREEN, this.getUser());
        openScreen.write(Types.VAR_INT, (int) container.windowId()); // window id
        openScreen.write(Types.VAR_INT, container.menuType().javaMenuTypeId()); // type
        openScreen.write(Types.TAG, TextUtil.textComponentToNbt(container.title())); // title
        openScreen.send(BedrockProtocol.class);
        PacketFactory.sendJavaContainerSetContent(this.getUser(), container);
    }

    public void markPendingClose(final Container container) {
        if (container instanceof FakeContainer fakeContainer) {
            this.containerStack.remove(fakeContainer);
            fakeContainer.onClosed();
            if (!this.containerStack.isEmpty()) {
                this.openContainer(this.containerStack.pop());
            }
            return;
        }

        if (this.pendingCloseContainer != null) {
            throw new IllegalStateException("There is already a container pending close");
        }
        this.pendingCloseContainer = container;
    }

    public void setCurrentContainerClosed(final boolean serverInitiated) {
        if (serverInitiated) {
            this.pendingCloseContainer = this.getCurrentContainer();
            PacketFactory.sendBedrockContainerClose(this.getUser(), this.pendingCloseContainer.windowId(), ContainerType.NONE);
        }
        if (this.pendingCloseContainer != this.getCurrentContainer()) {
            throw new IllegalStateException("Current container is not the pending close container");
        }
        this.containerStack.remove(this.pendingCloseContainer);
        this.pendingCloseContainer = null;

        if (!this.containerStack.isEmpty()) {
            this.openContainer(this.containerStack.pop());
        }
    }

    public void closeAllContainers() {
        while (!this.containerStack.isEmpty()) {
            final Container container = this.containerStack.pop();
            if (container instanceof FakeContainer fakeContainer) {
                if (fakeContainer instanceof FormContainer) {
                    fakeContainer.onClosed(); // Send user closed response
                }
            } else {
                PacketFactory.sendBedrockContainerClose(this.getUser(), container.windowId(), ContainerType.NONE);
            }
        }
        this.pendingCloseContainer = null;
    }

    public void closeWhenTicked(final Container container) {
        this.closeWhenTickedContainers.add(container);
    }

    public void tick() {
        final Container currentContainer = this.getOpenContainer();
        if (this.closeWhenTickedContainers.remove(currentContainer)) {
            this.forceCloseContainer(currentContainer);
            return;
        }

        if (currentContainer != null && currentContainer.position() != null) {
            if (currentContainer.menuType().bedrockContainerType().equals(ContainerType.INVENTORY)) return;

            final ChunkTracker chunkTracker = this.getUser().get(ChunkTracker.class);
            final BlockStateRewriter blockStateRewriter = this.getUser().get(BlockStateRewriter.class);
            final int blockState = chunkTracker.getBlockState(currentContainer.position());
            final String tag = blockStateRewriter.tag(blockState);
            if (!currentContainer.menuType().isAcceptedTag(tag)) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + currentContainer.menuType().bedrockContainerType() + " because block state is not valid for container type: " + blockState);
                this.forceCloseContainer(currentContainer);
                return;
            }

            final EntityTracker entityTracker = this.getUser().get(EntityTracker.class);
            final Position3f containerPosition = new Position3f(currentContainer.position().x() + 0.5F, currentContainer.position().y() + 0.5F, currentContainer.position().z() + 0.5F);
            final Position3f playerPosition = entityTracker.getClientPlayer().position();
            if (playerPosition.distanceTo(containerPosition) > 6) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + currentContainer.menuType().bedrockContainerType() + " because player is too far away (" + playerPosition.distanceTo(containerPosition) + " > 6)");
                this.forceCloseContainer(currentContainer);
            }
        }
    }

    public boolean isContainerOpen() {
        return this.getCurrentContainer() != null || this.pendingCloseContainer != null;
    }

    public boolean isInventoryOpen() {
        return this.getCurrentContainer() instanceof WrappedContainer wrappedContainer && wrappedContainer.delegate() == this.inventoryContainer;
    }

    public InventoryContainer getInventoryContainer() {
        return this.inventoryContainer;
    }

    public Container getCurrentContainer() {
        for (int i = this.containerStack.size() - 1; i >= 0; i--) {
            final Container container = this.containerStack.get(i);
            if (container instanceof FakeContainer) continue;
            return container;
        }
        return null;
    }

    public FakeContainer getCurrentFakeContainer() {
        for (int i = this.containerStack.size() - 1; i >= 0; i--) {
            final Container container = this.containerStack.get(i);
            if (container instanceof FakeContainer fakeContainer) return fakeContainer;
        }
        return null;
    }

    public Container getOpenContainer() {
        return this.containerStack.isEmpty() ? null : this.containerStack.peek();
    }

    public Container getPendingCloseContainer() {
        return this.pendingCloseContainer;
    }

    public byte getNextFakeWindowId() {
        final int id = this.FAKE_ID_COUNTER.getAndIncrement();
        if (id > MAX_FAKE_ID) {
            this.FAKE_ID_COUNTER.set(MIN_FAKE_ID);
            return (byte) MIN_FAKE_ID;
        }
        return (byte) id;
    }

    private void forceCloseContainer(final Container container) {
        this.markPendingClose(container);
        PacketFactory.sendJavaContainerClose(this.getUser(), this.pendingCloseContainer.windowId());
        PacketFactory.sendBedrockContainerClose(this.getUser(), this.pendingCloseContainer.windowId(), ContainerType.NONE);
    }

}
