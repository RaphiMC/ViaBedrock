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
package net.raphimc.viabedrock.api.model.inventory;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.MenuType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.InteractPacket_Action;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class InventoryContainer extends Container {

    private byte selectedHotbarSlot = 0;

    public InventoryContainer(final UserConnection user, final byte windowId) {
        super(user, windowId, MenuType.INVENTORY, null, null, 36);
    }

    @Override
    public Item[] getJavaItems() {
        final Item[] combinedItems = StructuredItem.emptyArray(45);
        final Item[] inventoryItems = super.getJavaItems();

        System.arraycopy(inventoryItems, 9, combinedItems, 9, 27);
        System.arraycopy(inventoryItems, 0, combinedItems, 36, 9);

        return combinedItems;
    }

    @Override
    public void setItems(final BedrockItem[] items) {
        if (items.length != this.size()) {
            final BedrockItem[] newItems = BedrockItem.emptyArray(this.size());
            System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
            super.setItems(newItems);
        } else {
            super.setItems(items);
        }
    }

    public void sendSelectedHotbarSlotToClient() {
        final PacketWrapper setCarriedItem = PacketWrapper.create(ClientboundPackets1_21.SET_CARRIED_ITEM, this.user);
        setCarriedItem.write(Types.BYTE, this.selectedHotbarSlot);
        setCarriedItem.send(BedrockProtocol.class);
    }

    public void setSelectedHotbarSlot(final byte slot, final PacketWrapper mobEquipment) {
        final BedrockItem oldItem = this.getItem(this.selectedHotbarSlot);
        final BedrockItem newItem = this.getItem(slot);
        this.selectedHotbarSlot = slot;
        this.onSelectedHotbarSlotChanged(oldItem, newItem, mobEquipment);
    }

    @Override
    protected void onSlotChanged(final int slot, final BedrockItem oldItem, final BedrockItem newItem) {
        super.onSlotChanged(slot, oldItem, newItem);
        if (slot == this.selectedHotbarSlot) {
            final PacketWrapper mobEquipment = PacketWrapper.create(ServerboundBedrockPackets.MOB_EQUIPMENT, this.user);
            this.onSelectedHotbarSlotChanged(oldItem, newItem, mobEquipment);
            mobEquipment.sendToServer(BedrockProtocol.class);
        }
    }

    private void onSelectedHotbarSlotChanged(final BedrockItem oldItem, final BedrockItem newItem, final PacketWrapper mobEquipment) {
        if (!oldItem.equals(newItem)) {
            final PacketWrapper interact = PacketWrapper.create(ServerboundBedrockPackets.INTERACT, this.user);
            interact.write(Types.BYTE, (byte) InteractPacket_Action.InteractUpdate.getValue()); // action
            interact.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // target runtime entity id
            interact.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // mouse position
            interact.sendToServer(BedrockProtocol.class);
        }

        mobEquipment.write(BedrockTypes.UNSIGNED_VAR_LONG, this.user.get(EntityTracker.class).getClientPlayer().runtimeId()); // runtime entity id
        mobEquipment.write(this.user.get(ItemRewriter.class).itemType(), newItem); // item
        mobEquipment.write(Types.BYTE, this.selectedHotbarSlot); // slot
        mobEquipment.write(Types.BYTE, this.selectedHotbarSlot); // selected slot
        mobEquipment.write(Types.BYTE, this.windowId); // window id
    }

}
