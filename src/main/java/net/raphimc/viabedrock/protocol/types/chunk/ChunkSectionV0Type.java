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
package net.raphimc.viabedrock.protocol.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockBlockArray;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSection;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSectionImpl;

public class ChunkSectionV0Type extends Type<BedrockChunkSection> {

    public ChunkSectionV0Type() {
        super(BedrockChunkSection.class);
    }

    @Override
    public BedrockChunkSection read(ByteBuf buffer) throws Exception {
        final BedrockBlockArray blockArray = new BedrockBlockArray();
        buffer.readBytes(blockArray.getBlocks()); // block ids
        buffer.readBytes(blockArray.getData().getHandle()); // block data

        final BedrockChunkSection chunkSection = new BedrockChunkSectionImpl();
        chunkSection.addPalette(PaletteType.BLOCKS, blockArray);
        return chunkSection;
    }

    @Override
    public void write(ByteBuf buffer, BedrockChunkSection value) throws Exception {
        final BedrockBlockArray blockArray = (BedrockBlockArray) value.palette(PaletteType.BLOCKS);
        buffer.writeBytes(blockArray.getBlocks()); // block ids
        buffer.writeBytes(blockArray.getData().getHandle()); // block data
    }

}
