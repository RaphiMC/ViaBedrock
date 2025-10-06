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
package net.raphimc.viabedrock.protocol.types.inventory;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.InventorySource;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class InventorySourceType extends Type<InventorySource> {

    public InventorySourceType() {
        super(InventorySource.class);
    }

    @Override
    public InventorySource read(ByteBuf buffer) {
        final InventorySource.Type type = InventorySource.Type.byId(BedrockTypes.VAR_INT.read(buffer));
        final int containerId = BedrockTypes.VAR_INT.read(buffer);
        final InventorySource.Flag flag = InventorySource.Flag.NONE; //TODO

        return new InventorySource(type, containerId, flag);
    }

    @Override
    public void write(ByteBuf buffer, InventorySource value) {
        BedrockTypes.VAR_INT.write(buffer, value.type().id());
        BedrockTypes.VAR_INT.write(buffer, value.containerId());
        BedrockTypes.VAR_INT.write(buffer, value.flag().ordinal()); //TODO: Id instead of ordinal?
    }
}
