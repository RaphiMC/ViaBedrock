/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.util.Pair;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.util.BitSets;
import net.raphimc.viabedrock.api.util.EnumUtil;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.Direction;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PredictionType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.data.enums.java.*;
import net.raphimc.viabedrock.protocol.model.Position2f;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.GameTypeRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class ClientPlayerPackets {

    private static final PacketHandler CLIENT_PLAYER_GAME_MODE_INFO_UPDATE = wrapper -> {
        final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();

        final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_21_9.PLAYER_INFO_UPDATE, wrapper.user());
        playerInfoUpdate.write(Types.PROFILE_ACTIONS_ENUM1_21_4, BitSets.create(8, PlayerInfoUpdateAction.UPDATE_GAME_MODE)); // actions
        playerInfoUpdate.write(Types.VAR_INT, 1); // length
        playerInfoUpdate.write(Types.UUID, clientPlayer.javaUuid()); // uuid
        playerInfoUpdate.write(Types.VAR_INT, clientPlayer.javaGameMode().ordinal()); // game mode
        playerInfoUpdate.send(BedrockProtocol.class);
    };

    private static final PacketHandler CLIENT_PLAYER_GAME_MODE_UPDATE = wrapper -> {
        final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
        PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.CHANGE_GAME_MODE, clientPlayer.javaGameMode().ordinal());
    };

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.RESPAWN, ClientboundPackets1_21_9.RESPAWN, wrapper -> {
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final byte rawState = wrapper.read(Types.BYTE); // state
            final PlayerRespawnState state = PlayerRespawnState.getByValue(rawState);
            if (state == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown PlayerRespawnState: " + rawState);
                wrapper.cancel();
                return;
            }
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id

            switch (state) {
                case ReadyToSpawn -> {
                    final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
                    clientPlayer.setPosition(position);

                    if (clientPlayer.isInitiallySpawned()) {
                        final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                        final GameRulesStorage gameRulesStorage = wrapper.user().get(GameRulesStorage.class);
                        final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
                        final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);

                        if (clientPlayer.isDead() && !gameRulesStorage.<Boolean>getGameRule("keepInventory")) {
                            inventoryTracker.getInventoryContainer().clearItems();
                            inventoryTracker.getOffhandContainer().clearItems();
                            inventoryTracker.getArmorContainer().clearItems();
                            inventoryTracker.getHudContainer().clearItems();
                            // TODO: InventoryTransactionPacket(legacyRequestId=0, legacySlots=[], actions=[], transactionType=INVENTORY_MISMATCH, actionType=0, runtimeEntityId=0, blockPosition=null, blockFace=0, hotbarSlot=0, itemInHand=null, playerPosition=null, clickPosition=null, headPosition=null, usingNetIds=false, blockDefinition=null)
                        }
                        clientPlayer.clearEffects();

                        clientPlayer.setHealth(clientPlayer.attributes().get("minecraft:health").maxValue());
                        clientPlayer.sendPlayerActionPacketToServer(PlayerActionType.Respawn, -1);
                        wrapper.write(Types.VAR_INT, chunkTracker.getDimension().ordinal()); // dimension id
                        wrapper.write(Types.STRING, chunkTracker.getDimension().getKey()); // dimension name
                        wrapper.write(Types.LONG, 0L); // hashed seed
                        wrapper.write(Types.BYTE, (byte) clientPlayer.javaGameMode().ordinal()); // game mode
                        wrapper.write(Types.BYTE, (byte) -1); // previous game mode
                        wrapper.write(Types.BOOLEAN, false); // is debug
                        wrapper.write(Types.BOOLEAN, gameSession.isFlatGenerator()); // is flat
                        wrapper.write(Types.OPTIONAL_GLOBAL_POSITION, null); // last death position
                        wrapper.write(Types.VAR_INT, 0); // portal cooldown
                        wrapper.write(Types.VAR_INT, 64); // sea level
                        wrapper.write(Types.BYTE, (byte) 0x03); // keep data mask
                        wrapper.send(BedrockProtocol.class);
                        clientPlayer.sendAttribute("minecraft:health"); // Ensure health is synced
                        clientPlayer.setAbilities(clientPlayer.abilities()); // Java client always resets abilities on respawn. Resend them
                        PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.LEVEL_CHUNKS_LOAD_START, 0F);
                        if (gameRulesStorage.getGameRule("keepInventory")) {
                            PacketFactory.sendJavaContainerSetContent(wrapper.user(), inventoryTracker.getInventoryContainer()); // Java client always resets inventory on respawn. Resend it
                        }
                        inventoryTracker.getInventoryContainer().sendSelectedHotbarSlotToClient(); // Java client always resets selected hotbar slot on respawn. Resend it
                    }
                    wrapper.cancel();

                    clientPlayer.sendPlayerPositionPacketToClient(Relative.NONE);
                }
                case SearchingForSpawn, ClientReadyToSpawn -> wrapper.cancel();
                default -> throw new IllegalStateException("Unhandled PlayerRespawnState: " + state);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_ACTION, null, wrapper -> {
            wrapper.cancel();
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final int rawAction = wrapper.read(BedrockTypes.VAR_INT); // action
            final PlayerActionType action = PlayerActionType.getByValue(rawAction);
            if (action == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown PlayerActionType: " + rawAction);
                return;
            }
            wrapper.read(BedrockTypes.BLOCK_POSITION); // block position
            wrapper.read(BedrockTypes.BLOCK_POSITION); // result position
            wrapper.read(BedrockTypes.VAR_INT); // face

            if (action == PlayerActionType.ChangeDimensionAck) {
                final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
                if (clientPlayer.dimensionChangeInfo() != null) {
                    clientPlayer.sendPlayerActionPacketToServer(PlayerActionType.ChangeDimensionAck);
                    PacketFactory.sendBedrockLoadingScreen(wrapper.user(), ServerboundLoadingScreenPacketType.EndLoadingScreen, clientPlayer.dimensionChangeInfo().loadingScreenId());
                    clientPlayer.sendPlayerPositionPacketToClient(Relative.NONE);
                    PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.LEVEL_CHUNKS_LOAD_START, 0F);
                    clientPlayer.setDimensionChangeInfo(null);
                }
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CORRECT_PLAYER_MOVE_PREDICTION, ClientboundPackets1_21_9.PLAYER_POSITION, wrapper -> {
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

            final byte rawPredictionType = wrapper.read(Types.BYTE); // prediction type
            final PredictionType predictionType = PredictionType.getByValue(rawPredictionType);
            if (predictionType == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown PredictionType: " + rawPredictionType);
                return;
            }
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            wrapper.read(BedrockTypes.POSITION_3F); // position delta
            wrapper.read(BedrockTypes.POSITION_2F); // vehicle rotation
            if (wrapper.read(Types.BOOLEAN)) {
                wrapper.read(BedrockTypes.FLOAT_LE); // vehicle angular velocity
            }
            switch (predictionType) {
                case Player -> {
                    final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
                    final boolean onGround = wrapper.read(Types.BOOLEAN); // on ground
                    final long tick = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // tick
                    if (tick > clientPlayer.age() || tick < clientPlayer.age() - gameSession.getMovementRewindHistorySize()) {
                        wrapper.cancel();
                        return;
                    }

                    clientPlayer.setPosition(position);
                    clientPlayer.setOnGround(onGround);
                    clientPlayer.writePlayerPositionPacketToClient(wrapper, Relative.union(Relative.ROTATION, Relative.VELOCITY), true);
                }
                case Vehicle -> wrapper.cancel();
                default -> throw new IllegalStateException("Unhandled PredictionType: " + predictionType);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_PLAYER_GAME_TYPE, null, new PacketHandlers() {
            @Override
            protected void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    wrapper.user().get(EntityTracker.class).getClientPlayer().setGameType(GameType.getByValue(wrapper.read(BedrockTypes.VAR_INT), GameType.Undefined)); // game type
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
                    wrapper.user().get(GameSessionStorage.class).setLevelGameType(GameType.getByValue(wrapper.read(BedrockTypes.VAR_INT), GameType.Undefined)); // game type
                    wrapper.user().get(EntityTracker.class).getClientPlayer().updateJavaGameMode();
                });
                handler(CLIENT_PLAYER_GAME_MODE_INFO_UPDATE);
                handler(CLIENT_PLAYER_GAME_MODE_UPDATE);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_PLAYER_GAME_TYPE, ClientboundPackets1_21_9.PLAYER_INFO_UPDATE, wrapper -> {
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final PlayerListStorage playerList = wrapper.user().get(PlayerListStorage.class);

            final GameType gameType = GameType.getByValue(wrapper.read(BedrockTypes.VAR_INT), GameType.Undefined); // game type
            final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // tick

            final Pair<UUID, String> playerListEntry = playerList.getPlayer(uniqueEntityId);
            if (playerListEntry == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.PROFILE_ACTIONS_ENUM1_21_4, BitSets.create(8, PlayerInfoUpdateAction.UPDATE_GAME_MODE)); // actions
            wrapper.write(Types.VAR_INT, 1); // length
            wrapper.write(Types.UUID, playerListEntry.key()); // uuid
            wrapper.write(Types.VAR_INT, GameTypeRewriter.getEffectiveGameMode(gameType, gameSession.getLevelGameType()).ordinal()); // game mode

            if (playerListEntry.key().equals(clientPlayer.javaUuid())) {
                clientPlayer.setGameType(gameType);
                CLIENT_PLAYER_GAME_MODE_UPDATE.handle(wrapper);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_ADVENTURE_SETTINGS, null, wrapper -> {
            wrapper.cancel();
            wrapper.read(Types.BOOLEAN); // no player vs mobs
            wrapper.read(Types.BOOLEAN); // no mobs vs player
            wrapper.user().get(GameSessionStorage.class).setImmutableWorld(wrapper.read(Types.BOOLEAN)); // immutable world
            wrapper.read(Types.BOOLEAN); // show name tags
            wrapper.read(Types.BOOLEAN); // auto jump
        });

        protocol.registerServerbound(ServerboundPackets1_21_6.CLIENT_COMMAND, ServerboundBedrockPackets.RESPAWN, wrapper -> {
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final ClientCommandAction action = ClientCommandAction.values()[wrapper.read(Types.VAR_INT)]; // action

            switch (action) {
                case PERFORM_RESPAWN -> {
                    wrapper.write(BedrockTypes.POSITION_3F, Position3f.ZERO); // position
                    wrapper.write(Types.BYTE, (byte) PlayerRespawnState.ClientReadyToSpawn.getValue()); // state
                    wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                }
                case REQUEST_STATS -> wrapper.cancel();
                default -> throw new IllegalStateException("Unhandled ClientCommandAction: " + action);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.PLAYER_COMMAND, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            wrapper.read(Types.VAR_INT); // entity id
            final PlayerCommandAction action = PlayerCommandAction.values()[wrapper.read(Types.VAR_INT)]; // action
            final int data = wrapper.read(Types.VAR_INT); // data

            switch (action) {
                case START_SPRINTING -> {
                    clientPlayer.setSprinting(true);
                    clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.StartSprinting);
                }
                case STOP_SPRINTING -> {
                    clientPlayer.setSprinting(false);
                    clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.StopSprinting);
                }
                default -> throw new IllegalStateException("Unhandled PlayerCommandAction: " + action);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.USE_ITEM, ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper -> {
            // TODO: Implement armor swapping

            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();

            final int hand = wrapper.read(Types.VAR_INT); // hand
            wrapper.read(Types.VAR_INT); // sequence
            wrapper.read(Types.FLOAT); // yaw
            wrapper.read(Types.FLOAT); // pitch

            // Bedrock can't hold the majority of item in offhand and can't use any either.
            if (hand != InteractionHand.MAIN_HAND.ordinal()) {
                wrapper.cancel();
                return;
            }

            wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemUseTransaction.getValue()); // transaction type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // actions count

            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 1); // action type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_TriggerType.Unknown.getValue()); // trigger type

            wrapper.write(BedrockTypes.BLOCK_POSITION, new BlockPosition(0, 0, 0)); // block position

            // When player isn't right-clicking a block, block face always default back to 255
            wrapper.write(BedrockTypes.VAR_INT, 255); // Block face

            wrapper.write(BedrockTypes.VAR_INT, (int) inventoryContainer.getSelectedHotbarSlot()); // hotbar slot
            wrapper.write(wrapper.user().get(ItemRewriter.class).itemType(), inventoryContainer.getSelectedHotbarItem()); // hand item
            wrapper.write(BedrockTypes.POSITION_3F, entityTracker.getClientPlayer().position()); // player position
            wrapper.write(BedrockTypes.POSITION_3F, Position3f.ZERO); // Click position

            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // block runtime id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_PredictedResult.Failure.getValue()); // predicted result.
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.PLAYER_ACTION, null, wrapper -> {
            wrapper.cancel();
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            final PlayerActionAction action = PlayerActionAction.values()[wrapper.read(Types.VAR_INT)]; // action
            final BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_14); // block position
            final Direction direction = Direction.values()[wrapper.read(Types.UNSIGNED_BYTE)]; // face
            final int sequence = wrapper.read(Types.VAR_INT); // sequence number

            final boolean isMining = action == PlayerActionAction.START_DESTROY_BLOCK || action == PlayerActionAction.ABORT_DESTROY_BLOCK || action == PlayerActionAction.STOP_DESTROY_BLOCK;
            if (isMining && (gameSession.isImmutableWorld() || !clientPlayer.abilities().getBooleanValue(AbilitiesIndex.Mine))) {
                // TODO: Prevent breaking and cancel any packets that would be sent (swing, player action)
                BedrockProtocol.kickForIllegalState(wrapper.user(), "Breaking blocks in protected areas is not handled yet by ViaBedrock");
                PacketFactory.sendJavaBlockUpdate(wrapper.user(), position, chunkTracker.getJavaBlockState(position));
                PacketFactory.sendJavaBlockChangedAck(wrapper.user(), sequence);
                return;
            }

            // TODO: Block breaking: Send correct inventory transactions

            switch (action) {
                case START_DESTROY_BLOCK -> {
                    clientPlayer.sendSwingPacketToServer();
                    clientPlayer.cancelNextSwingPacket();
                    clientPlayer.setBlockBreakingInfo(new ClientPlayerEntity.BlockBreakingInfo(position, direction));
                    // TODO: Handle instant breaking
                    // TODO: Handle creative mode mining
                    // TODO: Test breaking fire
                    // TODO: The java client keeps spamming swing packets while waiting for the block break cooldown. Those need to be cancelled

                    clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.StartDestroyBlock, position, direction.ordinal()));
                }
                case ABORT_DESTROY_BLOCK -> {
                    clientPlayer.setBlockBreakingInfo(null);
                    clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.AbortDestroyBlock, position, 0/*TODO: Figure this value out*/));
                }
                case STOP_DESTROY_BLOCK -> {
                    clientPlayer.cancelNextSwingPacket();
                    clientPlayer.setBlockBreakingInfo(null);

                    if (!gameSession.isBlockBreakingServerAuthoritative()) {
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.StopDestroyBlock));
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.CrackBlock, position, direction.ordinal()));
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.AbortDestroyBlock, position, 0));
                    } else {
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.ContinueDestroyBlock, position, direction.ordinal()));
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.PredictDestroyBlock, position, direction.ordinal()));
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.AbortDestroyBlock, position, 0));
                    }

                    chunkTracker.handleBlockChange(position, 0, chunkTracker.airId());
                    PacketFactory.sendJavaBlockUpdate(wrapper.user(), position, 0);
                }
                case DROP_ALL_ITEMS, DROP_ITEM -> {
                    // TODO: Implement DROP_ALL_ITEMS, DROP_ITEM
                    PacketFactory.sendJavaContainerSetContent(wrapper.user(), wrapper.user().get(InventoryTracker.class).getInventoryContainer());
                }
                case RELEASE_USE_ITEM -> {
                    final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();

                    wrapper.setCancelled(false);
                    wrapper.setPacketType(ServerboundBedrockPackets.INVENTORY_TRANSACTION);

                    wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
                    wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemReleaseTransaction.getValue()); // transaction type
                    wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // actions count
                    wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // action type
                    wrapper.write(BedrockTypes.VAR_INT, (int) inventoryContainer.getSelectedHotbarSlot()); // selected hotbar slot
                    wrapper.write(wrapper.user().get(ItemRewriter.class).itemType(), inventoryContainer.getSelectedHotbarItem()); // hand item
                    wrapper.write(BedrockTypes.POSITION_3F, wrapper.user().get(EntityTracker.class).getClientPlayer().position()); // head position, the same as player position.
                }
                case SWAP_ITEM_WITH_OFFHAND -> {
                }
                default -> throw new IllegalStateException("Unhandled PlayerActionAction: " + action);
            }

            if (sequence > 0) {
                PacketFactory.sendJavaBlockChangedAck(wrapper.user(), sequence);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.INTERACT, ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();
            final int entityId = wrapper.read(Types.VAR_INT); // entity id
            final InteractActionType action = InteractActionType.values()[wrapper.read(Types.VAR_INT)]; // action
            final Entity entity = entityTracker.getEntityByJid(entityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemUseOnEntityTransaction.getValue()); // transaction type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // actions count
            wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, entity.runtimeId()); // runtime entity id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, (switch (action) {
                case INTERACT, INTERACT_AT -> ItemUseOnActorInventoryTransaction_ActionType.Interact;
                case ATTACK -> ItemUseOnActorInventoryTransaction_ActionType.Attack;
                default -> throw new IllegalStateException("Unhandled InteractActionType: " + action);
            }).getValue()); // action type
            wrapper.write(BedrockTypes.VAR_INT, (int) inventoryContainer.getSelectedHotbarSlot()); // hotbar slot
            wrapper.write(wrapper.user().get(ItemRewriter.class).itemType(), inventoryContainer.getSelectedHotbarItem()); // held item
            wrapper.write(BedrockTypes.POSITION_3F, entityTracker.getClientPlayer().position()); // player position

            // TODO: Bedrock client sends INTERACT packet when hovered entity changes. Might be used by anticheats

            switch (action) {
                case INTERACT -> wrapper.cancel();
                case ATTACK -> {
                    wrapper.read(Types.BOOLEAN); // secondary action
                    wrapper.write(BedrockTypes.POSITION_3F, Position3f.ZERO); // click position

                    entityTracker.getClientPlayer().sendSwingPacketToServer();
                    entityTracker.getClientPlayer().cancelNextSwingPacket();
                }
                case INTERACT_AT -> {
                    final float x = wrapper.read(Types.FLOAT); // x
                    final float y = wrapper.read(Types.FLOAT); // y
                    final float z = wrapper.read(Types.FLOAT); // z
                    final InteractionHand hand = InteractionHand.values()[wrapper.read(Types.VAR_INT)]; // hand
                    if (hand != InteractionHand.MAIN_HAND) {
                        wrapper.cancel();
                        return;
                    }
                    wrapper.read(Types.BOOLEAN); // secondary action
                    wrapper.write(BedrockTypes.POSITION_3F, entity.position().add(x, y, z)); // click position
                }
                default -> throw new IllegalStateException("Unhandled InteractActionType: " + action);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.MOVE_PLAYER_STATUS_ONLY, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.updatePlayerPosition(wrapper.read(Types.UNSIGNED_BYTE));
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.MOVE_PLAYER_POS, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.updatePlayerPosition(wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), wrapper.read(Types.UNSIGNED_BYTE));
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.MOVE_PLAYER_POS_ROT, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.updatePlayerPosition(wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), MathUtil.wrapDegrees(wrapper.read(Types.FLOAT)), wrapper.read(Types.FLOAT), wrapper.read(Types.UNSIGNED_BYTE));
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.MOVE_PLAYER_ROT, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.updatePlayerPosition(MathUtil.wrapDegrees(wrapper.read(Types.FLOAT)), wrapper.read(Types.FLOAT), wrapper.read(Types.UNSIGNED_BYTE));
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.ACCEPT_TELEPORTATION, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.confirmTeleport(wrapper.read(Types.VAR_INT)); // teleport id
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.PLAYER_INPUT, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final Set<InputFlag> inputFlags = EnumUtil.getEnumSetFromBitmask(InputFlag.class, wrapper.read(Types.BYTE), InputFlag::ordinal); // input flags
            clientPlayer.setInputFlags(inputFlags);
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.CLIENT_TICK_END, ServerboundBedrockPackets.PLAYER_AUTH_INPUT, wrapper -> {
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final Position3f prevPosition = clientPlayer.prevPosition();
            final boolean prevOnGround = clientPlayer.prevOnGround();
            final Set<InputFlag> prevInputFlags = clientPlayer.prevInputFlags();
            clientPlayer.tick();

            if (prevOnGround && clientPlayer.inputFlags().contains(InputFlag.JUMP)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.StartJumping);
            }

            if (!clientPlayer.isInitiallySpawned() || clientPlayer.isDead()) {
                wrapper.cancel();
                return;
            }

            clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.BlockBreakingDelayEnabled);
            if (clientPlayer.isOnGround()) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.VerticalCollision);
            }
            if (clientPlayer.horizontalCollision()) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.HorizontalCollision);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.FORWARD)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.Up);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.BACKWARD)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.Down);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.LEFT)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.Left);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.RIGHT)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.Right);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.JUMP)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.JumpDown, PlayerAuthInputPacket_InputData.Jumping, PlayerAuthInputPacket_InputData.WantUp, PlayerAuthInputPacket_InputData.JumpCurrentRaw);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.SHIFT)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.SneakDown, PlayerAuthInputPacket_InputData.Sneaking, PlayerAuthInputPacket_InputData.WantDown, PlayerAuthInputPacket_InputData.SneakCurrentRaw);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.SPRINT)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.SprintDown, PlayerAuthInputPacket_InputData.Sprinting);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.JUMP) && !prevInputFlags.contains(InputFlag.JUMP)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.JumpPressedRaw);
            }
            if (prevInputFlags.contains(InputFlag.JUMP) && !clientPlayer.inputFlags().contains(InputFlag.JUMP)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.JumpReleasedRaw);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.SHIFT) && !prevInputFlags.contains(InputFlag.SHIFT)) {
                clientPlayer.setSneaking(true);
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.SneakPressedRaw, PlayerAuthInputPacket_InputData.StartSneaking);
            }
            if (prevInputFlags.contains(InputFlag.SHIFT) && !clientPlayer.inputFlags().contains(InputFlag.SHIFT)) {
                clientPlayer.setSneaking(false);
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.SneakReleasedRaw, PlayerAuthInputPacket_InputData.StopSneaking);
            }

            final Position3f positionDelta = clientPlayer.position().subtract(prevPosition);
            final Position3f velocity;
            if (!clientPlayer.isInitiallySpawned() || clientPlayer.dimensionChangeInfo() != null || clientPlayer.abilities().getBooleanValue(AbilitiesIndex.Flying)) {
                velocity = positionDelta;
            } else {
                float dx = positionDelta.x() * 0.98F;
                float dy = positionDelta.y();
                float dz = positionDelta.z() * 0.98F;
                final float friction = clientPlayer.isOnGround() ? ProtocolConstants.BLOCK_FRICTION : 1F;
                dx *= friction;
                dz *= friction;

                if (clientPlayer.effects().containsKey("minecraft:levitation")) {
                    dy += (0.05F * (clientPlayer.effects().get("minecraft:levitation").amplifier() + 1)) * 0.2F;
                } else {
                    dy -= ProtocolConstants.PLAYER_GRAVITY;
                }
                // Slow falling does not change the velocity when standing still

                velocity = new Position3f(dx * 0.91F, dy * 0.98F, dz * 0.91F);
            }

            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().x()); // pitch
            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().y()); // yaw
            wrapper.write(BedrockTypes.POSITION_3F, clientPlayer.position()); // position
            wrapper.write(BedrockTypes.POSITION_2F, MathUtil.calculateMovementDirections(clientPlayer.authInputData(), clientPlayer.isSneaking())); // move vector
            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().z()); // head yaw
            wrapper.write(BedrockTypes.UNSIGNED_VAR_BIG_INTEGER, EnumUtil.getBigBitmaskFromEnumSet(clientPlayer.authInputData(), PlayerAuthInputPacket_InputData::getValue)); // input flags
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, InputMode.Mouse.getValue()); // input mode
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ClientPlayMode.Screen.getValue()); // play mode
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, NewInteractionModel.Touch.getValue()); // interaction mode
            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().x()); // interact pitch
            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().y()); // interact yaw
            wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, (long) clientPlayer.age()); // tick
            wrapper.write(BedrockTypes.POSITION_3F, velocity); // delta
            if (clientPlayer.authInputData().contains(PlayerAuthInputPacket_InputData.PerformBlockActions)) {
                wrapper.write(BedrockTypes.VAR_INT, clientPlayer.authInputBlockActions().size()); // player block actions count
                for (ClientPlayerEntity.AuthInputBlockAction blockAction : clientPlayer.authInputBlockActions()) {
                    wrapper.write(BedrockTypes.VAR_INT, blockAction.action().getValue()); // action
                    switch (blockAction.action()) {
                        // StopDestroyBlock does not have additional data even tho bedrock protocol docs claim it does
                        case StartDestroyBlock, AbortDestroyBlock, CrackBlock, PredictDestroyBlock, ContinueDestroyBlock -> {
                            wrapper.write(BedrockTypes.POSITION_3I, blockAction.position()); // position
                            wrapper.write(BedrockTypes.VAR_INT, blockAction.direction()); // facing
                        }
                    }
                }
            }
            wrapper.write(BedrockTypes.POSITION_2F, new Position2f(0F, 0F)); // analog move vector
            wrapper.write(BedrockTypes.POSITION_3F, MathUtil.calculateCameraOrientation(clientPlayer.rotation().y(), clientPlayer.rotation().x())); // camera orientation
            wrapper.write(BedrockTypes.POSITION_2F, MathUtil.calculateMovementDirections(clientPlayer.authInputData(), false)); // raw move vector

            clientPlayer.authInputData().clear();
            clientPlayer.authInputBlockActions().clear();
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.PLAYER_ABILITIES, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final byte flags = wrapper.read(Types.BYTE); // flags
            final boolean flying = (flags & AbilitiesFlag.FLYING.getBit()) != 0;
            if (flying != clientPlayer.abilities().getBooleanValue(AbilitiesIndex.Flying)) {
                clientPlayer.abilities().getOrCreateCacheLayer().setAbility(AbilitiesIndex.Flying, flying);
                clientPlayer.addAuthInputData(flying ? PlayerAuthInputPacket_InputData.StartFlying : PlayerAuthInputPacket_InputData.StopFlying);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.SWING, ServerboundBedrockPackets.ANIMATE, wrapper -> {
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final InteractionHand hand = InteractionHand.values()[wrapper.read(Types.VAR_INT)]; // hand
            if (hand != InteractionHand.MAIN_HAND || clientPlayer.checkCancelSwingPacket()) {
                wrapper.cancel();
                return;
            }

            wrapper.write(BedrockTypes.VAR_INT, AnimatePacket_Action.Swing.getValue()); // action
            wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id

            if (clientPlayer.blockBreakingInfo() != null) {
                if (!gameSession.isBlockBreakingServerAuthoritative()) {
                    final ClientPlayerEntity.BlockBreakingInfo blockBreakingInfo = clientPlayer.blockBreakingInfo();
                    clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.CrackBlock, blockBreakingInfo.position(), blockBreakingInfo.direction().ordinal()));
                }
            } else {
                clientPlayer.addAuthInputData(PlayerAuthInputPacket_InputData.MissedSwing);
            }
        });
    }

}
