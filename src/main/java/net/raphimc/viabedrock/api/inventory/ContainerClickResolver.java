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
package net.raphimc.viabedrock.api.inventory;

import com.viaversion.nbt.tag.IntTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.ContainerSlot;
import net.raphimc.viabedrock.api.model.container.MenuContainer;
import net.raphimc.viabedrock.api.model.container.dynamic.BundleContainer;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.GameMode;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Translates a Java Edition container click into a list of {@link InventoryOperation}s.
 *
 * <p>Instead of reimplementing the Java Edition click logic, the slots the Java client predicted to have changed are
 * used. That way all click modes (including dragging, double clicking and shift clicking) are handled with the stack
 * limits and slot restrictions the Java client applied, and only the resulting item movement has to be described in
 * terms Bedrock Edition understands.</p>
 */
public class ContainerClickResolver {

    private final UserConnection user;
    private final InventoryTracker inventoryTracker;
    private final ItemRewriter itemRewriter;
    private final Map<BedrockItem, Integer> javaIdCache = new HashMap<>();
    private ContainerSlot clickedSlot;
    private boolean failed;

    public ContainerClickResolver(final UserConnection user) {
        this.user = user;
        this.inventoryTracker = user.get(InventoryTracker.class);
        this.itemRewriter = user.get(ItemRewriter.class);
    }

    /**
     * @param newItems   The new content of every slot which changed
     * @param operations The operations which describe the change
     */
    public record Result(Map<ContainerSlot, BedrockItem> newItems, List<InventoryOperation> operations) {
    }

    /**
     * @param changedSlots   The slots the Java client predicted to have changed, keyed by Java slot index
     * @param carriedItem    The item the Java client predicted to be on the cursor
     * @param clickedSlot    The slot the player clicked, or null if they clicked outside of the screen
     * @param droppedOutside Whether the click was made outside of the screen, which drops the affected items
     * @return The resolved result or null if the click can't be translated
     */
    public Result resolve(final Map<Integer, HashedItem> changedSlots, final HashedItem carriedItem, final ContainerSlot clickedSlot, final boolean droppedOutside) {
        this.clickedSlot = clickedSlot;
        return this.resolve(changedSlots, carriedItem, droppedOutside);
    }

    private Result resolve(final Map<Integer, HashedItem> changedSlots, final HashedItem carriedItem, final boolean droppedOutside) {
        final Map<ContainerSlot, BedrockItem> newItems = new LinkedHashMap<>();

        final MenuContainer menuContainer = this.inventoryTracker.getCurrentMenuContainer();
        for (Map.Entry<Integer, HashedItem> entry : changedSlots.entrySet()) {
            if (menuContainer != null && menuContainer.isIgnoredJavaSlot(entry.getKey())) {
                continue;
            }
            final ContainerSlot containerSlot = this.inventoryTracker.resolveJavaSlot(entry.getKey());
            if (containerSlot == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received container click for unmappable slot: " + entry.getKey());
                return null;
            }
            final BedrockItem newItem = this.resolveItem(containerSlot, entry.getValue());
            if (newItem == null) {
                return null;
            }
            newItems.put(containerSlot, newItem);
        }

        final ContainerSlot cursorSlot = this.inventoryTracker.getCursorSlot();
        final BedrockItem newCursorItem = this.resolveItem(cursorSlot, carriedItem);
        if (newCursorItem == null) {
            return null;
        }
        if (newCursorItem.isDifferent(cursorSlot.getItem()) || newCursorItem.amount() != cursorSlot.getItem().amount()) {
            newItems.put(cursorSlot, newCursorItem);
        }

        final List<InventoryOperation> operations = this.buildOperations(newItems, droppedOutside);
        return this.failed ? null : new Result(newItems, operations);
    }

