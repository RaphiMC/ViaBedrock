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
package net.raphimc.viabedrock.api.model.container.block;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.experimental.ExperimentalPacketFactory;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestInfo;
import net.raphimc.viabedrock.experimental.model.inventory.ItemStackRequestSlotInfo;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestStorage;
import net.raphimc.viabedrock.experimental.storage.InventoryRequestTracker;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.TextProcessingEventOrigin;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class BeaconContainer extends Container {

    public BeaconContainer(UserConnection user, byte containerId, TextComponent title, BlockPosition position) {
        super(user, containerId, ContainerType.BEACON, title, position, 1, "beacon");

        PacketWrapper propertiesPacket = PacketWrapper.create(ClientboundPackets1_21_11.CONTAINER_SET_DATA, user);
        propertiesPacket.write(Types.VAR_INT, (int) containerId);
        propertiesPacket.write(Types.SHORT, (short) 0); // Property ID (Power level)
        // TODO: Dynamically set this based on the beacon's current level
        propertiesPacket.write(Types.SHORT, (short) 4); // Property Value (0-4)
        propertiesPacket.scheduleSend(BedrockProtocol.class);
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        if (slot != 27) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Invalid slot: " + slot);
        }
        return new FullContainerName(ContainerEnumName.BeaconPaymentContainer, null);
    }

    @Override
    public int javaSlot(final int slot) {
        if (slot == 27) {
            return 0;
        } else {
            return super.javaSlot(slot);
        }
    }

    @Override
    public int bedrockSlot(final int slot) {
        if (slot == 0) {
            return 27;
        } else {
            return super.bedrockSlot(slot);
        }
    }

    public void updateEffects(int primaryEffect, int secondaryEffect) {
        InventoryRequestTracker inventoryRequestTracker = this.user.get(InventoryRequestTracker.class);
        InventoryTracker inventoryTracker = this.user.get(InventoryTracker.class);

        //TODO: This is kinda cooked, refactor later
        final String javaIdentifierPrimary = BedrockProtocol.MAPPINGS.getJavaEffects().inverse().get(primaryEffect);
        final String javaIdentifierSecondary = BedrockProtocol.MAPPINGS.getJavaEffects().inverse().get(secondaryEffect);

        final String bedrockIdentifierPrimary = BedrockProtocol.MAPPINGS.getJavaToBedrockEffects().get(javaIdentifierPrimary);
        final String bedrockIdentifierSecondary = BedrockProtocol.MAPPINGS.getJavaToBedrockEffects().get(javaIdentifierSecondary);

        final int bedrockIdPrimary = bedrockIdentifierPrimary == null ? 0 : BedrockProtocol.MAPPINGS.getBedrockEffects().get(bedrockIdentifierPrimary);
        final int bedrockIdSecondary = bedrockIdentifierSecondary == null ? 0 : BedrockProtocol.MAPPINGS.getBedrockEffects().get(bedrockIdentifierSecondary);

        ItemStackRequestInfo requestInfo = new ItemStackRequestInfo(
                inventoryRequestTracker.nextRequestId(),
                List.of(
                        new ItemStackRequestAction.BeaconPaymentAction(
                                bedrockIdPrimary,
                                bedrockIdSecondary
                        ),
                        new ItemStackRequestAction.DestroyAction(
                                1,
                                new ItemStackRequestSlotInfo(
                                        this.getFullContainerName(27),
                                        (byte) 27,
                                        this.getItem(0).netId()
                                )
                        )
                ),
                List.of(),
                TextProcessingEventOrigin.unknown
        );

        List<Container> prevContainers = new ArrayList<>();
        prevContainers.add(this.copy());
        Container prevCursorContainer = inventoryTracker.getHudContainer().copy();

        this.setItem(0, BedrockItem.empty()); // Clear the payment slot

        inventoryRequestTracker.addRequest(new InventoryRequestStorage(requestInfo, 0, prevCursorContainer, prevContainers));
        ExperimentalPacketFactory.sendBedrockInventoryRequest(user, new ItemStackRequestInfo[] {requestInfo});
    }

}
