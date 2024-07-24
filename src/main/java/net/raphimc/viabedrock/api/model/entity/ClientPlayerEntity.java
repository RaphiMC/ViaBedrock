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
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.util.Pair;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.*;
import net.raphimc.viabedrock.protocol.model.EntityAttribute;
import net.raphimc.viabedrock.protocol.model.PlayerAbilities;
import net.raphimc.viabedrock.protocol.model.Position2f;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.CommandsStorage;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.PlayerListStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ClientPlayerEntity extends PlayerEntity {

    private final AtomicInteger TELEPORT_ID = new AtomicInteger(1);
    private final GameSessionStorage gameSession;

    // Initial spawn and respawning
    private boolean initiallySpawned;
    private boolean changingDimension;
    private boolean wasInsideUnloadedChunk;

    // Position syncing
    private int pendingTeleportId = 0;
    private boolean waitingForPositionSync = false;

    // Server Authoritative Movement
    private Position3f prevPosition;
    private long authInput;

    // Misc data
    private int gameType;

    public ClientPlayerEntity(final UserConnection user, final long runtimeId, final UUID javaUuid, final PlayerAbilities abilities) {
        super(user, runtimeId, 0, javaUuid, abilities);
        this.attributes.put("minecraft:movement", new EntityAttribute("minecraft:movement", 0.7F, 0F, Float.MAX_VALUE));
        this.attributes.put("minecraft:player.hunger", new EntityAttribute("minecraft:player.hunger", 20F, 0F, 20F));
        this.attributes.put("minecraft:player.saturation", new EntityAttribute("minecraft:player.saturation", 5F, 0F, 20F));
        this.attributes.put("minecraft:player.experience", new EntityAttribute("minecraft:player.experience", 0F, 0F, 1F));
        this.attributes.put("minecraft:player.level", new EntityAttribute("minecraft:player.level", 0F, 0F, 24791F));

        this.gameSession = user.get(GameSessionStorage.class);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.gameSession.getMovementMode() != ServerAuthMovementMode.ClientAuthoritative && this.initiallySpawned) {
            this.sendAuthInputPacketToServer(ClientPlayMode.Screen);
        }
    }

    public void sendPlayerPositionPacketToClient(final boolean keepRotation) {
        final PacketWrapper playerPosition = PacketWrapper.create(ClientboundPackets1_21.PLAYER_POSITION, this.user);
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
        wrapper.write(Types.BYTE, (byte) mode.getValue()); // mode
        wrapper.write(Types.BOOLEAN, this.onGround); // on ground
        wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // riding runtime entity id
        wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, 0L); // tick
    }

    public void sendAuthInputPacketToServer(final ClientPlayMode playMode) {
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
        if (!this.preMove(null, null, onGround)) {
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

        if (!this.preMove(newPosition, null, onGround)) {
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

        if (!this.preMove(newPosition, newRotation, onGround)) {
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

        if (!this.preMove(null, newRotation, onGround)) {
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
            if (!this.initiallySpawned) {
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
    public String name() {
        final PlayerListStorage playerList = this.user.get(PlayerListStorage.class);
        final Pair<Long, String> entry = playerList.getPlayer(this.javaUuid);
        if (entry != null) {
            return entry.value();
        }

        return this.name;
    }

    @Override
    public void setAbilities(final PlayerAbilities abilities) {
        final PlayerAbilities prevAbilities = this.abilities;
        super.setAbilities(abilities);
        if (abilities.commandPermission() != prevAbilities.commandPermission()) {
            final CommandsStorage commandsStorage = this.user.get(CommandsStorage.class);
            if (commandsStorage != null) {
                commandsStorage.updateCommandTree();
            }
        }
    }

    public boolean isInitiallySpawned() {
        return this.initiallySpawned;
    }

    public void setInitiallySpawned() {
        this.initiallySpawned = true;
    }

    public boolean isChangingDimension() {
        return this.changingDimension;
    }

    public void setChangingDimension(final boolean changingDimension) {
        this.changingDimension = changingDimension;
    }

    public int gameType() {
        return this.gameType;
    }

    public void setGameType(final int gameType) {
        this.gameType = gameType;
    }

    @Override
    protected boolean translateAttribute(final EntityAttribute attribute, final PacketWrapper javaAttributes, final AtomicInteger attributeCount, final List<EntityData> javaEntityData) {
        return switch (attribute.name()) {
            case "minecraft:health", "minecraft:player.hunger", "minecraft:player.saturation" -> {
                final EntityAttribute health = attribute.name().equals("minecraft:health") ? attribute : this.attributes.get("minecraft:health");
                final EntityAttribute hunger = attribute.name().equals("minecraft:player.hunger") ? attribute : this.attributes.get("minecraft:player.hunger");
                final EntityAttribute saturation = attribute.name().equals("minecraft:player.saturation") ? attribute : this.attributes.get("minecraft:player.saturation");
                final PacketWrapper setHealth = PacketWrapper.create(ClientboundPackets1_21.SET_HEALTH, this.user);
                setHealth.write(Types.FLOAT, health.computeValue(false)); // health
                setHealth.write(Types.VAR_INT, (int) hunger.computeValue(false)); // food
                setHealth.write(Types.FLOAT, saturation.computeValue(false)); // saturation
                setHealth.send(BedrockProtocol.class);

                if (attribute.name().equals("minecraft:health")) { // Call super to translate max health
                    yield super.translateAttribute(attribute, javaAttributes, attributeCount, javaEntityData);
                } else {
                    yield true;
                }
            }
            case "minecraft:player.experience", "minecraft:player.level" -> {
                final EntityAttribute experience = attribute.name().equals("minecraft:player.experience") ? attribute : this.attributes.get("minecraft:player.experience");
                final EntityAttribute level = attribute.name().equals("minecraft:player.level") ? attribute : this.attributes.get("minecraft:player.level");
                final PacketWrapper setExperience = PacketWrapper.create(ClientboundPackets1_21.SET_EXPERIENCE, this.user);
                setExperience.write(Types.FLOAT, experience.computeValue(false)); // bar progress
                setExperience.write(Types.VAR_INT, (int) level.computeValue(false)); // experience level
                setExperience.write(Types.VAR_INT, 0); // total experience
                setExperience.send(BedrockProtocol.class);
                yield true;
            }
            case "minecraft:player.exhaustion" -> true; // Ignore exhaustion
            default -> super.translateAttribute(attribute, javaAttributes, attributeCount, javaEntityData);
        };
    }

    private int nextTeleportId() {
        return this.pendingTeleportId = TELEPORT_ID.getAndIncrement();
    }

    private boolean preMove(final Position3f newPosition, final Position3f newRotation, final boolean newOnGround) {
        final boolean positionLook = newPosition != null && newRotation != null;
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
                if (!this.initiallySpawned || this.changingDimension) {
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
        if (!this.initiallySpawned || this.changingDimension) {
            if (!this.position.equals(newPosition)) {
                this.sendPlayerPositionPacketToClient(false);
            }
            return false;
        }

        if (newPosition != null && !this.position.equals(newPosition)) {
            return true;
        } else if (newRotation != null && !this.rotation.equals(newRotation)) {
            return true;
        } else if (this.onGround != newOnGround) {
            return true;
        }

        return false;
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
