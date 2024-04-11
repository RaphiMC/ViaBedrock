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
package net.raphimc.viabedrock.protocol.packets;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.util.Pair;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.BitSets;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayerActionType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayerPositionModeComponent_PositionMode;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayerRespawnState;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ServerAuthMovementMode;
import net.raphimc.viabedrock.protocol.data.enums.java.ClientCommandAction;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEventType;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerInfoUpdateAction;
import net.raphimc.viabedrock.protocol.model.PlayerAbilities;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.GameTypeRewriter;
import net.raphimc.viabedrock.protocol.storage.CommandsStorage;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.PlayerListStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.UUID;
import java.util.logging.Level;

public class ClientPlayerPackets {

    private static final PacketHandler CLIENT_PLAYER_GAME_MODE_INFO_UPDATE = wrapper -> {
        final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
        final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

        final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_20_3.PLAYER_INFO_UPDATE, wrapper.user());
        playerInfoUpdate.write(Type.PROFILE_ACTIONS_ENUM, BitSets.create(6, PlayerInfoUpdateAction.UPDATE_GAME_MODE.ordinal())); // actions
        playerInfoUpdate.write(Type.VAR_INT, 1); // length
        playerInfoUpdate.write(Type.UUID, entityTracker.getClientPlayer().javaUuid()); // uuid
        playerInfoUpdate.write(Type.VAR_INT, (int) GameTypeRewriter.getEffectiveGameMode(entityTracker.getClientPlayer().getGameType(), gameSession.getLevelGameType())); // game mode
        playerInfoUpdate.send(BedrockProtocol.class);
    };

    private static final PacketHandler CLIENT_PLAYER_GAME_MODE_UPDATE = wrapper -> {
        final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
        final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

        final PacketWrapper gameEvent = PacketWrapper.create(ClientboundPackets1_20_3.GAME_EVENT, wrapper.user());
        gameEvent.write(Type.UNSIGNED_BYTE, (short) GameEventType.CHANGE_GAME_MODE.ordinal()); // event id
        gameEvent.write(Type.FLOAT, (float) GameTypeRewriter.getEffectiveGameMode(entityTracker.getClientPlayer().getGameType(), gameSession.getLevelGameType())); // value
        gameEvent.send(BedrockProtocol.class);
    };

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.RESPAWN, ClientboundPackets1_20_3.PLAYER_POSITION, wrapper -> {
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final short rawState = wrapper.read(Type.UNSIGNED_BYTE); // state
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id

            final PlayerRespawnState state = PlayerRespawnState.getByValue(rawState);
            if (state != PlayerRespawnState.ReadyToSpawn) {
                wrapper.cancel();
                return;
            }

            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.setPosition(position);

            if (!clientPlayer.isInitiallySpawned()) {
                clientPlayer.setRespawning(true);
            } else {
                clientPlayer.sendPlayerActionPacketToServer(PlayerActionType.Respawn, -1);
                clientPlayer.closeDownloadingTerrainScreen();
            }

            clientPlayer.writePlayerPositionPacketToClient(wrapper, true, true);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_ACTION, null, wrapper -> {
            wrapper.cancel();
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final int rawAction = wrapper.read(BedrockTypes.VAR_INT); // action
            final PlayerActionType action = PlayerActionType.getByValue(rawAction);
            if (action == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown player action type: " + rawAction);
                wrapper.cancel();
                return;
            }
            wrapper.read(BedrockTypes.BLOCK_POSITION); // block position
            wrapper.read(BedrockTypes.BLOCK_POSITION); // result position
            wrapper.read(BedrockTypes.VAR_INT); // face

            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            if (action == PlayerActionType.ChangeDimensionAck && clientPlayer.isChangingDimension()) {
                if (wrapper.user().get(GameSessionStorage.class).getMovementMode() == ServerAuthMovementMode.ClientAuthoritative) {
                    clientPlayer.sendMovePlayerPacketToServer(PlayerPositionModeComponent_PositionMode.Normal);
                }
                clientPlayer.sendPlayerPositionPacketToClient(false);
                clientPlayer.closeDownloadingTerrainScreen();
                clientPlayer.setChangingDimension(false);
                clientPlayer.sendPlayerActionPacketToServer(PlayerActionType.ChangeDimensionAck, 0);
            }
            // TODO: Handle remaining actions
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CORRECT_PLAYER_MOVE_PREDICTION, null, wrapper -> {
            throw new UnsupportedOperationException("Received CorrectPlayerMovePrediction packet, but the client does not support movement corrections.");
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_PLAYER_GAME_TYPE, null, new PacketHandlers() {
            @Override
            protected void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                    entityTracker.getClientPlayer().setGameType(wrapper.read(BedrockTypes.VAR_INT)); // game type
                });
                handler(CLIENT_PLAYER_GAME_MODE_INFO_UPDATE);
                handler(CLIENT_PLAYER_GAME_MODE_UPDATE);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_DEFAULT_GAME_TYPE, null, new PacketHandlers() {
            @Override
            protected void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                    gameSession.setLevelGameType(wrapper.read(BedrockTypes.VAR_INT)); // game type
                });
                handler(CLIENT_PLAYER_GAME_MODE_INFO_UPDATE);
                handler(CLIENT_PLAYER_GAME_MODE_UPDATE);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_PLAYER_GAME_TYPE, ClientboundPackets1_20_3.PLAYER_INFO_UPDATE, wrapper -> {
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final PlayerListStorage playerList = wrapper.user().get(PlayerListStorage.class);

            final int gameType = wrapper.read(BedrockTypes.VAR_INT); // game type
            final long playerListId = wrapper.read(BedrockTypes.VAR_LONG); // player list id

            final Pair<UUID, String> playerListEntry = playerList.getPlayer(playerListId);
            if (playerListEntry == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Type.PROFILE_ACTIONS_ENUM, BitSets.create(6, PlayerInfoUpdateAction.UPDATE_GAME_MODE.ordinal())); // actions
            wrapper.write(Type.VAR_INT, 1); // length
            wrapper.write(Type.UUID, playerListEntry.key()); // uuid
            wrapper.write(Type.VAR_INT, (int) GameTypeRewriter.getEffectiveGameMode(gameType, gameSession.getLevelGameType())); // game mode

            if (playerListEntry.key().equals(entityTracker.getClientPlayer().javaUuid())) {
                entityTracker.getClientPlayer().setGameType(gameType);
                CLIENT_PLAYER_GAME_MODE_UPDATE.handle(wrapper);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_ABILITIES, null, wrapper -> {
            wrapper.cancel();
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final PlayerAbilities abilities = wrapper.read(BedrockTypes.PLAYER_ABILITIES); // abilities
            if (abilities.uniqueEntityId() == entityTracker.getClientPlayer().uniqueId()) {
                if (!abilities.equals(gameSession.getAbilities())) {
                    // TODO: Handle remaining fields
                    gameSession.setAbilities(abilities);
                    handleAbilitiesUpdate(wrapper.user());
                }
            }
        });

        protocol.registerServerbound(ServerboundPackets1_20_3.CLIENT_STATUS, ServerboundBedrockPackets.RESPAWN, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final ClientCommandAction action = ClientCommandAction.values()[wrapper.read(Type.VAR_INT)]; // action
            if (action != ClientCommandAction.PERFORM_RESPAWN) {
                wrapper.cancel();
                return;
            }

            wrapper.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // position
            wrapper.write(Type.UNSIGNED_BYTE, (short) PlayerRespawnState.ClientReadyToSpawn.getValue()); // state
            wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, entityTracker.getClientPlayer().runtimeId()); // runtime entity id
        });
        protocol.registerServerbound(ServerboundPackets1_20_3.PLAYER_MOVEMENT, ServerboundBedrockPackets.MOVE_PLAYER, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().updatePlayerPosition(wrapper, wrapper.read(Type.BOOLEAN));
        });
        protocol.registerServerbound(ServerboundPackets1_20_3.PLAYER_POSITION, ServerboundBedrockPackets.MOVE_PLAYER, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().updatePlayerPosition(wrapper, wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(Type.BOOLEAN));
        });
        protocol.registerServerbound(ServerboundPackets1_20_3.PLAYER_POSITION_AND_ROTATION, ServerboundBedrockPackets.MOVE_PLAYER, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().updatePlayerPosition(wrapper, wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(Type.FLOAT), wrapper.read(Type.FLOAT), wrapper.read(Type.BOOLEAN));
        });
        protocol.registerServerbound(ServerboundPackets1_20_3.PLAYER_ROTATION, ServerboundBedrockPackets.MOVE_PLAYER, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().updatePlayerPosition(wrapper, wrapper.read(Type.FLOAT), wrapper.read(Type.FLOAT), wrapper.read(Type.BOOLEAN));
        });
        protocol.registerServerbound(ServerboundPackets1_20_3.TELEPORT_CONFIRM, null, wrapper -> {
            wrapper.cancel();
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().confirmTeleport(wrapper.read(Type.VAR_INT));
        });
    }

    public static void handleAbilitiesUpdate(final UserConnection user) throws Exception {
        final CommandsStorage commandsStorage = user.get(CommandsStorage.class);
        if (commandsStorage != null) {
            commandsStorage.updateCommandTree();
        }
    }

}
