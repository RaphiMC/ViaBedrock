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
package net.raphimc.viabedrock.protocol.types.item;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSet;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class BedrockCreativeItemType extends BedrockItemType {

    public BedrockCreativeItemType(final int blockingId, final Int2ObjectMap<IntSortedSet> blockItemValidBlockStates) {
        super(blockingId, blockItemValidBlockStates, false, false);
    }

    @Override
    public BedrockItem read(ByteBuf buffer) {
        final int netId = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        final BedrockItem item = super.read(buffer);
        item.setNetId(netId);
        return item;
    }

    @Override
    public void write(ByteBuf buffer, BedrockItem value) {
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.netId());
        super.write(buffer, value);
    }

}
