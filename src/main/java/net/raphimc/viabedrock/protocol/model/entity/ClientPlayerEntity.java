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
package net.raphimc.viabedrock.protocol.model.entity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_3Types;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.MovePlayerMode;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.SpawnPositionStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ClientPlayerEntity extends Entity {

    private boolean spawned;
    private int respawnWaitingTicks = -1;

    public ClientPlayerEntity(final long uniqueId, final long runtimeId, final int javaId) {
        super(uniqueId, runtimeId, javaId, Entity1_19_3Types.PLAYER);
    }

    @Override
    public void tick(final EntityTracker entityTracker) throws Exception {
        super.tick(entityTracker);

        if (this.isRespawning()) {
            this.respawnWaitingTicks++;
        }

        if (this.respawnWaitingTicks >= 40) {
            this.sendMovementPacket(entityTracker.getUser(), MovePlayerMode.RESPAWN);
        }
    }

    public void closeDownloadingTerrainScreen(final UserConnection user) throws Exception {
        final SpawnPositionStorage spawnPositionStorage = user.get(SpawnPositionStorage.class);
        final ChunkTracker chunkTracker = user.get(ChunkTracker.class);

        final PacketWrapper spawnPosition = PacketWrapper.create(ClientboundPackets1_19_3.SPAWN_POSITION, user);
        spawnPosition.write(Type.POSITION1_14, spawnPositionStorage.getSpawnPosition(chunkTracker.getDimensionId())); // position
        spawnPosition.write(Type.FLOAT, 0F); // angle
        spawnPosition.send(BedrockProtocol.class);
    }

    public void sendMovementPacket(final UserConnection user, final short mode) throws Exception {
        final PacketWrapper movePlayer = PacketWrapper.create(ServerboundBedrockPackets.MOVE_PLAYER, user);
        movePlayer.write(BedrockTypes.UNSIGNED_VAR_LONG, this.runtimeId); // runtime entity id
        movePlayer.write(BedrockTypes.POSITION_3F, this.position); // position
        movePlayer.write(BedrockTypes.POSITION_3F, this.rotation); // rotation
        movePlayer.write(Type.UNSIGNED_BYTE, mode); // mode
        movePlayer.write(Type.BOOLEAN, this.onGround); // on ground
        movePlayer.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // riding runtime entity id
        movePlayer.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // tick
        movePlayer.sendToServer(BedrockProtocol.class);
    }

    // TODO: Java client movement passthrough only when the player isSpawned()

    public boolean isSpawned() {
        return this.spawned;
    }

    public void setSpawned(final boolean spawned) {
        this.spawned = spawned;
        this.respawnWaitingTicks = spawned ? -1 : 0;
    }

    public boolean isRespawning() {
        return this.respawnWaitingTicks >= 0;
    }

    public void setRespawning(final boolean respawning) {
        this.respawnWaitingTicks = respawning ? 0 : -1;
    }

}
