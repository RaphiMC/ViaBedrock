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
package net.raphimc.viabedrock.api.chunk;

import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_18;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSection;

import java.util.ArrayList;
import java.util.List;

public class BedrockChunk extends Chunk1_18 {

    private boolean requestSubChunks = false;

    public BedrockChunk(final int x, final int z, final BedrockChunkSection[] sections) {
        this(x, z, sections, new CompoundTag(), new ArrayList<>());
    }

    public BedrockChunk(final int x, final int z, final BedrockChunkSection[] sections, final CompoundTag heightMap, final List<BlockEntity> blockEntities) {
        super(x, z, sections, heightMap, blockEntities);
    }

    @Override
    public BedrockChunkSection[] getSections() {
        return (BedrockChunkSection[]) super.getSections();
    }

    public boolean isRequestSubChunks() {
        return this.requestSubChunks;
    }

    public void setRequestSubChunks(final boolean requestSubChunks) {
        this.requestSubChunks = requestSubChunks;
    }

}