    /**
     * Reconstructs the full Bedrock item for a slot from the hashed item the Java client sent. The Java client only
     * sends the item id and the amount, so the remaining item data is taken from the item which was moved into the slot.
     */
    private BedrockItem resolveItem(final ContainerSlot containerSlot, final HashedItem hashedItem) {
        if (hashedItem == null || hashedItem.amount() <= 0) {
            return BedrockItem.empty();
        }

        final BedrockItem currentItem = containerSlot.getItem();
        if (this.javaId(currentItem) == hashedItem.identifier()) { // The item stayed in place, only the amount changed
            final BedrockItem newItem = currentItem.copy();
            newItem.setAmount(hashedItem.amount());
            return newItem;
        }

        BedrockItem template = null;
        final BedrockItem cursorItem = this.inventoryTracker.getCursorSlot().getItem();
        if (this.javaId(cursorItem) == hashedItem.identifier()) { // The item came from the cursor
            template = cursorItem;
        } else {
            for (Container container : this.inventoryTracker.getOpenContainers()) {
                for (int slot = 0; slot < container.size() && template == null; slot++) {
                    final BedrockItem item = container.getItem(slot);
                    if (this.javaId(item) == hashedItem.identifier()) {
                        template = item;
                    }
                }
                if (template != null) break;
            }
        }

        if (template == null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received container click with an item which is not present in any open container: " + hashedItem.identifier());
            return null;
        }

        final BedrockItem newItem = template.copy();
        newItem.setAmount(hashedItem.amount());
        newItem.setNetId(null); // The server assigns a new net id to moved items
        return newItem;
    }

    private List<InventoryOperation> buildOperations(final Map<ContainerSlot, BedrockItem> newItems, final boolean droppedOutside) {
        final List<Delta> deltas = new ArrayList<>(newItems.size());
        for (Map.Entry<ContainerSlot, BedrockItem> entry : newItems.entrySet()) {
            final BedrockItem before = entry.getKey().getItem();
            final BedrockItem after = entry.getValue();
            if (!isSameStack(before, after)) {
                deltas.add(new Delta(entry.getKey(), before, after));
            }
        }

        final List<InventoryOperation> operations = new ArrayList<>();
        final boolean[] handled = new boolean[deltas.size()];

        // Two slots exchanging their contents are sent as a dedicated swap action
        for (int i = 0; i < deltas.size(); i++) {
            if (handled[i]) continue;
            for (int j = i + 1; j < deltas.size(); j++) {
                if (handled[j]) continue;
                final Delta a = deltas.get(i);
                final Delta b = deltas.get(j);
                if (isSameStack(a.before, b.after) && isSameStack(b.before, a.after)) {
                    operations.add(new InventoryOperation.Swap(a.slot, b.slot));
                    handled[i] = true;
                    handled[j] = true;
                    break;
                }
            }
        }

        final List<Movement> sources = new ArrayList<>();
        final List<Movement> destinations = new ArrayList<>();
        for (int i = 0; i < deltas.size(); i++) {
            if (handled[i]) continue;
            final Delta delta = deltas.get(i);
            if (!delta.before.isEmpty() && !delta.after.isEmpty() && !delta.before.isDifferent(delta.after)) {
                final int difference = delta.after.amount() - delta.before.amount();
                if (difference > 0) {
                    destinations.add(new Movement(delta.slot, this.javaId(delta.after), difference));
                } else if (difference < 0) {
                    sources.add(new Movement(delta.slot, this.javaId(delta.before), -difference));
                }
            } else {
                if (!delta.before.isEmpty()) {
                    sources.add(new Movement(delta.slot, this.javaId(delta.before), delta.before.amount()));
                }
                if (!delta.after.isEmpty()) {
                    destinations.add(new Movement(delta.slot, this.javaId(delta.after), delta.after.amount()));
                }
            }
        }

        for (Movement destination : destinations) {
            for (Movement source : sources) {
                if (destination.remaining == 0) break;
                if (source.remaining == 0 || source.javaId != destination.javaId) continue;
                final int count = Math.min(source.remaining, destination.remaining);
                operations.add(new InventoryOperation.Transfer(source.slot, destination.slot, count));
                source.remaining -= count;
                destination.remaining -= count;
            }
            if (destination.remaining > 0) {
                // Items can only appear out of nowhere if they came out of a bundle, which keeps its contents in a
                // container of its own instead of in the item
                final ContainerSlot bundleSlot = this.findBundleSlot(destination.javaId, false);
                if (bundleSlot != null) {
                    operations.add(new InventoryOperation.Transfer(bundleSlot, destination.slot, destination.remaining));
                    destination.remaining = 0;
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Container click created " + destination.remaining + " items out of nowhere in " + destination.slot);
                    this.failed = true;
                }
            }
        }

        for (Movement source : sources) {
            if (source.remaining == 0) continue;
            if (droppedOutside) { // Thrown into the world
                operations.add(new InventoryOperation.Drop(source.slot, source.remaining));
                continue;
            }

            // Items which are gone without a destination were either put into a bundle or, in creative mode, deleted
            final ContainerSlot bundleSlot = this.findBundleSlot(source.javaId, true);
            if (bundleSlot != null) {
                operations.add(new InventoryOperation.Transfer(source.slot, bundleSlot, source.remaining));
            } else if (this.user.get(EntityTracker.class).getClientPlayer().javaGameMode() == GameMode.CREATIVE) {
                operations.add(new InventoryOperation.Destroy(source.slot, source.remaining));
            } else {
                // Destroying items in survival is never correct, so rather resync than let the server delete them
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Container click made " + source.remaining + " items in " + source.slot + " disappear");
                this.failed = true;
            }
        }

        return operations;
    }

