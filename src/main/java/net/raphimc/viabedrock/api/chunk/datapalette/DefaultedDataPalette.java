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
package net.raphimc.viabedrock.api.chunk.datapalette;

import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

public interface DefaultedDataPalette extends DataPalette {

    @Override
    default void forEachMatchingCoordinate(IntPredicate idPredicate, IntConsumer coordinateConsumer) {
        for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
            if (idPredicate.test(idAt(idx))) {
                coordinateConsumer.accept(idx);
            }
        }
    }

}
