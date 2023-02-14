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

import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.api.chunk.BedrockDataPalette;
import net.raphimc.viabedrock.api.chunk.bitarray.BitArray;
import net.raphimc.viabedrock.api.chunk.bitarray.BitArrayVersion;
import net.raphimc.viabedrock.api.chunk.bitarray.SingletonBitArray;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.function.Function;

public class DataPaletteType extends Type<BedrockDataPalette> {

    private final Function<Tag, Number> tagToRuntimeId;

    public DataPaletteType(final Function<Tag, Number> tagToRuntimeId) {
        super(BedrockDataPalette.class);

        this.tagToRuntimeId = tagToRuntimeId;
    }

    @Override
    public BedrockDataPalette read(ByteBuf buffer) throws Exception {
        final short header = buffer.readUnsignedByte();
        final int bitArrayVersion = header >> 1;
        final boolean isRuntime = (header & 1) != 0;

        if (bitArrayVersion == 127) { // 127 = Same values as previous palette
            return null;
        }

        final BitArray bitArray = BitArrayVersion.get(bitArrayVersion, true).createArray(ChunkSection.SIZE);

        if (!(bitArray instanceof SingletonBitArray)) {
            for (int i = 0; i < bitArray.getWords().length; i++) {
                bitArray.getWords()[i] = buffer.readIntLE();
            }
        }

        final int size = bitArray instanceof SingletonBitArray ? 1 : BedrockTypes.VAR_INT.readPrimitive(buffer);
        final IntList palette = new IntArrayList(size);

        if (isRuntime) {
            for (int i = 0; i < size; i++) {
                palette.add(BedrockTypes.VAR_INT.read(buffer).intValue());
            }
        } else {
            for (int i = 0; i < size; i++) {
                palette.add(this.tagToRuntimeId.apply(BedrockTypes.TAG.read(buffer)).intValue());
            }
        }

        return new BedrockDataPalette(palette, bitArray);
    }

    @Override
    public void write(ByteBuf buffer, BedrockDataPalette value) throws Exception {
        if (value == null) {
            buffer.writeByte(127 << 1);
            return;
        }

        final BitArray bitArray = value.getBitArray();
        final BitArrayVersion version = bitArray.getVersion();
        final boolean isRuntime = true;
        buffer.writeByte((version.getBits() << 1) | (isRuntime ? 1 : 0));

        if (!(bitArray instanceof SingletonBitArray)) {
            for (int i = 0; i < bitArray.getWords().length; i++) {
                buffer.writeIntLE(bitArray.getWords()[i]);
            }
        }

        if (!(bitArray instanceof SingletonBitArray)) {
            BedrockTypes.VAR_INT.writePrimitive(buffer, value.size());
        } else if (value.size() != 1) {
            throw new IllegalStateException("Singleton bit array only supports one palette entry!");
        }

        if (isRuntime) {
            for (int i = 0; i < value.size(); i++) {
                BedrockTypes.VAR_INT.write(buffer, value.idByIndex(i));
            }
        }
    }

}