    /**
     * Bundles don't store their contents in the item but in a container of their own, so moving items in or out of one
     * has to be translated into a transfer to or from that container.
     *
     * @param javaId    The item which was moved
     * @param inserting Whether the item is going into the bundle instead of coming out of it
     * @return The bundle slot to transfer to or from, or null if the clicked slot doesn't hold a usable bundle
     */
    private ContainerSlot findBundleSlot(final int javaId, final boolean inserting) {
        if (this.clickedSlot == null) {
            return null;
        }
        final BundleContainer bundleContainer = this.getBundleContainer(this.clickedSlot.getItem());
        if (bundleContainer == null) {
            return null;
        }

        if (inserting) {
            // The vanilla client always inserts into the first free slot
            for (int slot = 0; slot < bundleContainer.size(); slot++) {
                if (bundleContainer.getItem(slot).isEmpty()) {
                    return new ContainerSlot(bundleContainer, slot);
                }
            }
            return null;
        }

        // Bundles hand out the item which was put in last
        for (int slot = bundleContainer.size() - 1; slot >= 0; slot--) {
            final BedrockItem item = bundleContainer.getItem(slot);
            if (!item.isEmpty() && this.javaId(item) == javaId) {
                return new ContainerSlot(bundleContainer, slot);
            }
        }
        return null;
    }

    private BundleContainer getBundleContainer(final BedrockItem item) {
        if (item == null || item.isEmpty() || item.tag() == null) {
            return null;
        }
        final IntTag bundleIdTag = item.tag().getIntTag("bundle_id");
        if (bundleIdTag == null || bundleIdTag.asInt() == 0) {
            return null;
        }
        return this.inventoryTracker.getDynamicContainer(new FullContainerName(ContainerEnumName.DynamicContainer, bundleIdTag.asInt()));
    }

    private int javaId(final BedrockItem item) {
        if (item == null || item.isEmpty()) {
            return -1;
        }
        return this.javaIdCache.computeIfAbsent(item, i -> this.itemRewriter.javaItem(i).identifier());
    }

    private static boolean isSameStack(final BedrockItem a, final BedrockItem b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        return !a.isDifferent(b) && a.amount() == b.amount();
    }

    private record Delta(ContainerSlot slot, BedrockItem before, BedrockItem after) {
    }

    private static final class Movement {

        private final ContainerSlot slot;
        private final int javaId;
        private int remaining;

        private Movement(final ContainerSlot slot, final int javaId, final int remaining) {
            this.slot = slot;
            this.javaId = javaId;
            this.remaining = remaining;
        }

    }

}
