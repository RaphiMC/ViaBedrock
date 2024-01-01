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

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.util.Pair;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.BitSets;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayerActions;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.RespawnStates;
import net.raphimc.viabedrock.protocol.data.enums.java.ClientStatus;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEvents;
import net.raphimc.viabedrock.protocol.model.PlayerAbilities;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.GameTypeRewriter;
import net.raphimc.viabedrock.protocol.storage.CommandsStorage;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.PlayerListStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.JavaTypes;

import java.util.UUID;

public class ClientPlayerPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.RESPAWN, ClientboundPackets1_20_3.PLAYER_POSITION, wrapper -> {
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final short state = wrapper.read(Type.UNSIGNED_BYTE); // state
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id

            if (state != RespawnStates.SERVER_READY) {
                wrapper.cancel();
                return;
            }

            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.setPosition(position);

            if (!clientPlayer.isInitiallySpawned()) {
                clientPlayer.setRespawning(true);
            } else {
                clientPlayer.sendPlayerActionPacketToServer(PlayerActions.RESPAWN, -1);
                clientPlayer.closeDownloadingTerrainScreen();
            }

            clientPlayer.writePlayerPositionPacketToClient(wrapper, true, true);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_ACTION, null, wrapper -> {
            wrapper.cancel();
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final int action = wrapper.read(BedrockTypes.VAR_INT); // action
            wrapper.read(BedrockTypes.BLOCK_POSITION); // block position
            wrapper.read(BedrockTypes.BLOCK_POSITION); // result position
            wrapper.read(BedrockTypes.VAR_INT); // face

            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            if (action == PlayerActions.DIMENSION_CHANGE_SUCCESS && clientPlayer.isChangingDimension()) {
                clientPlayer.closeDownloadingTerrainScreen();
            }
            // TODO: Handle remaining actions
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CORRECT_PLAYER_MOVE_PREDICTION, null, wrapper -> {
            wrapper.cancel();
            BedrockProtocol.kickForIllegalState(wrapper.user(), "Received CorrectPlayerMovePrediction packet, but the client does not support movement corrections.");
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_PLAYER_GAME_TYPE, ClientboundPackets1_20_3.GAME_EVENT, new PacketHandlers() {
            @Override
            protected void register() {
                create(Type.UNSIGNED_BYTE, GameEvents.GAME_MODE_CHANGED); // event id
                handler(wrapper -> {
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                    final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

                    final int gameType = wrapper.read(BedrockTypes.VAR_INT); // game type
                    entityTracker.getClientPlayer().setGameType(gameType);
                    final int gameMode = GameTypeRewriter.getEffectiveGameMode(gameType, gameSession.getLevelGameType());

                    final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_20_3.PLAYER_INFO_UPDATE, wrapper.user());
                    playerInfoUpdate.write(JavaTypes.PROFILE_ACTIONS_ENUM, BitSets.create(6, 2)); // actions | UPDATE_GAME_MODE
                    playerInfoUpdate.write(Type.VAR_INT, 1); // length
                    playerInfoUpdate.write(Type.UUID, entityTracker.getClientPlayer().javaUuid()); // uuid
                    playerInfoUpdate.write(Type.VAR_INT, gameMode); // game mode
                    playerInfoUpdate.send(BedrockProtocol.class);

                    wrapper.write(Type.FLOAT, (float) gameMode); // value
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_DEFAULT_GAME_TYPE, ClientboundPackets1_20_3.GAME_EVENT, new PacketHandlers() {
            @Override
            protected void register() {
                create(Type.UNSIGNED_BYTE, GameEvents.GAME_MODE_CHANGED); // event id
                handler(wrapper -> {
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                    final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

                    final int gameType = wrapper.read(BedrockTypes.VAR_INT); // game type
                    gameSession.setLevelGameType(gameType);
                    final int gameMode = GameTypeRewriter.getEffectiveGameMode(entityTracker.getClientPlayer().getGameType(), gameType);

                    final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_20_3.PLAYER_INFO_UPDATE, wrapper.user());
                    playerInfoUpdate.write(JavaTypes.PROFILE_ACTIONS_ENUM, BitSets.create(6, 2)); // actions | UPDATE_GAME_MODE
                    playerInfoUpdate.write(Type.VAR_INT, 1); // length
                    playerInfoUpdate.write(Type.UUID, entityTracker.getClientPlayer().javaUuid()); // uuid
                    playerInfoUpdate.write(Type.VAR_INT, gameMode); // game mode
                    playerInfoUpdate.send(BedrockProtocol.class);

                    wrapper.write(Type.FLOAT, (float) gameMode); // value
                });
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

            final int gameMode = GameTypeRewriter.getEffectiveGameMode(gameType, gameSession.getLevelGameType());
            wrapper.write(JavaTypes.PROFILE_ACTIONS_ENUM, BitSets.create(6, 2)); // actions | UPDATE_GAME_MODE
            wrapper.write(Type.VAR_INT, 1); // length
            wrapper.write(Type.UUID, playerListEntry.key()); // uuid
            wrapper.write(Type.VAR_INT, gameMode); // game mode

            if (playerListEntry.key().equals(entityTracker.getClientPlayer().javaUuid())) {
                entityTracker.getClientPlayer().setGameType(gameType);

                final PacketWrapper gameEvent = PacketWrapper.create(ClientboundPackets1_20_3.GAME_EVENT, wrapper.user());
                gameEvent.write(Type.UNSIGNED_BYTE, GameEvents.GAME_MODE_CHANGED); // event id
                gameEvent.write(Type.FLOAT, (float) gameMode); // value
                gameEvent.send(BedrockProtocol.class);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_ABILITIES, null, wrapper -> {
            wrapper.cancel();
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final PlayerAbilities abilities = wrapper.read(BedrockTypes.PLAYER_ABILITIES); // abilities
            if (abilities.uniqueEntityId() == entityTracker.getClientPlayer().uniqueId()) {
                gameSession.setPlayerPermission(abilities.playerPermission());
                gameSession.setCommandPermission(abilities.commandPermission());
                // TODO: Handle remaining fields

                final CommandsStorage commandsStorage = wrapper.user().get(CommandsStorage.class);
                if (commandsStorage != null) {
                    commandsStorage.updateCommandTree();
                }
            }
        });

        protocol.registerServerbound(ServerboundPackets1_20_3.CLIENT_STATUS, ServerboundBedrockPackets.RESPAWN, wrapper -> {
            final int action = wrapper.read(Type.VAR_INT); // action

            if (action != ClientStatus.PERFORM_RESPAWN) {
                wrapper.cancel();
                return;
            }
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            wrapper.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // position
            wrapper.write(Type.UNSIGNED_BYTE, RespawnStates.CLIENT_READY); // state
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

}
