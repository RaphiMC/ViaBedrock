/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
import com.viaversion.viaversion.libs.fastutil.ints.IntObjectPair;
import net.lenni0451.mcstructs_bedrock.forms.Form;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.dynamic.BundleContainer;
import net.raphimc.viabedrock.api.model.container.player.*;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ModalFormCancelReason;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class InventoryTracker extends StoredObject {

    private final InventoryContainer inventoryContainer = new InventoryContainer(this.user());
    private final OffhandContainer offhandContainer = new OffhandContainer(this.user());
    private final ArmorContainer armorContainer = new ArmorContainer(this.user());
    private final HudContainer hudContainer = new HudContainer(this.user());
    private final Map<FullContainerName, BundleContainer> dynamicContainerRegistry = new HashMap<>();

    private Container currentContainer = null;
    private Container pendingCloseContainer = null;
    private IntObjectPair<Form> currentForm = null;

    public InventoryTracker(final UserConnection user) {
        super(user);
    }

    public Container getContainerClientbound(final byte containerId, final FullContainerName containerName, final BedrockItem storageItem) {
        if (containerId == this.inventoryContainer.containerId()) return this.inventoryContainer;
        if (containerId == this.offhandContainer.containerId()) return this.offhandContainer;
        if (containerId == this.armorContainer.containerId()) return this.armorContainer;
        if (containerId == this.hudContainer.containerId()) return this.hudContainer;
        if (containerId == ContainerID.CONTAINER_ID_REGISTRY.getValue() && containerName.name() == ContainerEnumName.DynamicContainer) {
            final String itemTag = BedrockProtocol.MAPPINGS.getBedrockItemTags().getOrDefault(this.user().get(ItemRewriter.class).getItems().inverse().get(storageItem.identifier()), "");
            if (!storageItem.isEmpty() && itemTag.equals("bundle")) {
                return this.dynamicContainerRegistry.computeIfAbsent(containerName, cn -> new BundleContainer(this.user(), cn));
            } else {
                return null;
            }
        }
        if (this.currentContainer != null && containerId == this.currentContainer.containerId()) {
            return this.currentContainer;
        }
        return null;
    }

    public Container getContainerServerbound(final byte containerId) {
        if (this.currentContainer != null && containerId == this.currentContainer.javaContainerId()) {
            return this.currentContainer;
        }
        return null;
    }

    public BundleContainer getDynamicContainer(final FullContainerName containerName) {
        return this.dynamicContainerRegistry.get(containerName);
    }

    public void removeDynamicContainer(final FullContainerName containerName) {
        this.dynamicContainerRegistry.remove(containerName);
    }

    public void markPendingClose(final Container container) {
        if (this.pendingCloseContainer != null) {
            throw new IllegalStateException("There is already another container pending close");
        }
        if (this.currentContainer == container) {
            this.currentContainer = null;
        }
        this.pendingCloseContainer = container;
    }

    public void setCurrentContainerClosed(final boolean serverInitiated) {
        if (serverInitiated) {
            PacketFactory.sendBedrockContainerClose(this.user(), this.currentContainer.containerId(), ContainerType.NONE);
        }
        this.hudContainer.setItem(0, BedrockItem.empty()); // TODO: Drop cursor item if needed
        this.currentContainer = null;
        this.pendingCloseContainer = null;
    }

    public void closeCurrentForm() {
        if (this.currentForm == null) {
            throw new IllegalStateException("There is no form currently open");
        }
        final PacketWrapper modalFormResponse = PacketWrapper.create(ServerboundBedrockPackets.MODAL_FORM_RESPONSE, this.user());
        modalFormResponse.write(BedrockTypes.UNSIGNED_VAR_INT, this.currentForm.leftInt()); // id
        modalFormResponse.write(Types.BOOLEAN, false); // has response
        modalFormResponse.write(Types.BOOLEAN, true); // has cancel reason
        modalFormResponse.write(Types.BYTE, (byte) ModalFormCancelReason.UserClosed.getValue()); // cancel reason
        modalFormResponse.sendToServer(BedrockProtocol.class);
        this.currentForm = null;
    }

    public void tick() {
        if (this.currentContainer != null && this.currentContainer.position() != null) {
            if (this.currentContainer.type() == ContainerType.INVENTORY) return;

            final ChunkTracker chunkTracker = this.user().get(ChunkTracker.class);
            final BlockStateRewriter blockStateRewriter = this.user().get(BlockStateRewriter.class);
            final int blockState = chunkTracker.getBlockState(this.currentContainer.position());
            final String tag = blockStateRewriter.tag(blockState);
            if (!this.currentContainer.isValidBlockTag(tag)) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + this.currentContainer.type() + " because block state is not valid for container type: " + blockState);
                this.forceCloseCurrentContainer();
                return;
            }

            final EntityTracker entityTracker = this.user().get(EntityTracker.class);
            final Position3f containerPosition = new Position3f(this.currentContainer.position().x() + 0.5F, this.currentContainer.position().y() + 0.5F, this.currentContainer.position().z() + 0.5F);
            final Position3f playerPosition = entityTracker.getClientPlayer().position();
            if (playerPosition.distanceTo(containerPosition) > 6) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + this.currentContainer.type() + " because player is too far away (" + playerPosition.distanceTo(containerPosition) + " > 6)");
                this.forceCloseCurrentContainer();
            }
        }

        // TODO: Drop Cursor item if no container is open
    }

    public boolean isContainerOpen() {
        return this.currentContainer != null || this.pendingCloseContainer != null;
    }

    public boolean isAnyScreenOpen() {
        return this.isContainerOpen() || this.currentForm != null;
    }

    public InventoryContainer getInventoryContainer() {
        return this.inventoryContainer;
    }

    public OffhandContainer getOffhandContainer() {
        return this.offhandContainer;
    }

    public ArmorContainer getArmorContainer() {
        return this.armorContainer;
    }

    public HudContainer getHudContainer() {
        return this.hudContainer;
    }

    public Container getCurrentContainer() {
        return this.currentContainer;
    }

    public void setCurrentContainer(final Container container) {
        if (this.isContainerOpen()) {
            throw new IllegalStateException("There is already another container open");
        }
        this.currentContainer = container;
    }

    public Container getPendingCloseContainer() {
        return this.pendingCloseContainer;
    }

    public IntObjectPair<Form> getCurrentForm() {
        return this.currentForm;
    }

    public void setCurrentForm(final IntObjectPair<Form> currentForm) {
        this.currentForm = currentForm;
    }

    private void forceCloseCurrentContainer() {
        this.markPendingClose(this.currentContainer);
        PacketFactory.sendJavaContainerClose(this.user(), this.pendingCloseContainer.javaContainerId());
        PacketFactory.sendBedrockContainerClose(this.user(), this.pendingCloseContainer.containerId(), ContainerType.NONE);
    }

}
