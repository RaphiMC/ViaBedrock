/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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

import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_2;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
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
import net.raphimc.viabedrock.protocol.data.enums.bedrock.AbilitiesIndex;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ActorFlags;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ComplexInventoryTransaction_Type;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.data.enums.java.*;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.*;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.Position2f;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.protocol.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.protocol.model.inventory.InventorySource;
import net.raphimc.viabedrock.protocol.model.inventory.InventoryTransactionData;
import net.raphimc.viabedrock.protocol.rewriter.GameTypeRewriter;
import net.raphimc.viabedrock.protocol.rewriter.InventoryTransactionRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class ClientPlayerPackets {

    private static final PacketHandler CLIENT_PLAYER_GAME_MODE_INFO_UPDATE = wrapper -> {
        final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();

        final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets26_1.PLAYER_INFO_UPDATE, wrapper.user());
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
        protocol.registerClientbound(ClientboundBedrockPackets.RESPAWN, ClientboundPackets26_1.RESPAWN, wrapper -> {
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final byte rawState = wrapper.read(Types.BYTE); // state
            final PlayerRespawnState state = PlayerRespawnState.getByValue(rawState);
            if (state == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown PlayerRespawnState: " + rawState);
                wrapper.cancel();
                return;
            }
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // entity runtime id

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
                            // TODO: InventoryTransactionPacket(legacyRequestId=0, legacySlots=[], actions=[], transactionType=INVENTORY_MISMATCH, actionType=0, entityRuntimeId=0, blockPosition=null, blockFace=0, hotbarSlot=0, itemInHand=null, playerPosition=null, clickPosition=null, headPosition=null, usingNetIds=false, blockDefinition=null)
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
                        wrapper.write(Types.BYTE, (byte) (RespawnKeepFlag.ATTRIBUTE_MODIFIERS.getBit() | RespawnKeepFlag.ENTITY_DATA.getBit())); // keep data mask
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
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // entity runtime id
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
        protocol.registerClientbound(ClientboundBedrockPackets.CORRECT_PLAYER_MOVE_PREDICTION, ClientboundPackets26_1.PLAYER_POSITION, wrapper -> {
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

            final byte rawRewindType = wrapper.read(Types.BYTE); // rewind type
            final RewindType rewindType = RewindType.getByValue(rawRewindType);
            if (rewindType == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown RewindType: " + rawRewindType);
                return;
            }
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            wrapper.read(BedrockTypes.POSITION_3F); // position delta
            wrapper.read(BedrockTypes.POSITION_2F); // vehicle rotation
            if (wrapper.read(Types.BOOLEAN)) {
                wrapper.read(BedrockTypes.FLOAT_LE); // vehicle angular velocity
            }
            switch (rewindType) {
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
                default -> throw new IllegalStateException("Unhandled RewindType: " + rewindType);
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
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_PLAYER_GAME_TYPE, ClientboundPackets26_1.PLAYER_INFO_UPDATE, wrapper -> {
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final PlayerListStorage playerList = wrapper.user().get(PlayerListStorage.class);

            final GameType gameType = GameType.getByValue(wrapper.read(BedrockTypes.VAR_INT), GameType.Undefined); // game type
            final long entityUniqueId = wrapper.read(BedrockTypes.VAR_LONG); // entity unique id
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // tick

            final Pair<UUID, String> playerListEntry = playerList.getPlayer(entityUniqueId);
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
        protocol.registerClientbound(ClientboundBedrockPackets.OPEN_SIGN, ClientboundPackets26_1.OPEN_SIGN_EDITOR, new PacketHandlers() {
            @Override
            protected void register() {
                map(BedrockTypes.BLOCK_POSITION, Types.BLOCK_POSITION1_14); // position
                map(Types.BOOLEAN); // front
            }
        });

        protocol.registerServerbound(ServerboundPackets26_1.CLIENT_COMMAND, ServerboundBedrockPackets.RESPAWN, wrapper -> {
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final ClientCommandAction action = ClientCommandAction.values()[wrapper.read(Types.VAR_INT)]; // action

            switch (action) {
                case PERFORM_RESPAWN -> {
                    wrapper.write(BedrockTypes.POSITION_3F, Position3f.ZERO); // position
                    wrapper.write(Types.BYTE, (byte) PlayerRespawnState.ClientReadyToSpawn.getValue()); // state
                    wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // entity runtime id
                }
                case REQUEST_STATS, REQUEST_GAMERULE_VALUES -> wrapper.cancel();
                default -> throw new IllegalStateException("Unhandled ClientCommandAction: " + action);
            }
        });
        protocol.registerServerbound(ServerboundPackets26_1.PLAYER_COMMAND, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            wrapper.read(Types.VAR_INT); // entity id
            final PlayerCommandAction action = PlayerCommandAction.values()[wrapper.read(Types.VAR_INT)]; // action
            final int data = wrapper.read(Types.VAR_INT); // data

            switch (action) {
                case START_SPRINTING -> {
                    clientPlayer.setSprinting(true);
                    clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.StartSprinting);
                }
                case STOP_SPRINTING -> {
                    clientPlayer.setSprinting(false);
                    clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.StopSprinting);
                }
                case START_FALL_FLYING -> {
                    clientPlayer.setGliding(true);
                    clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.StartGliding);
                }
                // Riding jumps are driven by the Jumping input flag in PlayerAuthInput;
                // the Bedrock server computes the jump scale from the held ticks itself
                case START_RIDING_JUMP, STOP_RIDING_JUMP -> {
                }
                default -> ViaBedrock.getPlatform().getLogger().log(Level.FINE, "Unhandled PlayerCommandAction: " + action);
            }
        });
        protocol.registerServerbound(ServerboundPackets26_1.PLAYER_ACTION, null, wrapper -> {
            wrapper.cancel();
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();
            final PlayerActionAction action = PlayerActionAction.values()[wrapper.read(Types.VAR_INT)]; // action
            final BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_14); // block position
            final Direction direction = Direction.values()[wrapper.read(Types.UNSIGNED_BYTE)]; // face
            final int sequence = wrapper.read(Types.VAR_INT); // sequence number

            final boolean isMining = action == PlayerActionAction.START_DESTROY_BLOCK || action == PlayerActionAction.ABORT_DESTROY_BLOCK || action == PlayerActionAction.STOP_DESTROY_BLOCK;
            if (isMining && (gameSession.isImmutableWorld() || !clientPlayer.abilities().getBooleanValue(AbilitiesIndex.Mine))) {
                // TODO: Prevent breaking and cancel any packets that would be sent (swing, player action)
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
                    clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.AbortDestroyBlock, position, direction.ordinal()));
                }
                case STOP_DESTROY_BLOCK -> {
                    clientPlayer.cancelNextSwingPacket();
                    clientPlayer.setBlockBreakingInfo(null);

                    if (!gameSession.isBlockBreakingServerAuthoritative()) {
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.StopDestroyBlock));
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.CrackBlock, position, direction.ordinal()));
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.AbortDestroyBlock, position, direction.ordinal()));
                    } else {
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.ContinueDestroyBlock, position, direction.ordinal()));
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.PredictDestroyBlock, position, direction.ordinal()));
                        clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.AbortDestroyBlock, position, direction.ordinal()));
                    }

                    chunkTracker.handleBlockChange(position, 0, chunkTracker.bedrockAirId());
                    PacketFactory.sendJavaBlockUpdate(wrapper.user(), position, ProtocolConstants.JAVA_AIR_ID);
                }
                case DROP_ITEM, DROP_ALL_ITEMS -> {
                    // Dropping items is done with a synthetic inventory transaction: a world interaction
                    // creating the dropped item and an inventory action predicting the hotbar change
                    final BedrockItem currentItem = inventoryContainer.getSelectedHotbarItem();
                    if (currentItem.isEmpty()) {
                        break;
                    }

                    final BedrockItem predictedAmount = currentItem.copy();
                    if (action == PlayerActionAction.DROP_ITEM) {
                        predictedAmount.setAmount(1); // Drop a single item
                    } else {
                        // DROP_ALL_ITEMS drops the whole stack
                        predictedAmount.setAmount(currentItem.amount());
                    }

                    BedrockItem predictedToItem;
                    if (action == PlayerActionAction.DROP_ITEM && currentItem.amount() > 1) {
                        predictedToItem = currentItem.copy();
                        predictedToItem.setAmount(currentItem.amount() - 1);
                    } else {
                        predictedToItem = BedrockItem.empty();
                    }

                    final BedrockInventoryTransaction inventoryTransaction = new BedrockInventoryTransaction(
                            0, // legacy request id
                            null,
                            List.of(
                                    new InventoryActionData(
                                            new InventorySource(InventorySourceType.World_Interaction, ContainerID.CONTAINER_ID_NONE.getValue(), InventorySource_InventorySourceFlags.No_Flag),
                                            0,
                                            BedrockItem.empty(),
                                            predictedAmount
                                    ),
                                    new InventoryActionData(
                                            new InventorySource(InventorySourceType.Container_Inventory, ContainerID.CONTAINER_ID_INVENTORY.getValue(), InventorySource_InventorySourceFlags.No_Flag),
                                            inventoryContainer.getSelectedHotbarSlot(),
                                            currentItem,
                                            predictedToItem
                                    )
                            ),
                            ComplexInventoryTransaction_Type.NormalTransaction,
                            new InventoryTransactionData.NormalTransactionData()
                    );

                    final PacketWrapper transactionPacket = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper.user());
                    transactionPacket.write(wrapper.user().get(InventoryTransactionRewriter.class).getInventoryTransactionType(), inventoryTransaction);
                    transactionPacket.sendToServer(BedrockProtocol.class);
                }
                case RELEASE_USE_ITEM -> {
                    clientPlayer.setBlockBreakingInfo(null);

                    final BedrockInventoryTransaction inventoryTransaction = new BedrockInventoryTransaction(
                            0, // legacy request id
                            null,
                            null,
                            ComplexInventoryTransaction_Type.ItemReleaseTransaction,
                            new InventoryTransactionData.ReleaseItemTransactionData(
                                    ItemReleaseInventoryTransaction_ActionType.Release,
                                    inventoryContainer.getSelectedHotbarSlot(),
                                    inventoryContainer.getSelectedHotbarItem(),
                                    clientPlayer.position()
                            )
                    );

                    final PacketWrapper transactionPacket = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper.user());
                    transactionPacket.write(wrapper.user().get(InventoryTransactionRewriter.class).getInventoryTransactionType(), inventoryTransaction);
                    transactionPacket.sendToServer(BedrockProtocol.class);
                }
                case SWAP_ITEM_WITH_OFFHAND, STAB -> {
                }
                default -> throw new IllegalStateException("Unhandled PlayerActionAction: " + action);
            }

            if (sequence > 0) {
                PacketFactory.sendJavaBlockChangedAck(wrapper.user(), sequence);
            }
        });
        protocol.registerServerbound(ServerboundPackets26_1.ATTACK, ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();
            final int entityId = wrapper.read(Types.VAR_INT); // entity id
            final Entity entity = entityTracker.getEntityByJid(entityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
            wrapper.write(Types.BOOLEAN, false); // has legacy data
            wrapper.write(Types.BOOLEAN, true); // has transaction type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemUseOnEntityTransaction.getValue()); // transaction type
            wrapper.write(Types.BOOLEAN, true); // has transaction data
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // actions count
            wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, entity.runtimeId()); // entity runtime id
            wrapper.write(BedrockTypes.VAR_INT, ItemUseOnActorInventoryTransaction_ActionType.Attack.getValue()); // action type
            wrapper.write(BedrockTypes.VAR_INT, (int) inventoryContainer.getSelectedHotbarSlot()); // hotbar slot
            wrapper.write(wrapper.user().get(ItemRewriter.class).newItemType(), inventoryContainer.getSelectedHotbarItem()); // held item
            wrapper.write(BedrockTypes.POSITION_3F, entityTracker.getClientPlayer().position()); // player position
            wrapper.write(BedrockTypes.POSITION_3F, Position3f.ZERO); // click position

            entityTracker.getClientPlayer().sendSwingPacketToServer();
            entityTracker.getClientPlayer().cancelNextSwingPacket();
        });
        protocol.registerServerbound(ServerboundPackets26_1.INTERACT, ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();
            final int entityId = wrapper.read(Types.VAR_INT); // entity id
            final Entity entity = entityTracker.getEntityByJid(entityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }
            final InteractionHand hand = InteractionHand.values()[wrapper.read(Types.VAR_INT)]; // hand
            if (hand != InteractionHand.MAIN_HAND) {
                wrapper.cancel();
                return;
            }

            // Right-clicking a rideable entity with the main hand mounts it in Java Edition.
            // Bedrock clients mount by sending a passenger-initiated SetActorLink packet.
            if (EntityPackets.isRideable(entity)) {
                wrapper.cancel();
                PacketFactory.sendBedrockMount(wrapper.user(), entity, entityTracker.getClientPlayer());
                return;
            }

            // TODO: Bedrock client sends INTERACT packet when hovered entity changes. Might be used by anticheats

            wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
            wrapper.write(Types.BOOLEAN, false); // has legacy data
            wrapper.write(Types.BOOLEAN, true); // has transaction type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemUseOnEntityTransaction.getValue()); // transaction type
            wrapper.write(Types.BOOLEAN, true); // has transaction data
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // actions count
            wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, entity.runtimeId()); // entity runtime id
            wrapper.write(BedrockTypes.VAR_INT, ItemUseOnActorInventoryTransaction_ActionType.Interact.getValue()); // action type
            wrapper.write(BedrockTypes.VAR_INT, (int) inventoryContainer.getSelectedHotbarSlot()); // hotbar slot
            wrapper.write(wrapper.user().get(ItemRewriter.class).newItemType(), inventoryContainer.getSelectedHotbarItem()); // held item
            wrapper.write(BedrockTypes.POSITION_3F, entityTracker.getClientPlayer().position()); // player position
            final Vector3d location = wrapper.read(Types.LOW_PRECISION_VECTOR); // location
            wrapper.write(BedrockTypes.POSITION_3F, entity.position().add((float) location.x(), (float) location.y(), (float) location.z())); // click position
            wrapper.read(Types.BOOLEAN); // using secondary action
        });
        protocol.registerServerbound(ServerboundPackets26_1.USE_ITEM, ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper -> {
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();
            final InventoryTransactionRewriter inventoryTransactionRewriter = wrapper.user().get(InventoryTransactionRewriter.class);

            final int hand = wrapper.read(Types.VAR_INT); // hand
            wrapper.read(Types.VAR_INT); // sequence
            wrapper.read(Types.FLOAT); // yaw
            wrapper.read(Types.FLOAT); // pitch

            // Bedrock can't hold the majority of items in offhand and can't use any either.
            // TODO: We need to handle cases where the item changes, or it affects player movement (eg: eating/blocking/etc)
            if (hand != InteractionHand.MAIN_HAND.ordinal()) {
                wrapper.cancel();
                return;
            }

            // Tell the server that the player started using an item (e.g. for eating/blocking state)
            clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.StartUsingItem);

            final BedrockInventoryTransaction inventoryTransaction = new BedrockInventoryTransaction(
                    0, // legacy request id
                    null,
                    null,
                    ComplexInventoryTransaction_Type.ItemUseTransaction,
                    new InventoryTransactionData.UseItemTransactionData(
                            ItemUseInventoryTransaction_ActionType.Use,
                            ItemUseInventoryTransaction_TriggerType.PlayerInput,
                            new BlockPosition(0, 0, 0), // block position
                            255, // block face
                            inventoryContainer.getSelectedHotbarSlot(),
                            inventoryContainer.getSelectedHotbarItem(),
                            clientPlayer.position(),
                            Position3f.ZERO, // click position
                            0, // block runtime id
                            ItemUseInventoryTransaction_PredictedResult.Failure,
                            ItemUseInventoryTransaction_ClientCooldownState.Off
                    )
            );
            wrapper.write(inventoryTransactionRewriter.getInventoryTransactionType(), inventoryTransaction);
        });

        protocol.registerServerbound(ServerboundPackets26_1.USE_ITEM_ON, null, wrapper -> {
            wrapper.cancel();

            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            final InventoryTransactionRewriter inventoryTransactionRewriter = wrapper.user().get(InventoryTransactionRewriter.class);

            final InteractionHand hand = InteractionHand.values()[wrapper.read(Types.VAR_INT)]; // hand

            BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_14); // block position
            int faceInt = wrapper.read(Types.UNSIGNED_BYTE); // face
            Direction direction = Direction.getFromVerticalId(faceInt);
            if (direction == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown block face id: " + faceInt);
                return;
            }
            BlockFace face = direction.blockFace();
            Position3f clickPosition = new Position3f(
                    wrapper.read(Types.FLOAT), // x
                    wrapper.read(Types.FLOAT), // y
                    wrapper.read(Types.FLOAT)  // z
            );
            boolean insideBlock = wrapper.read(Types.BOOLEAN); // inside block
            wrapper.read(Types.BOOLEAN); // world border, this doesn't exist on Bedrock.

            // Send back block changed ack with the sequence, this will help with ghost blocks.
            PacketFactory.sendJavaBlockChangedAck(wrapper.user(), wrapper.read(Types.VAR_INT));

            // The player can only interact using the main hand on Bedrock!
            if (hand != InteractionHand.MAIN_HAND) {
                return;
            }

            // The bedrock client will send a start item use on action to the server first.
            PacketFactory.sendBedrockPlayerAction(
                    wrapper.user(),
                    clientPlayer.runtimeId(),
                    PlayerActionType.StartItemUseOn,
                    position,
                    insideBlock ? position : position.getRelative(face),
                    faceInt
            );

            // This is the main packet that the bedrock client uses to interact with blocks
            final PacketWrapper transactionPacket = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper.user());

            BedrockItem predictedToItem = inventoryTracker.getInventoryContainer().getSelectedHotbarItem().copy();
            // This is not entirely correct, but at least it's more accurate than not sending actions or sending the original item data.
            if (predictedToItem.blockRuntimeId() != 0 && clientPlayer.javaGameMode() != GameMode.CREATIVE) {
                predictedToItem.setAmount(predictedToItem.amount() - 1);
            }
            if (predictedToItem.amount() <= 0) {
                predictedToItem = BedrockItem.empty();
            }

            final BedrockInventoryTransaction inventoryTransaction = new BedrockInventoryTransaction(
                    0, // legacy request id
                    null,
                    List.of(
                            new InventoryActionData(
                                    new InventorySource(InventorySourceType.Container_Inventory, ContainerID.CONTAINER_ID_INVENTORY.getValue(), InventorySource_InventorySourceFlags.No_Flag),
                                    inventoryTracker.getInventoryContainer().getSelectedHotbarSlot(),
                                    inventoryTracker.getInventoryContainer().getSelectedHotbarItem(),
                                    predictedToItem
                            )
                    ),
                    ComplexInventoryTransaction_Type.ItemUseTransaction,
                    new InventoryTransactionData.UseItemTransactionData(
                            ItemUseInventoryTransaction_ActionType.Place,
                            ItemUseInventoryTransaction_TriggerType.PlayerInput,
                            position,
                            faceInt,
                            inventoryTracker.getInventoryContainer().getSelectedHotbarSlot(),
                            inventoryTracker.getInventoryContainer().getSelectedHotbarItem(),
                            clientPlayer.position(),
                            clickPosition,
                            chunkTracker.getBlockState(position),
                            ItemUseInventoryTransaction_PredictedResult.Success,
                            ItemUseInventoryTransaction_ClientCooldownState.Off
                    )
            );
            transactionPacket.write(inventoryTransactionRewriter.getInventoryTransactionType(), inventoryTransaction);

            transactionPacket.sendToServer(BedrockProtocol.class);

            // Bedrock sends a stop item use on after the transaction packet
            PacketFactory.sendBedrockPlayerAction(
                    wrapper.user(),
                    clientPlayer.runtimeId(),
                    PlayerActionType.StopItemUseOn,
                    position,
                    new BlockPosition(0, 0, 0),
                    0
            );
        });
        protocol.registerServerbound(ServerboundPackets26_1.MOVE_PLAYER_STATUS_ONLY, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.updatePlayerPosition(wrapper.read(Types.UNSIGNED_BYTE));
        });
        protocol.registerServerbound(ServerboundPackets26_1.MOVE_PLAYER_POS, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.updatePlayerPosition(wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), wrapper.read(Types.UNSIGNED_BYTE));
        });
        protocol.registerServerbound(ServerboundPackets26_1.MOVE_PLAYER_POS_ROT, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.updatePlayerPosition(wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), MathUtil.wrapDegrees(wrapper.read(Types.FLOAT)), wrapper.read(Types.FLOAT), wrapper.read(Types.UNSIGNED_BYTE));
        });
        protocol.registerServerbound(ServerboundPackets26_1.MOVE_PLAYER_ROT, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.updatePlayerPosition(MathUtil.wrapDegrees(wrapper.read(Types.FLOAT)), wrapper.read(Types.FLOAT), wrapper.read(Types.UNSIGNED_BYTE));
        });
        protocol.registerServerbound(ServerboundPackets26_1.ACCEPT_TELEPORTATION, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.confirmTeleport(wrapper.read(Types.VAR_INT)); // teleport id
        });
        protocol.registerServerbound(ServerboundPackets26_1.PLAYER_INPUT, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final Set<InputFlag> inputFlags = EnumUtil.getEnumSetFromBitmask(InputFlag.class, wrapper.read(Types.BYTE), InputFlag::ordinal); // input flags
            clientPlayer.setInputFlags(inputFlags);
        });
        protocol.registerServerbound(ServerboundPackets26_1.CLIENT_TICK_END, ServerboundBedrockPackets.PLAYER_AUTH_INPUT, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final ClientPlayerEntity clientPlayer = entityTracker.getClientPlayer();
            final Position3f prevPosition = clientPlayer.prevPosition();
            final boolean prevOnGround = clientPlayer.prevOnGround();
            final Set<InputFlag> prevInputFlags = clientPlayer.prevInputFlags();
            clientPlayer.tick();

            if (prevOnGround && clientPlayer.inputFlags().contains(InputFlag.JUMP)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.StartJumping);
            }

            if (clientPlayer.isGliding() && (
                    clientPlayer.isOnGround() ||
                    clientPlayer.effects().containsKey("minecraft:levitation") ||
                    clientPlayer.entityFlags().contains(ActorFlags.WALLCLIMBING) ||
                    clientPlayer.entityFlags().contains(ActorFlags.IN_ASCENDABLE_BLOCK) ||
                    clientPlayer.entityFlags().contains(ActorFlags.IN_SCAFFOLDING)
            )) {
                clientPlayer.setGliding(false);
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.StopGliding);
            }

            if (!clientPlayer.isInitiallySpawned() || clientPlayer.isDead()) {
                wrapper.cancel();
                return;
            }

            clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.BlockBreakingDelayEnabled);
            if (clientPlayer.isOnGround()) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.VerticalCollision);
            }
            if (clientPlayer.horizontalCollision()) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.HorizontalCollision);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.FORWARD)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.Up);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.BACKWARD)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.Down);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.LEFT)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.Left);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.RIGHT)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.Right);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.JUMP)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.JumpDown, PlayerAuthInputPacketPayload_InputData.Jumping, PlayerAuthInputPacketPayload_InputData.WantUp, PlayerAuthInputPacketPayload_InputData.JumpCurrentRaw);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.SHIFT)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.SneakDown, PlayerAuthInputPacketPayload_InputData.Sneaking, PlayerAuthInputPacketPayload_InputData.WantDown, PlayerAuthInputPacketPayload_InputData.SneakCurrentRaw);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.SPRINT)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.SprintDown, PlayerAuthInputPacketPayload_InputData.Sprinting);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.JUMP) && !prevInputFlags.contains(InputFlag.JUMP)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.JumpPressedRaw);
            }
            if (prevInputFlags.contains(InputFlag.JUMP) && !clientPlayer.inputFlags().contains(InputFlag.JUMP)) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.JumpReleasedRaw);
            }
            if (clientPlayer.inputFlags().contains(InputFlag.SHIFT) && !prevInputFlags.contains(InputFlag.SHIFT)) {
                clientPlayer.setSneaking(true);
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.SneakPressedRaw, PlayerAuthInputPacketPayload_InputData.StartSneaking);
            }
            if (prevInputFlags.contains(InputFlag.SHIFT) && !clientPlayer.inputFlags().contains(InputFlag.SHIFT)) {
                clientPlayer.setSneaking(false);
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.SneakReleasedRaw, PlayerAuthInputPacketPayload_InputData.StopSneaking);
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

            // Client predicted vehicle: while riding, position/velocity are interpreted as the vehicle's.
            // The input flags must be added before they get serialized below
            final Entity vehicle = clientPlayer.mountEntityRId() != -1 ? entityTracker.getEntityByRid(clientPlayer.mountEntityRId()) : null;
            if (vehicle != null) {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.IsInClientPredictedVehicle);

                // Paddle force mode: the oar states are implied from the movement input
                if (vehicle.javaType().isOrHasParent(EntityTypes26_2.ABSTRACT_BOAT)) {
                    if (clientPlayer.inputFlags().contains(InputFlag.JUMP) || clientPlayer.inputFlags().contains(InputFlag.LEFT) || clientPlayer.inputFlags().contains(InputFlag.FORWARD)) {
                        clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.PaddlingLeft);
                    }
                    if (clientPlayer.inputFlags().contains(InputFlag.JUMP) || clientPlayer.inputFlags().contains(InputFlag.RIGHT) || clientPlayer.inputFlags().contains(InputFlag.FORWARD)) {
                        clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.PaddlingRight);
                    }
                }
            }

            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().x()); // pitch
            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().y()); // yaw
            wrapper.write(BedrockTypes.POSITION_3F, clientPlayer.position()); // position
            wrapper.write(BedrockTypes.POSITION_2F, MathUtil.calculateMovementDirections(clientPlayer.authInputData(), clientPlayer.isSneaking())); // move vector
            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().z()); // head yaw
            wrapper.write(Types.BOOLEAN, true); // input flags present
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, clientPlayer.authInputData().size()); // input flags count
            for (PlayerAuthInputPacketPayload_InputData inputData : PlayerAuthInputPacketPayload_InputData.values()) {
                if (clientPlayer.authInputData().contains(inputData)) {
                    wrapper.write(BedrockTypes.VAR_INT, inputData.getValue()); // input flag
                }
            }
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, InputMode.Mouse.getValue()); // input mode
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ClientPlayMode.Screen.getValue()); // play mode
            wrapper.write(BedrockTypes.VAR_INT, NewInteractionModel.Touch.getValue()); // interaction mode
            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().x()); // interact pitch
            wrapper.write(BedrockTypes.FLOAT_LE, clientPlayer.rotation().y()); // interact yaw
            wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, (long) clientPlayer.age()); // tick
            wrapper.write(BedrockTypes.POSITION_3F, velocity); // delta
            wrapper.write(Types.BOOLEAN, true); // item interaction optional reflected
            wrapper.write(Types.BOOLEAN, false); // no item interaction
            wrapper.write(Types.BOOLEAN, true); // item stack request optional reflected
            wrapper.write(Types.BOOLEAN, false); // no item stack request
            wrapper.write(Types.BOOLEAN, true); // block actions optional reflected
            final boolean hasBlockActions = clientPlayer.authInputData().contains(PlayerAuthInputPacketPayload_InputData.PerformBlockActions);
            wrapper.write(Types.BOOLEAN, hasBlockActions);
            if (hasBlockActions) {
                wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, clientPlayer.authInputBlockActions().size()); // player block actions count
                for (ClientPlayerEntity.AuthInputBlockAction blockAction : clientPlayer.authInputBlockActions()) {
                    wrapper.write(BedrockTypes.VAR_INT, blockAction.action().getValue()); // action
                    wrapper.write(BedrockTypes.BLOCK_POSITION, blockAction.position() != null ? blockAction.position() : new BlockPosition(0, 0, 0)); // position
                    wrapper.write(BedrockTypes.VAR_INT, blockAction.direction()); // facing
                }
            }
            // Client predicted vehicle: while riding, position/velocity are interpreted as the vehicle's
            wrapper.write(Types.BOOLEAN, true); // vehicle rotation optional reflected
            if (vehicle != null) {
                wrapper.write(Types.BOOLEAN, true); // vehicle rotation present
                wrapper.write(BedrockTypes.POSITION_2F, new Position2f(0F, clientPlayer.rotation().y())); // vehicle rotation (pitch, yaw)
            } else {
                wrapper.write(Types.BOOLEAN, false); // no vehicle rotation
            }
            wrapper.write(Types.BOOLEAN, true); // predicted vehicle id optional reflected
            if (vehicle != null) {
                wrapper.write(Types.BOOLEAN, true); // predicted vehicle id present
                wrapper.write(BedrockTypes.VAR_LONG, vehicle.uniqueId()); // client predicted vehicle
            } else {
                wrapper.write(Types.BOOLEAN, false); // not in predicted vehicle
            }
            wrapper.write(BedrockTypes.POSITION_2F, new Position2f(0F, 0F)); // analog move vector
            wrapper.write(BedrockTypes.POSITION_3F, MathUtil.calculateCameraOrientation(clientPlayer.rotation().y(), clientPlayer.rotation().x())); // camera orientation
            wrapper.write(BedrockTypes.POSITION_2F, MathUtil.calculateMovementDirections(clientPlayer.authInputData(), false)); // raw move vector

            clientPlayer.authInputData().clear();
            clientPlayer.authInputBlockActions().clear();
        });
        protocol.registerServerbound(ServerboundPackets26_1.PLAYER_ABILITIES, null, wrapper -> {
            wrapper.cancel();
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final byte flags = wrapper.read(Types.BYTE); // flags
            final boolean flying = (flags & AbilitiesFlag.FLYING.getBit()) != 0;
            if (flying != clientPlayer.abilities().getBooleanValue(AbilitiesIndex.Flying)) {
                clientPlayer.abilities().getOrCreateCacheLayer().setAbility(AbilitiesIndex.Flying, flying);
                clientPlayer.addAuthInputData(flying ? PlayerAuthInputPacketPayload_InputData.StartFlying : PlayerAuthInputPacketPayload_InputData.StopFlying);
            }
        });
        protocol.registerServerbound(ServerboundPackets26_1.CHANGE_GAME_MODE, ServerboundBedrockPackets.SET_PLAYER_GAME_TYPE, new PacketHandlers() {
            @Override
            protected void register() {
                handler(wrapper -> {
                    final GameMode gameMode = GameMode.values()[wrapper.read(Types.VAR_INT)]; // game mode
                    final GameType gameType = switch (gameMode) {
                        case SURVIVAL -> GameType.Survival;
                        case CREATIVE -> GameType.Creative;
                        case ADVENTURE -> GameType.Adventure;
                        case SPECTATOR -> GameType.Spectator;
                        default -> throw new IllegalStateException("Unhandled GameMode: " + gameMode);
                    };
                    wrapper.write(BedrockTypes.VAR_INT, gameType.getValue()); // game type
                    wrapper.user().get(EntityTracker.class).getClientPlayer().setGameType(gameType);
                });
                handler(CLIENT_PLAYER_GAME_MODE_INFO_UPDATE);
                handler(CLIENT_PLAYER_GAME_MODE_UPDATE);
            }
        });
        protocol.registerServerbound(ServerboundPackets26_1.SWING, ServerboundBedrockPackets.ANIMATE, wrapper -> {
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            final InteractionHand hand = InteractionHand.values()[wrapper.read(Types.VAR_INT)]; // hand
            if (hand != InteractionHand.MAIN_HAND || clientPlayer.checkCancelSwingPacket()) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.UNSIGNED_BYTE, (short) AnimatePacketPayload_Action.Swing.getValue()); // action
            wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // entity runtime id
            wrapper.write(BedrockTypes.FLOAT_LE, 0F); // data
            wrapper.write(BedrockTypes.OPTIONAL_STRING, ActorSwingSource.Attack.name().toLowerCase(Locale.ROOT)); // swing source // TODO: 1.21.130

            if (clientPlayer.blockBreakingInfo() != null) {
                if (!gameSession.isBlockBreakingServerAuthoritative()) {
                    final ClientPlayerEntity.BlockBreakingInfo blockBreakingInfo = clientPlayer.blockBreakingInfo();
                    clientPlayer.addAuthInputBlockAction(new ClientPlayerEntity.AuthInputBlockAction(PlayerActionType.CrackBlock, blockBreakingInfo.position(), blockBreakingInfo.direction().ordinal()));
                }
            } else {
                clientPlayer.addAuthInputData(PlayerAuthInputPacketPayload_InputData.MissedSwing);
            }
        });
    }

}
