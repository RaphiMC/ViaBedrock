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
package net.raphimc.viabedrock.protocol.packets;

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.SpawnPositionStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class WorldPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.SET_SPAWN_POSITION, ClientboundPackets1_19_3.SPAWN_POSITION, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final SpawnPositionStorage spawnPositionStorage = wrapper.user().get(SpawnPositionStorage.class);
                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);

                    final int type = wrapper.read(BedrockTypes.VAR_INT); // type
                    if (type != 1) { // WORLD_SPAWN
                        wrapper.cancel();
                        return;
                    }

                    final Position compassPosition = wrapper.read(BedrockTypes.POSITION_3I); // compass position
                    final int dimensionId = wrapper.read(BedrockTypes.VAR_INT); // dimension
                    wrapper.read(BedrockTypes.POSITION_3I); // spawn position

                    spawnPositionStorage.setSpawnPosition(dimensionId, compassPosition);
                    if (chunkTracker.getDimensionId() != dimensionId) {
                        wrapper.cancel();
                        return;
                    }

                    wrapper.write(Type.POSITION1_14, compassPosition); // position
                    wrapper.write(Type.FLOAT, 0F); // angle
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.LEVEL_CHUNK, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.cancel();
                    final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);

                    final int chunkX = wrapper.read(BedrockTypes.VAR_INT); // chunk x
                    final int chunkZ = wrapper.read(BedrockTypes.VAR_INT); // chunk z
                    int sectionCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // sub chunk count

                    if (sectionCount == -1) {
                        sectionCount = 0;
                        throw new RuntimeException("Section count -1 is not implemented yet");
                    } else if (sectionCount == -2) {
                        sectionCount = 0;
                        final int startY = chunkTracker.getMinY() >> 4;
                        final int count = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE); // count
                        chunkTracker.requestSubChunks(chunkX, chunkZ, startY, startY + count);
                    }

                    final boolean cachingEnabled = wrapper.read(Type.BOOLEAN); // caching enabled
                    if (cachingEnabled) {
                        final Long[] blobIds = wrapper.read(BedrockTypes.LONG_ARRAY); // blob ids
                        throw new RuntimeException("Caching is not implemented yet");
                    }
                    final byte[] data = wrapper.read(BedrockTypes.BYTE_ARRAY); // data
                });
            }
        });
        // TODO: Dimension change -> store spawn position
        protocol.registerClientbound(ClientboundBedrockPackets.SET_TIME, ClientboundPackets1_19_3.TIME_UPDATE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(BedrockTypes.VAR_INT, Type.LONG); // game time
                handler(wrapper -> {
                    wrapper.write(Type.LONG, wrapper.get(Type.LONG, 0) % 24000L); // time of day

                    if (!wrapper.user().has(ChunkTracker.class)) { // Bedrock servers might send this packet before the world is initialized
                        wrapper.cancel();
                    }
                });
            }
        });
    }

}
