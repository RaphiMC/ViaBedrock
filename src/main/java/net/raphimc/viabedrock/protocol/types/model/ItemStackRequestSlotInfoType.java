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
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestSlotInfo;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ItemStackRequestSlotInfoType extends Type<ItemStackRequestSlotInfo> {

    public ItemStackRequestSlotInfoType() {
        super(ItemStackRequestSlotInfo.class);
    }

    @Override
    public ItemStackRequestSlotInfo read(final ByteBuf buffer) {
        return new ItemStackRequestSlotInfo(
                BedrockTypes.FULL_CONTAINER_NAME.read(buffer), // container name
                buffer.readUnsignedByte(), // slot
                BedrockTypes.VAR_INT.read(buffer) // stack network id
        );
    }

    @Override
    public void write(final ByteBuf buffer, final ItemStackRequestSlotInfo value) {
        BedrockTypes.FULL_CONTAINER_NAME.write(buffer, value.containerName()); // container name
        buffer.writeByte(value.slot()); // slot
        BedrockTypes.VAR_INT.write(buffer, value.stackNetworkId()); // stack network id
    }

}
