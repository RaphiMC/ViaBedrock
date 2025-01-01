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
package net.raphimc.viabedrock.api.chunk.section;

import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockDataPalette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BedrockChunkSectionImpl implements BedrockChunkSection {

    private final List<DataPalette> blockPalettes = new ArrayList<>();
    private DataPalette biomePalette;
    private List<int[]> pendingBlockUpdates = new ArrayList<>();

    public BedrockChunkSectionImpl() {
    }

    public BedrockChunkSectionImpl(final boolean noPendingBlockUpdates) {
        if (noPendingBlockUpdates) this.pendingBlockUpdates = null;
    }

    @Override
    public int palettesCount(final PaletteType type) {
        if (type == PaletteType.BLOCKS) {
            return this.blockPalettes.size();
        } else if (type == PaletteType.BIOMES) {
            return this.biomePalette != null ? 1 : 0;
        }

        return 0;
    }

    @Override
    public List<DataPalette> palettes(final PaletteType type) {
        if (type == PaletteType.BLOCKS) {
            return this.blockPalettes;
        } else if (type == PaletteType.BIOMES) {
            return this.biomePalette != null ? Collections.singletonList(this.biomePalette) : Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Override
    public void mergeWith(final BedrockChunkSection other) {
        if (!this.hasPendingBlockUpdates()) {
            throw new IllegalStateException("This section already has been merged with another section");
        }

        if (this.blockPalettes.isEmpty()) {
            this.blockPalettes.addAll(other.palettes(PaletteType.BLOCKS));
        }
        if (this.biomePalette == null) {
            this.biomePalette = other.palette(PaletteType.BIOMES);
        }
    }

    @Override
    public boolean hasPendingBlockUpdates() {
        return this.pendingBlockUpdates != null;
    }

    @Override
    public void addPendingBlockUpdate(int x, int y, int z, int layer, int blockState) {
        if (!this.hasPendingBlockUpdates()) {
            throw new IllegalStateException("This section already has been merged with another section");
        }

        this.pendingBlockUpdates.add(new int[]{layer, x, y, z, blockState});
    }

    @Override
    public void applyPendingBlockUpdates(final int airId) {
        if (this.hasPendingBlockUpdates()) {
            for (int[] blockUpdate : this.pendingBlockUpdates) {
                final int layer = blockUpdate[0];
                while (this.blockPalettes.size() <= layer) {
                    final BedrockDataPalette palette = new BedrockDataPalette();
                    palette.addId(airId);
                    this.addPalette(PaletteType.BLOCKS, palette);
                }

                final DataPalette palette = this.blockPalettes.get(layer);
                final int sectionIndex = palette.index(blockUpdate[1], blockUpdate[2], blockUpdate[3]);
                final int blockState = blockUpdate[4];

                if (layer > 0) {
                    final int prevBlockState = this.blockPalettes.get(layer - 1).idAt(sectionIndex);
                    if (prevBlockState == airId) continue;
                }

                palette.setIdAt(sectionIndex, blockState);
            }
            this.pendingBlockUpdates = null;
        }
    }

    @Override
    public void addPalette(final PaletteType type, final DataPalette palette) {
        if (type == PaletteType.BLOCKS) {
            if (palette == null) throw new IllegalArgumentException("Block palette cannot be null");

            if (this.blockPalettes.size() >= 2) {
                throw new IllegalStateException("This section already has two block palettes");
            }

            this.blockPalettes.add(palette);
        } else if (type == PaletteType.BIOMES) {
            this.biomePalette = palette;
        }
    }

    @Override
    public void removePalette(final PaletteType type) {
        if (type == PaletteType.BLOCKS) {
            this.blockPalettes.clear();
        } else if (type == PaletteType.BIOMES) {
            this.biomePalette = null;
        }
    }

}
