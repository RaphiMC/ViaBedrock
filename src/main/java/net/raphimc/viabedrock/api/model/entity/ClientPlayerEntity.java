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
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.util.Pair;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.*;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEventType;
import net.raphimc.viabedrock.protocol.model.Position2f;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
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

    public ClientPlayerEntity(final UserConnection user, final long uniqueId, final long runtimeId, final UUID javaUuid) {
        super(user, uniqueId, runtimeId, 0, javaUuid);

        this.gameSession = user.get(GameSessionStorage.class);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.gameSession.getMovementMode() != ServerAuthMovementMode.ClientAuthoritative) {
            this.sendAuthInputPacketToServer(this.initiallySpawned ? ClientPlayMode.Screen : ClientPlayMode.Normal);
        }
    }

    public void closeDownloadingTerrainScreen() {
        final PacketWrapper gameEvent = PacketWrapper.create(ClientboundPackets1_20_5.GAME_EVENT, this.user);
        gameEvent.write(Types.UNSIGNED_BYTE, (short) GameEventType.LEVEL_CHUNKS_LOAD_START.ordinal()); // event id
        gameEvent.write(Types.FLOAT, 0F); // value
        gameEvent.send(BedrockProtocol.class);
    }

    public void sendPlayerPositionPacketToClient(final boolean keepRotation) {
        final PacketWrapper playerPosition = PacketWrapper.create(ClientboundPackets1_20_5.PLAYER_POSITION, this.user);
        this.writePlayerPositionPacketToClient(playerPosition, keepRotation, true);
        playerPosition.send(BedrockProtocol.class);
    }

    public void writePlayerPositionPacketToClient(final PacketWrapper wrapper, final boolean keepRotation, final boolean fakeTeleport) {
        wrapper.write(Types.DOUBLE, (double) this.position.x()); // x
        wrapper.write(Types.DOUBLE, (double) this.position.y() - this.eyeOffset()); // y
        wrapper.write(Types.DOUBLE, (double) this.position.z()); // z
        wrapper.write(Types.FLOAT, keepRotation ? 0F : this.rotation.y()); // yaw
        wrapper.write(Types.FLOAT, keepRotation ? 0F : this.rotation.x()); // pitch
        wrapper.write(Types.BYTE, (byte) (keepRotation ? 0b11000 : 0)); // flags
        wrapper.write(Types.VAR_INT, this.nextTeleportId() * (fakeTeleport ? -1 : 1)); // teleport id
    }

    public void sendMovePlayerPacketToServer(final PlayerPositionModeComponent_PositionMode mode) {
        final PacketWrapper movePlayer = PacketWrapper.create(ServerboundBedrockPackets.MOVE_PLAYER, this.user);
        this.writeMovementPacketToServer(movePlayer, mode);
        movePlayer.sendToServer(BedrockProtocol.class);
    }

    public void writeMovementPacketToServer(final PacketWrapper wrapper, final PlayerPositionModeComponent_PositionMode mode) {
        wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, this.runtimeId); // runtime entity id
        wrapper.write(BedrockTypes.POSITION_3F, this.position); // position
        wrapper.write(BedrockTypes.POSITION_3F, this.rotation); // rotation
        wrapper.write(Types.UNSIGNED_BYTE, (short) mode.getValue()); // mode
        wrapper.write(Types.BOOLEAN, this.onGround); // on ground
        wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // riding runtime entity id
        wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // tick
    }

    public void sendAuthInputPacketToServer(final ClientPlayMode playMode) {
        if (!this.prevOnGround && this.onGround) {
            this.prevOnGround = true;
            this.sendMovePlayerPacketToServer(PlayerPositionModeComponent_PositionMode.Normal);
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
        playerAuthInput.write(BedrockTypes.UNSIGNED_VAR_INT, InputMode.Mouse.getValue()); // input mode
        playerAuthInput.write(BedrockTypes.UNSIGNED_VAR_INT, playMode.getValue()); // play mode
        playerAuthInput.write(BedrockTypes.UNSIGNED_VAR_INT, NewInteractionModel.Touch.getValue()); // interaction mode
        playerAuthInput.write(BedrockTypes.UNSIGNED_VAR_LONG, (long) this.age); // tick
        playerAuthInput.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // position delta
        playerAuthInput.write(BedrockTypes.POSITION_2F, new Position2f(0F, 0F)); // analog move vector
        //playerAuthInput.sendToServer(BedrockProtocol.class);

        this.authInput = 0;
    }

    public void sendPlayerActionPacketToServer(final PlayerActionType action, final int face) {
        final PacketWrapper playerAction = PacketWrapper.create(ServerboundBedrockPackets.PLAYER_ACTION, this.user);
        playerAction.write(BedrockTypes.UNSIGNED_VAR_LONG, this.runtimeId); // runtime entity id
        playerAction.write(BedrockTypes.VAR_INT, action.getValue()); // action
        playerAction.write(BedrockTypes.BLOCK_POSITION, new BlockPosition(0, 0, 0)); // block position
        playerAction.write(BedrockTypes.BLOCK_POSITION, new BlockPosition(0, 0, 0)); // result position
        playerAction.write(BedrockTypes.VAR_INT, face); // face
        playerAction.sendToServer(BedrockProtocol.class);
    }

    public void updatePlayerPosition(final PacketWrapper wrapper, final boolean onGround) {
        if (!this.preMove(null, false)) {
            wrapper.cancel();
            return;
        }

        this.onGround = onGround;

        if (this.gameSession.getMovementMode() == ServerAuthMovementMode.ClientAuthoritative) {
            this.writeMovementPacketToServer(wrapper, PlayerPositionModeComponent_PositionMode.Normal);
        } else {
            wrapper.cancel();
        }
    }

    public void updatePlayerPosition(final PacketWrapper wrapper, final double x, final double y, final double z, final boolean onGround) {
        final Position3f newPosition = new Position3f((float) x, (float) y + this.eyeOffset(), (float) z);

        if (!this.preMove(newPosition, false)) {
            wrapper.cancel();
            return;
        }

        this.position = newPosition;
        this.onGround = onGround;

        if (this.gameSession.getMovementMode() == ServerAuthMovementMode.ClientAuthoritative) {
            this.writeMovementPacketToServer(wrapper, PlayerPositionModeComponent_PositionMode.Normal);
        } else {
            wrapper.cancel();
        }
    }

    public void updatePlayerPosition(final PacketWrapper wrapper, final double x, final double y, final double z, final float yaw, final float pitch, final boolean onGround) {
        final Position3f newPosition = new Position3f((float) x, (float) y + this.eyeOffset(), (float) z);
        final Position3f newRotation = new Position3f(pitch, yaw, yaw);

        if (!this.preMove(newPosition, true)) {
            wrapper.cancel();
            return;
        }

        this.position = newPosition;
        this.rotation = newRotation;
        this.onGround = onGround;

        if (this.gameSession.getMovementMode() == ServerAuthMovementMode.ClientAuthoritative) {
            this.writeMovementPacketToServer(wrapper, PlayerPositionModeComponent_PositionMode.Normal);
        } else {
            wrapper.cancel();
        }
    }

    public void updatePlayerPosition(final PacketWrapper wrapper, final float yaw, final float pitch, final boolean onGround) {
        final Position3f newRotation = new Position3f(pitch, yaw, yaw);

        if (!this.preMove(null, false)) {
            wrapper.cancel();
            return;
        }

        this.rotation = newRotation;
        this.onGround = onGround;

        if (this.gameSession.getMovementMode() == ServerAuthMovementMode.ClientAuthoritative) {
            this.writeMovementPacketToServer(wrapper, PlayerPositionModeComponent_PositionMode.Normal);
        } else {
            wrapper.cancel();
        }
    }

    public void confirmTeleport(final int teleportId) {
        if (teleportId < 0) { // Fake teleport
            if (this.pendingTeleportId == -teleportId) {
                this.pendingTeleportId = 0;
            }
        } else {
            if (!this.initiallySpawned || this.respawning) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received teleport confirm for teleport id " + teleportId + " but player is not spawned yet");
            }
            if (this.gameSession.getMovementMode() == ServerAuthMovementMode.ClientAuthoritative) {
                this.sendPlayerActionPacketToServer(PlayerActionType.HandledTeleport, 0);
            } else {
                this.authInput |= PlayerAuthInputPacket_InputData.HandledTeleport.getValue();
            }
        }
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

    private int nextTeleportId() {
        return this.pendingTeleportId = TELEPORT_ID.getAndIncrement();
    }

    private boolean preMove(final Position3f newPosition, final boolean positionLook) {
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
