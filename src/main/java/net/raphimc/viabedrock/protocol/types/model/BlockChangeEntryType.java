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

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.BlockChangeEntry;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class BlockChangeEntryType extends Type<BlockChangeEntry> {

    public BlockChangeEntryType() {
        super(BlockChangeEntry.class);
    }

    @Override
    public BlockChangeEntry read(ByteBuf buffer) {
        final BlockPosition position = BedrockTypes.BLOCK_POSITION.read(buffer);
        final int blockState = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        final int flags = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        final long messageEntityUniqueId = BedrockTypes.UNSIGNED_VAR_LONG.read(buffer);
        final int messageType = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);

        return new BlockChangeEntry(position, blockState, flags, messageEntityUniqueId, messageType);
    }

    @Override
    public void write(ByteBuf buffer, BlockChangeEntry value) {
        BedrockTypes.BLOCK_POSITION.write(buffer, value.position());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.blockState());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.flags());
        BedrockTypes.UNSIGNED_VAR_LONG.write(buffer, value.messageEntityUniqueId());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.messageType());
    }

}
