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
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestSlotInfo;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;

public abstract class Container {

    protected final UserConnection user;
    protected final byte containerId;
    protected final ContainerType type;
    protected final TextComponent title;
    protected final BlockPosition position;
    protected final BedrockItem[] items;
    protected final Set<String> validBlockTags;

    public Container(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final int size, final String... validBlockTags) {
        this.user = user;
        this.containerId = containerId;
        this.type = type;
        this.title = title;
        this.position = position;
        this.items = BedrockItem.emptyArray(size);
        this.validBlockTags = Set.of(validBlockTags);
    }

    protected Container(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final BlockPosition position, final BedrockItem[] items, final Set<String> validBlockTags) {
        this.user = user;
        this.containerId = containerId;
        this.type = type;
        this.title = title;
        this.position = position;
        this.items = items;
        this.validBlockTags = validBlockTags;
    }

    public void clearItems() {
        for (int i = 0; i < this.size(); i++) {
            this.setItem(i, BedrockItem.empty());
        }
    }

    public Item getJavaItem(final int slot) {
        return this.user.get(ItemRewriter.class).javaItem(this.getItem(slot));
    }

    public Item[] getJavaItems() {
        return this.user.get(ItemRewriter.class).javaItems(this.getItems());
    }

    public BedrockItem getItem(final int slot) {
        return this.items[slot];
    }

    public BedrockItem[] getItems() {
        return Arrays.copyOf(this.items, this.items.length);
    }

    public boolean setItem(final int slot, final BedrockItem item) {
        if (slot < 0 || slot >= this.items.length) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to set item for " + this.type + ", but slot was out of bounds (" + slot + ")");
            return false;
        }

        final BedrockItem oldItem = this.items[slot];
        this.items[slot] = item;
        this.onSlotChanged(slot, oldItem, item);
        return true;
    }

    public boolean setItems(final BedrockItem[] items) {
        if (items.length != this.items.length) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to set items for " + this.type + ", but items array length was not correct (" + items.length + " != " + this.items.length + ")");
            return false;
        }

        for (int i = 0; i < items.length; i++) {
            this.setItem(i, items[i]);
        }
        return true;
    }

    public int javaSlot(final int slot) {
        return slot;
    }

    public byte javaContainerId() {
        return this.containerId();
    }

    public int size() {
        return this.items.length;
    }

    /**
     * @return The amount of slots the Java client expects for this container
     */
    public int javaSize() {
        return this.size();
    }

    /**
     * @return The Java menu type id of this container or -1 if this container is not displayed as a Java menu
     */
    public int javaMenuType() {
        return BedrockProtocol.MAPPINGS.getBedrockToJavaContainers().getOrDefault(this.type, -1);
    }

    public byte containerId() {
        return this.containerId;
    }

    public ContainerType type() {
        return this.type;
    }

    public TextComponent title() {
        return this.title;
    }

    public BlockPosition position() {
        return this.position;
    }

    /**
     * @param slot The Bedrock slot of this container
     * @return The Bedrock container name which addresses the given slot in item stack requests
     */
    public ContainerEnumName bedrockContainerName(final int slot) {
        return ContainerEnumName.LevelEntityContainer;
    }

    /**
     * @param slot The Bedrock slot of this container
     * @return The slot index which addresses the given slot in item stack requests
     */
    public int bedrockRequestSlot(final int slot) {
        return slot;
    }

    /**
     * @param slot The Bedrock slot of this container
     * @return The full container name which addresses the given slot in item stack requests
     */
    public FullContainerName bedrockFullContainerName(final int slot) {
        final ContainerEnumName containerName = this.bedrockContainerName(slot);
        return containerName != null ? new FullContainerName(containerName, null) : null;
    }

    /**
     * @param slot The Bedrock slot of this container
     * @return The slot info which addresses the given slot in item stack requests
     */
    public ItemStackRequestSlotInfo requestSlotInfo(final int slot) {
        final FullContainerName containerName = this.bedrockFullContainerName(slot);
        if (containerName == null) {
            throw new IllegalArgumentException("Slot " + slot + " of " + this.type + " can't be addressed in item stack requests");
        }
        final BedrockItem item = this.getItem(slot);
        final Integer netId = item.isEmpty() ? null : item.netId();
        return new ItemStackRequestSlotInfo(containerName, this.bedrockRequestSlot(slot), netId != null ? netId : 0);
    }

    public boolean isValidBlockTag(final String tag) {
        if (this.validBlockTags.isEmpty()) {
            // Blocks without a block entity (crafting tables, anvils, ...) can't be identified by a tag
            return true;
        } else if (tag == null) {
            return false;
        } else {
            return this.validBlockTags.contains(tag);
        }
    }

    protected void onSlotChanged(final int slot, final BedrockItem oldItem, final BedrockItem newItem) {
    }

}
