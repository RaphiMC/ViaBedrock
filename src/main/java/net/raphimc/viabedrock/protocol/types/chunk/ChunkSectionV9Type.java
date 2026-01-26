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
package net.raphimc.viabedrock.protocol.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockDataPalette;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSection;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSectionImpl;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.List;

public class ChunkSectionV9Type extends Type<BedrockChunkSection> {

    public ChunkSectionV9Type() {
        super(BedrockChunkSection.class);
    }

    @Override
    public BedrockChunkSection read(ByteBuf buffer) {
        final BedrockChunkSection chunkSection = new BedrockChunkSectionImpl();
        final short layers = buffer.readUnsignedByte(); // layer count
        buffer.readUnsignedByte(); // section y // Unused by Bedrock client
        for (int i = 0; i < layers; i++) {
            chunkSection.addPalette(PaletteType.BLOCKS, BedrockTypes.DATA_PALETTE.read(buffer)); // block palette
        }
        return chunkSection;
    }

    @Override
    public void write(ByteBuf buffer, BedrockChunkSection value) {
        final List<DataPalette> palettes = value.palettes(PaletteType.BLOCKS);
        buffer.writeByte(palettes.size()); // layer count
        buffer.writeByte(0); // section y // Unused by Bedrock client
        for (DataPalette palette : palettes) {
            BedrockTypes.DATA_PALETTE.write(buffer, (BedrockDataPalette) palette); // block palette
        }
    }

}
