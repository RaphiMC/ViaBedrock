/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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

import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.api.chunk.BedrockDataPalette;
import net.raphimc.viabedrock.api.chunk.section.AnvilChunkSection;
import net.raphimc.viabedrock.api.chunk.section.AnvilChunkSectionImpl;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.List;

public class ChunkSectionV8Type extends Type<AnvilChunkSection> {

    public ChunkSectionV8Type() {
        super(AnvilChunkSection.class);
    }

    @Override
    public AnvilChunkSection read(ByteBuf buffer) throws Exception {
        final AnvilChunkSectionImpl chunkSection = new AnvilChunkSectionImpl();
        final short layers = buffer.readUnsignedByte(); // layer count
        for (int i = 0; i < layers; i++) {
            chunkSection.addPalette(PaletteType.BLOCKS, BedrockTypes.BLOCK_PALETTE.read(buffer)); // block palette
        }
        return chunkSection;
    }

    @Override
    public void write(ByteBuf buffer, AnvilChunkSection value) throws Exception {
        final List<DataPalette> palettes = value.palettes(PaletteType.BLOCKS);
        buffer.writeByte(palettes.size()); // layer count
        for (DataPalette palette : palettes) {
            BedrockTypes.BLOCK_PALETTE.write(buffer, (BedrockDataPalette) palette); // block palette
        }
    }

}
