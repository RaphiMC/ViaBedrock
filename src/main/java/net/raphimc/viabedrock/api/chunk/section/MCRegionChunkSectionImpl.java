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
package net.raphimc.viabedrock.api.chunk.section;

import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import net.raphimc.viabedrock.api.chunk.BedrockBlockArray;
import net.raphimc.viabedrock.api.chunk.BedrockDataPalette;

public class MCRegionChunkSectionImpl implements MCRegionChunkSection {

    private BedrockBlockArray blockPalette;
    private BedrockDataPalette biomePalette;

    @Override
    public DataPalette palette(final PaletteType type) {
        if (type == PaletteType.BLOCKS) {
            return this.blockPalette;
        } else if (type == PaletteType.BIOMES) {
            return this.biomePalette;
        }

        return null;
    }

    @Override
    public void removePalette(final PaletteType type) {
        if (type == PaletteType.BLOCKS) {
            this.blockPalette = null;
        } else if (type == PaletteType.BIOMES) {
            this.biomePalette = null;
        }
    }

    @Override
    public void addPalette(final PaletteType type, final DataPalette palette) {
        if (type == PaletteType.BLOCKS) {
            this.blockPalette = (BedrockBlockArray) palette;
        } else if (type == PaletteType.BIOMES) {
            this.biomePalette = (BedrockDataPalette) palette;
        }
    }

}
