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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.IntObjectPair;
import net.lenni0451.mcstructs_bedrock.forms.Form;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.ContainerSlot;
import net.raphimc.viabedrock.api.model.container.MenuContainer;
import net.raphimc.viabedrock.api.model.container.dynamic.BundleContainer;
import net.raphimc.viabedrock.api.model.container.player.ArmorContainer;
import net.raphimc.viabedrock.api.model.container.player.HudContainer;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.api.model.container.player.OffhandContainer;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ModalFormCancelReason;
import net.raphimc.viabedrock.protocol.data.generated.bedrock.CustomItemTags;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.EnchantOption;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.model.TradeOffers;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.logging.Level;

public class InventoryTracker extends StoredObject {

    private final InventoryContainer inventoryContainer = new InventoryContainer(this.user());
    private final OffhandContainer offhandContainer = new OffhandContainer(this.user());
    private final ArmorContainer armorContainer = new ArmorContainer(this.user());
    private final HudContainer hudContainer = new HudContainer(this.user());
    private final Map<FullContainerName, BundleContainer> dynamicContainerRegistry = new HashMap<>();

    private Container currentContainer = null;
    private EnchantOption[] enchantOptions = new EnchantOption[0];
    private TradeOffers tradeOffers = null;
    private String itemRenameText = null;
    private TradeOffers.Offer selectedTrade = null;
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
            final String itemTag = BedrockProtocol.MAPPINGS.getBedrockCustomItemTags().get(this.user().get(ItemRewriter.class).getItems().inverse().get(storageItem.identifier()));
            if (!storageItem.isEmpty() && CustomItemTags.BUNDLE.equals(itemTag)) {
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

    /**
     * Resolves a slot of the Java screen which is currently open to the Bedrock container and slot it belongs to.
     *
     * @param javaSlot The slot index as sent by the Java client
     * @return The resolved container slot or null if the slot can't be mapped to a Bedrock container
     */
    public ContainerSlot resolveJavaSlot(final int javaSlot) {
        if (javaSlot < 0) { // Clicks outside of the screen
            return null;
        }

        final MenuContainer menuContainer = this.getCurrentMenuContainer();
        if (menuContainer != null) {
            final int inventorySlot = menuContainer.bedrockPlayerInventorySlot(javaSlot);
            if (inventorySlot != -1) {
                return new ContainerSlot(this.inventoryContainer, inventorySlot);
            }
            final int containerSlot = menuContainer.bedrockSlot(javaSlot);
            if (containerSlot == -1) {
                return null;
            }
            return new ContainerSlot(menuContainer, containerSlot);
        }

        // The player inventory screen
        if (javaSlot == 0) { // Crafting result
            return new ContainerSlot(this.hudContainer, HudContainer.CRAFTING_RESULT);
        } else if (javaSlot <= 4) { // 2x2 crafting grid
            return new ContainerSlot(this.hudContainer, HudContainer.CRAFTING_INPUT_START + (javaSlot - 1));
        } else if (javaSlot <= 8) { // Armor
            return new ContainerSlot(this.armorContainer, javaSlot - 5);
        } else if (javaSlot <= 35) { // Main inventory
            return new ContainerSlot(this.inventoryContainer, javaSlot);
        } else if (javaSlot <= 44) { // Hotbar
            return new ContainerSlot(this.inventoryContainer, javaSlot - 36);
        } else if (javaSlot == 45) { // Offhand
            return new ContainerSlot(this.offhandContainer, 0);
        }
        return null;
    }

    /**
     * @return The slot holding the item the player is currently dragging around
     */
    public ContainerSlot getCursorSlot() {
        return new ContainerSlot(this.hudContainer, HudContainer.CURSOR);
    }

    /**
     * @return All containers whose slots can be part of the currently open Java screen
     */
    public List<Container> getOpenContainers() {
        final List<Container> containers = new ArrayList<>(4);
        final MenuContainer menuContainer = this.getCurrentMenuContainer();
        if (menuContainer != null) {
            containers.add(menuContainer);
            containers.add(this.inventoryContainer);
        } else {
            containers.add(this.inventoryContainer);
            containers.add(this.armorContainer);
            containers.add(this.offhandContainer);
        }
        containers.add(this.hudContainer); // Holds the cursor item, which is part of every screen
        return containers;
    }

    /**
     * Resolves a slot which was addressed by the server in an item stack response back to the container it belongs to.
     *
     * @param containerName The Bedrock container name
     * @param requestSlot   The slot index within that container
     * @return The resolved container slot or null if the slot is not part of any open container
     */
    public ContainerSlot resolveBedrockSlot(final FullContainerName containerName, final int requestSlot) {
        for (Container container : this.getOpenContainers()) {
            for (int slot = 0; slot < container.size(); slot++) {
                if (container.bedrockRequestSlot(slot) != requestSlot) continue;
                final FullContainerName name = container.bedrockFullContainerName(slot);
                if (name != null && name.name() == containerName.name() && Objects.equals(name.dynamicId(), containerName.dynamicId())) {
                    return new ContainerSlot(container, slot);
                }
            }
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
        final boolean wasMenuContainer = (serverInitiated ? this.currentContainer : this.pendingCloseContainer) instanceof MenuContainer;
        this.currentContainer = null;
        this.pendingCloseContainer = null;
        this.enchantOptions = new EnchantOption[0];
        this.tradeOffers = null;
        this.itemRenameText = null;
        this.selectedTrade = null;
        if (wasMenuContainer) {
            // While a menu was open, the player inventory slots which are not part of the menu were not synced to the
            // Java client, so the whole player inventory has to be resent once the menu is closed.
            PacketFactory.sendJavaContainerSetContent(this.user(), this.inventoryContainer);
        }
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

    /**
     * @return The currently open container if it is displayed as a dedicated Java menu, otherwise null
     */
    public MenuContainer getCurrentMenuContainer() {
        if (this.currentContainer instanceof MenuContainer menuContainer) {
            return menuContainer;
        }
        return null;
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

    /**
     * @return The trade the Java client selected, or null if it didn't select one
     */
    public TradeOffers.Offer getSelectedTrade() {
        return this.selectedTrade;
    }

    public void setSelectedTrade(final TradeOffers.Offer selectedTrade) {
        this.selectedTrade = selectedTrade;
    }

    /**
     * @return The name the player typed into the anvil, or null if they didn't rename anything
     */
    public String getItemRenameText() {
        return this.itemRenameText;
    }

    public void setItemRenameText(final String itemRenameText) {
        this.itemRenameText = itemRenameText;
    }

    /**
     * @return The trades the server sent for the currently open (or about to open) trading screen
     */
    public TradeOffers getTradeOffers() {
        return this.tradeOffers;
    }

    public void setTradeOffers(final TradeOffers tradeOffers) {
        this.tradeOffers = tradeOffers;
    }

    /**
     * @return The enchantments the currently open enchanting table offers
     */
    public EnchantOption[] getEnchantOptions() {
        return this.enchantOptions;
    }

    public void setEnchantOptions(final EnchantOption[] enchantOptions) {
        this.enchantOptions = enchantOptions;
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
