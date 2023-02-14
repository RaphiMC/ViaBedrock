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

    private boolean initiallySpawned;
    private boolean respawning;

    public ClientPlayerEntity(final long uniqueId, final long runtimeId, final int javaId) {
        super(uniqueId, runtimeId, javaId, Entity1_19_3Types.PLAYER);
    }

    @Override
    public void tick(final EntityTracker entityTracker) throws Exception {
        super.tick(entityTracker);

        if (this.isRespawning()) {
            this.sendMovementPacketToServer(entityTracker.getUser(), MovePlayerMode.RESPAWN);
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

    public void sendMovementPacketToServer(final UserConnection user, final short mode) throws Exception {
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

    public void sendPlayerPositionPacketToClient(final UserConnection user) throws Exception {
        final PacketWrapper playerPosition = PacketWrapper.create(ClientboundPackets1_19_3.PLAYER_POSITION, user);
        playerPosition.write(Type.DOUBLE, (double) position.x()); // x
        playerPosition.write(Type.DOUBLE, (double) position.y() - 1.62D); // y
        playerPosition.write(Type.DOUBLE, (double) position.z()); // z
        playerPosition.write(Type.FLOAT, this.rotation.y()); // yaw
        playerPosition.write(Type.FLOAT, this.rotation.x()); // pitch
        playerPosition.write(Type.BYTE, (byte) 0); // flags
        playerPosition.write(Type.VAR_INT, 0); // teleport id
        playerPosition.write(Type.BOOLEAN, false); // dismount vehicle
        playerPosition.send(BedrockProtocol.class);
    }

    // TODO: Java client movement passthrough only when the player isInitiallySpawned()

    public boolean isInitiallySpawned() {
        return this.initiallySpawned;
    }

    public void setInitiallySpawned(final boolean initiallySpawned) {
        this.initiallySpawned = initiallySpawned;
        this.respawning = false;
    }

    public boolean isRespawning() {
        return this.respawning;
    }

    public void setRespawning(final boolean respawning) {
        this.respawning = respawning;
    }

}
