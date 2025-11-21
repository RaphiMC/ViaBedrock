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
package net.raphimc.viabedrock.experimental.types.inventory;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.experimental.model.inventory.InventorySource;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.InventorySourceType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.InventorySource_InventorySourceFlags;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class InventorySourcePacketType extends Type<InventorySource> {

    public InventorySourcePacketType() {
        super(InventorySource.class);
    }

    @Override
    public InventorySource read(ByteBuf buffer) {
        int rawTypeId = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        InventorySourceType type = InventorySourceType.getByValue(rawTypeId);
        if (type == null) {
            throw new IllegalStateException("Invalid inventory source type id: " + rawTypeId);
        }

        switch (type) {
            case ContainerInventory, NonImplementedFeatureTODO -> {
                return new InventorySource(type, BedrockTypes.VAR_INT.read(buffer), InventorySource_InventorySourceFlags.NoFlag);
            }
            case WorldInteraction -> {
                int rawSourceFlagId = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
                InventorySource_InventorySourceFlags flag = InventorySource_InventorySourceFlags.getByValue(rawSourceFlagId);
                if (flag == null) {
                    throw new IllegalStateException("Invalid inventory source flag id: " + rawSourceFlagId);
                }

                return new InventorySource(type, ContainerID.CONTAINER_ID_NONE.getValue(), flag);
            }
            default -> {
                return new InventorySource(type, ContainerID.CONTAINER_ID_NONE.getValue(), InventorySource_InventorySourceFlags.NoFlag);
            }
        }
    }

    @Override
    public void write(ByteBuf buffer, InventorySource value) {
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.type().getValue());

        switch (value.type()) {
            case ContainerInventory, NonImplementedFeatureTODO -> BedrockTypes.VAR_INT.write(buffer, value.containerId());
            case WorldInteraction -> BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.flags().getValue());
        }
    }
}