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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackNetResult;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackResponse;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Deserializes the Bedrock ItemStackResponse structure. Only reading is implemented because this structure is only ever
 * received from the server.
 */
public class ItemStackResponseType extends Type<ItemStackResponse> {

    public ItemStackResponseType() {
        super(ItemStackResponse.class);
    }

    @Override
    public ItemStackResponse read(final ByteBuf buffer) {
        final ItemStackNetResult result = ItemStackNetResult.getByValue(buffer.readUnsignedByte(), ItemStackNetResult.Error); // result
        final int requestId = BedrockTypes.VAR_INT.read(buffer); // request id

        final List<ItemStackResponse.ContainerInfo> containerInfos = new ArrayList<>();
        if (buffer.readBoolean()) { // has containers
            final int containerCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // container count
            for (int i = 0; i < containerCount; i++) {
                containerInfos.add(readContainerInfo(buffer));
            }
        }

        return new ItemStackResponse(result, requestId, containerInfos);
    }

    @Override
    public void write(final ByteBuf buffer, final ItemStackResponse value) {
        throw new UnsupportedOperationException("Writing item stack responses is not supported");
    }

    private static ItemStackResponse.ContainerInfo readContainerInfo(final ByteBuf buffer) {
        final var containerName = BedrockTypes.FULL_CONTAINER_NAME.read(buffer); // container name
        final int slotCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // slot count
        final List<ItemStackResponse.SlotInfo> slots = new ArrayList<>(slotCount);
        for (int i = 0; i < slotCount; i++) {
            final int slot = buffer.readUnsignedByte(); // slot
            final int hotbarSlot = buffer.readUnsignedByte(); // hotbar slot
            final int count = buffer.readUnsignedByte(); // count
            final int stackNetworkId = buffer.readBoolean() ? BedrockTypes.VAR_INT.read(buffer) : 0; // stack network id
            slots.add(new ItemStackResponse.SlotInfo(
                    slot,
                    hotbarSlot,
                    count,
                    stackNetworkId,
                    BedrockTypes.STRING.read(buffer), // custom name
                    BedrockTypes.STRING.read(buffer), // filtered custom name
                    BedrockTypes.VAR_INT.read(buffer) // durability correction
            ));
        }
        return new ItemStackResponse.ContainerInfo(containerName, slots);
    }

}
