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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntImmutablePair;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntPair;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.types.Chunk1_18Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.Protocol1_19_3To1_19_1;
import com.viaversion.viaversion.util.MathUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.rewriter.DimensionIdRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ChunkTracker extends StoredObject {

    private final int dimensionId;
    private final int minY;
    private final int worldHeight;
    private final Chunk1_18Type chunkType;

    public ChunkTracker(final UserConnection user, final int dimensionId) {
        super(user);
        final GameSessionStorage gameSession = user.get(GameSessionStorage.class);

        this.dimensionId = dimensionId;

        final CompoundTag registries = gameSession.getJavaRegistries();
        final String dimensionKey = DimensionIdRewriter.dimensionIdToDimensionKey(this.dimensionId);
        final CompoundTag dimensionRegistry = registries.get("minecraft:dimension_type");
        final ListTag dimensions = dimensionRegistry.get("value");
        final CompoundTag biomeRegistry = registries.get("minecraft:worldgen/biome");
        final ListTag biomes = biomeRegistry.get("value");

        final IntIntPair pair = dimensions.getValue()
                .stream()
                .map(t -> (CompoundTag) t)
                .filter(t -> t.get("name").getValue().toString().equals(dimensionKey))
                .findFirst()
                .map(t -> (CompoundTag) t.get("element"))
                .map(t -> new IntIntImmutablePair(((NumberTag) t.get("min_y")).asInt(), ((NumberTag) t.get("height")).asInt()))
                .orElse(null);
        if (pair == null) {
            throw new IllegalStateException("Could not find dimension min_y/height for dimension " + dimensionKey);
        }
        this.minY = pair.keyInt();
        this.worldHeight = pair.valueInt();

        this.chunkType = new Chunk1_18Type(this.worldHeight >> 4, MathUtil.ceilLog2(Protocol1_19_3To1_19_1.MAPPINGS.getBlockStateMappings().mappedSize()), MathUtil.ceilLog2(biomes.size()));
    }

    public void requestSubChunks(final int chunkX, final int chunkZ, final int from, final int to) throws Exception {
        final PacketWrapper subChunkRequest = PacketWrapper.create(ServerboundBedrockPackets.SUB_CHUNK_REQUEST, this.getUser());
        subChunkRequest.write(BedrockTypes.VAR_INT, this.dimensionId); // dimension id
        subChunkRequest.write(BedrockTypes.POSITION_3I, new Position(chunkX, 0, chunkZ)); // base position
        subChunkRequest.write(BedrockTypes.INT_LE, to - from); // sub chunk offset count
        for (int i = from; i < to; i++) {
            subChunkRequest.write(Type.BYTE, (byte) 0); // x offset
            subChunkRequest.write(Type.BYTE, (byte) i); // y offset
            subChunkRequest.write(Type.BYTE, (byte) 0); // z offset
        }
        subChunkRequest.sendToServer(BedrockProtocol.class);
    }

    /*
                    final ChunkSection[] sections = new ChunkSection[chunkTracker.getWorldHeight() >> 4];
                    for (int i = 0; i < sections.length; i++) {
                        sections[i] = new ChunkSectionImpl(true);
                        sections[i].getLight().setBlockLight(new NibbleArray(ChunkSection.SIZE).getHandle());
                        sections[i].getLight().setSkyLight(new NibbleArray(ChunkSection.SIZE).getHandle());
                        sections[i].addPalette(PaletteType.BIOMES, new DataPaletteImpl(ChunkSection.BIOME_SIZE));
                        sections[i].palette(PaletteType.BLOCKS).addId(0);
                        sections[i].palette(PaletteType.BIOMES).addId(0);
                        if (i == 5) {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    sections[i].palette(PaletteType.BLOCKS).setIdAt(x, 5, z, 1);
                                    sections[i].setNonAirBlocksCount(sections[i].getNonAirBlocksCount() + 1);
                                }
                            }
                        }
                    }

                    final Chunk chunk = new Chunk1_18(chunkX, chunkZ, sections, new CompoundTag(), new ArrayList<>());
                    final BitSet emptyLightMask = new BitSet();
                    emptyLightMask.set(0, sections.length + 2);
                    wrapper.write(chunkTracker.getChunkType(), chunk);
                    wrapper.write(Type.BOOLEAN, false);
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, new long[0]);
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, new long[0]);
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, emptyLightMask.toLongArray());
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, emptyLightMask.toLongArray());
                    wrapper.write(Type.VAR_INT, 0);
                    wrapper.write(Type.VAR_INT, 0);
     */

    public int getDimensionId() {
        return this.dimensionId;
    }

    public int getMinY() {
        return this.minY;
    }

    public int getWorldHeight() {
        return this.worldHeight;
    }

    public Type<Chunk> getChunkType() {
        return this.chunkType;
    }

}
