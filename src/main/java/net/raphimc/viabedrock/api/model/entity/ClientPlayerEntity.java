/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.api.model.entity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.util.Pair;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.*;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEvents;
import net.raphimc.viabedrock.protocol.model.Position2f;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.PacketSyncStorage;
import net.raphimc.viabedrock.protocol.storage.PlayerListStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ClientPlayerEntity extends PlayerEntity {

    private final AtomicInteger TELEPORT_ID = new AtomicInteger(1);
    private final GameSessionStorage gameSession;

    // Initial spawn and respawning
    private boolean initiallySpawned;
    private boolean respawning;
    private boolean changingDimension;
    private boolean wasInsideUnloadedChunk;

    // Position syncing
    private int pendingTeleportId = 0;
    private boolean waitingForPositionSync = false;

    // Server Authoritative Movement
    private Position3f prevPosition;
    private boolean prevOnGround;
    private long authInput;

    // Misc data
    private int gameType;

    public ClientPlayerEntity(final UserConnection user, final long uniqueId, final long runtimeId, final int javaId, final UUID javaUuid) {
        super(user, uniqueId, runtimeId, javaId, javaUuid);

        this.gameSession = user.get(GameSessionStorage.class);
    }

    @Override
    public void tick() throws Exception {
        super.tick();

        if (this.gameSession.getMovementMode() >= ServerMovementModes.SERVER) {
            this.sendAuthInputPacketToServer(this.initiallySpawned ? PlayModes.SCREEN : PlayModes.NORMAL);
        }

        if (this.respawning && this.gameSession.getMovementMode() == ServerMovementModes.CLIENT) {
            this.sendMovePlayerPacketToServer(MovePlayerModes.RESPAWN);
        }
    }

    public void closeDownloadingTerrainScreen() throws Exception {
        final PacketWrapper gameEvent = PacketWrapper.create(ClientboundPackets1_20_3.GAME_EVENT, this.user);
        gameEvent.write(Type.UNSIGNED_BYTE, GameEvents.LEVEL_CHUNKS_LOAD_START);
        gameEvent.write(Type.FLOAT, 0F); // value
        gameEvent.send(BedrockProtocol.class);
    }

    public void sendPlayerPositionPacketToClient(final boolean keepRotation) throws Exception {
        this.sendPlayerPositionPacketToClient(keepRotation, true);
    }

    public void sendPlayerPositionPacketToClient(final boolean keepRotation, final boolean fakeTeleport) throws Exception {
        final PacketWrapper playerPosition = PacketWrapper.create(ClientboundPackets1_20_3.PLAYER_POSITION, this.user);
        this.writePlayerPositionPacketToClient(playerPosition, keepRotation, fakeTeleport);
        playerPosition.send(BedrockProtocol.class);
    }

    public void writePlayerPositionPacketToClient(final PacketWrapper wrapper, final boolean keepRotation, final boolean fakeTeleport) {
        wrapper.write(Type.DOUBLE, (double) this.position.x()); // x
        wrapper.write(Type.DOUBLE, (double) this.position.y() - this.eyeOffset()); // y
        wrapper.write(Type.DOUBLE, (double) this.position.z()); // z
        wrapper.write(Type.FLOAT, keepRotation ? 0F : this.rotation.y()); // yaw
        wrapper.write(Type.FLOAT, keepRotation ? 0F : this.rotation.x()); // pitch
        wrapper.write(Type.BYTE, (byte) (keepRotation ? 0b11000 : 0)); // flags
        wrapper.write(Type.VAR_INT, this.nextTeleportId() * (fakeTeleport ? -1 : 1)); // teleport id
    }

    public void sendMovePlayerPacketToServer(final short mode) throws Exception {
        final PacketWrapper movePlayer = PacketWrapper.create(ServerboundBedrockPackets.MOVE_PLAYER, this.user);
        this.writeMovementPacketToServer(movePlayer, mode);
        movePlayer.sendToServer(BedrockProtocol.class);
    }

    public void writeMovementPacketToServer(final PacketWrapper wrapper, final short mode) {
        wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, this.runtimeId); // runtime entity id
        wrapper.write(BedrockTypes.POSITION_3F, this.position); // position
        wrapper.write(BedrockTypes.POSITION_3F, this.rotation); // rotation
        wrapper.write(Type.UNSIGNED_BYTE, mode); // mode
        wrapper.write(Type.BOOLEAN, this.onGround); // on ground
        wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // riding runtime entity id
        wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // tick
    }

    public void sendAuthInputPacketToServer(final int playMode) throws Exception {
        if (!this.prevOnGround && this.onGround) {
            this.prevOnGround = true;
            this.sendMovePlayerPacketToServer(MovePlayerModes.NORMAL);
        }
        if (this.prevPosition == null) {
            this.prevPosition = this.position;
        }

        final float[] motion = this.calculateDirectionVector(this.position.x(), this.position.z(), this.prevPosition.x(), this.prevPosition.z(), this.rotation.y());
        this.fixDirectionVector(motion);
        this.prevPosition = this.position;

        final PacketWrapper playerAuthInput = PacketWrapper.create(ServerboundBedrockPackets.PLAYER_AUTH_INPUT, this.user);
        playerAuthInput.write(BedrockTypes.FLOAT_LE, this.rotation.x()); // pitch
        playerAuthInput.write(BedrockTypes.FLOAT_LE, this.rotation.y()); // yaw
        playerAuthInput.write(BedrockTypes.POSITION_3F, this.position); // position
        playerAuthInput.write(BedrockTypes.POSITION_2F, new Position2f(motion[0], motion[1])); // motion
        playerAuthInput.write(BedrockTypes.FLOAT_LE, this.rotation.z()); // head yaw
        playerAuthInput.write(BedrockTypes.UNSIGNED_VAR_LONG, this.authInput); // input flags
        playerAuthInput.write(BedrockTypes.UNSIGNED_VAR_INT, 1); // input mode | 1 = MOUSE
        playerAuthInput.write(BedrockTypes.UNSIGNED_VAR_INT, playMode); // play mode
        playerAuthInput.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // interaction mode | 0 = CROSSHAIR
        playerAuthInput.write(BedrockTypes.UNSIGNED_VAR_LONG, (long) this.age); // tick
        playerAuthInput.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // position delta
        playerAuthInput.write(BedrockTypes.POSITION_2F, new Position2f(0F, 0F)); // analog move vector
        //playerAuthInput.sendToServer(BedrockProtocol.class);

        this.authInput = 0;
    }

    public void sendPlayerActionPacketToServer(final int action, final int face) throws Exception {
        final PacketWrapper playerAction = PacketWrapper.create(ServerboundBedrockPackets.PLAYER_ACTION, this.user);
        playerAction.write(BedrockTypes.UNSIGNED_VAR_LONG, this.runtimeId); // runtime entity id
        playerAction.write(BedrockTypes.VAR_INT, action); // action
        playerAction.write(BedrockTypes.BLOCK_POSITION, new Position(0, 0, 0)); // block position
        playerAction.write(BedrockTypes.BLOCK_POSITION, new Position(0, 0, 0)); // result position
        playerAction.write(BedrockTypes.VAR_INT, face); // face
        playerAction.sendToServer(BedrockProtocol.class);
    }

    public void updatePlayerPosition(final PacketWrapper wrapper, final boolean onGround) throws Exception {
        if (!this.preMove(null, false)) {
            wrapper.cancel();
            return;
        }

        this.onGround = onGround;

        if (this.gameSession.getMovementMode() == ServerMovementModes.CLIENT) {
            this.writeMovementPacketToServer(wrapper, MovePlayerModes.NORMAL);
        } else {
            wrapper.cancel();
        }
    }

    public void updatePlayerPosition(final PacketWrapper wrapper, final double x, final double y, final double z, final boolean onGround) throws Exception {
        final Position3f newPosition = new Position3f((float) x, (float) y + this.eyeOffset(), (float) z);

        if (!this.preMove(newPosition, false)) {
            wrapper.cancel();
            return;
        }

        this.position = newPosition;
        this.onGround = onGround;

        if (this.gameSession.getMovementMode() == ServerMovementModes.CLIENT) {
            this.writeMovementPacketToServer(wrapper, MovePlayerModes.NORMAL);
        } else {
            wrapper.cancel();
        }
    }

    public void updatePlayerPosition(final PacketWrapper wrapper, final double x, final double y, final double z, final float yaw, final float pitch, final boolean onGround) throws Exception {
        final Position3f newPosition = new Position3f((float) x, (float) y + this.eyeOffset(), (float) z);
        final Position3f newRotation = new Position3f(pitch, yaw, yaw);

        if (!this.preMove(newPosition, true)) {
            wrapper.cancel();
            return;
        }

        this.position = newPosition;
        this.rotation = newRotation;
        this.onGround = onGround;

        if (this.gameSession.getMovementMode() == ServerMovementModes.CLIENT) {
            this.writeMovementPacketToServer(wrapper, MovePlayerModes.NORMAL);
        } else {
            wrapper.cancel();
        }
    }

    public void updatePlayerPosition(final PacketWrapper wrapper, final float yaw, final float pitch, final boolean onGround) throws Exception {
        final Position3f newRotation = new Position3f(pitch, yaw, yaw);

        if (!this.preMove(null, false)) {
            wrapper.cancel();
            return;
        }

        this.rotation = newRotation;
        this.onGround = onGround;

        if (this.gameSession.getMovementMode() == ServerMovementModes.CLIENT) {
            this.writeMovementPacketToServer(wrapper, MovePlayerModes.NORMAL);
        } else {
            wrapper.cancel();
        }
    }

    public void confirmTeleport(final int teleportId) throws Exception {
        if (teleportId < 0) { // Fake teleport
            if (this.pendingTeleportId == -teleportId) {
                this.pendingTeleportId = 0;
            }
        } else {
            if (!this.initiallySpawned || this.respawning) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received teleport confirm for teleport id " + teleportId + " but player is not spawned yet");
            }
            if (this.gameSession.getMovementMode() == ServerMovementModes.CLIENT) {
                this.sendPlayerActionPacketToServer(PlayerActions.HANDLED_TELEPORT, 0);
            } else if (this.gameSession.getMovementMode() >= ServerMovementModes.SERVER) {
                this.authInput |= AuthInputActions.HANDLE_TELEPORT;
            }
        }
    }

    public int nextTeleportId() {
        return this.pendingTeleportId = TELEPORT_ID.getAndIncrement();
    }

    @Override
    public void setPosition(final Position3f position) {
        this.prevPosition = position;
        super.setPosition(position);
    }

    @Override
    public void setOnGround(final boolean onGround) {
        this.prevOnGround = onGround;
        super.setOnGround(onGround);
    }

    @Override
    public String name() {
        final PlayerListStorage playerList = this.user.get(PlayerListStorage.class);
        final Pair<Long, String> entry = playerList.getPlayer(this.javaUuid);
        if (entry != null) {
            return entry.value();
        }

        return this.name;
    }

    public boolean isInitiallySpawned() {
        return this.initiallySpawned;
    }

    public void setInitiallySpawned() {
        this.initiallySpawned = true;
        this.respawning = false;
    }

    public boolean isRespawning() {
        return this.respawning;
    }

    public void setRespawning(final boolean respawning) {
        this.respawning = respawning;
    }

    public boolean isChangingDimension() {
        return this.changingDimension;
    }

    public void setChangingDimension(final boolean changingDimension) {
        this.changingDimension = changingDimension;
    }

    public int getGameType() {
        return this.gameType;
    }

    public void setGameType(final int gameType) {
        this.gameType = gameType;
    }

    private boolean preMove(final Position3f newPosition, final boolean positionLook) throws Exception {
        final ChunkTracker chunkTracker = this.user.get(ChunkTracker.class);

        // Waiting for position sync
        if (this.waitingForPositionSync) {
            if (this.pendingTeleportId == 0 && positionLook) {
                this.waitingForPositionSync = false;
            }
            return false;
        }
        // Is in unloaded chunk
        if (chunkTracker.isInUnloadedChunkSection(this.position)) {
            this.wasInsideUnloadedChunk = true;
            if (!this.position.equals(newPosition)) {
                if (!this.initiallySpawned || this.respawning || this.changingDimension) {
                    this.sendPlayerPositionPacketToClient(false);
                } else {
                    this.waitingForPositionSync = true;
                    this.sendPlayerPositionPacketToClient(true);
                }
            }
            return false;
        } else if (this.wasInsideUnloadedChunk) {
            this.wasInsideUnloadedChunk = false;
            this.waitingForPositionSync = true;
            this.sendPlayerPositionPacketToClient(true);

            if (this.changingDimension) {
                this.user.get(PacketSyncStorage.class).syncWithClient(() -> {
                    this.sendPlayerActionPacketToServer(PlayerActions.DIMENSION_CHANGE_SUCCESS, 0);
                    this.closeDownloadingTerrainScreen();
                    changingDimension = false;
                    respawning = false;
                    return null;
                });
            }

            return false;
        }
        // Loaded -> Unloaded chunk
        if (newPosition != null && chunkTracker.isInUnloadedChunkSection(newPosition)) {
            this.waitingForPositionSync = true;
            this.sendPlayerPositionPacketToClient(true);
            return false;
        }
        // Not spawned yet or respawning
        if (!this.initiallySpawned || this.respawning || this.changingDimension) {
            if (!this.position.equals(newPosition)) {
                this.sendPlayerPositionPacketToClient(false);
            }
            return false;
        }

        return true;
    }

    private float[] calculateDirectionVector(final float x, final float z, final float prevX, final float prevZ, final float yaw) {
        final float dx = x - prevX;
        final float dz = z - prevZ;

        final double magnitude = Math.sqrt(dx * dx + dz * dz);
        double directionX = magnitude > 0 ? dx / magnitude : 0;
        double directionZ = magnitude > 0 ? dz / magnitude : 0;

        directionX = Math.max(-1, Math.min(1, directionX));
        directionZ = Math.max(-1, Math.min(1, directionZ));

        final double angle = Math.toRadians(-yaw);
        final double newDirectionX = directionX * Math.cos(angle) - directionZ * Math.sin(angle);
        final double newDirectionZ = directionX * Math.sin(angle) + directionZ * Math.cos(angle);
        directionX = newDirectionX;
        directionZ = newDirectionZ;

        return new float[]{(float) directionX, (float) directionZ};
    }

    private void fixDirectionVector(final float[] vector) {
        if (Math.abs(vector[0]) <= 0.5F) {
            vector[0] = 0;
        }
        if (Math.abs(vector[1]) <= 0.5F) {
            vector[1] = 0;
        }

        if (Math.abs(vector[0]) <= 0.8F) {
            vector[0] = Math.signum(vector[0]) * 0.70710677F;
        }
        if (Math.abs(vector[1]) <= 0.8F) {
            vector[1] = Math.signum(vector[1]) * 0.70710677F;
        }

        if (Math.abs(vector[0]) > 0.8F) {
            vector[0] = Math.signum(vector[0]);
        }
        if (Math.abs(vector[1]) > 0.8F) {
            vector[1] = Math.signum(vector[1]);
        }
    }

}
