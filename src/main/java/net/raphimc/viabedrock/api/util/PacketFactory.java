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
package net.raphimc.viabedrock.api.util;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.gson.JsonNull;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import net.raphimc.viabedrock.api.inventory.InventoryOperation;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.ContainerSlot;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.BedrockMappingData;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ComplexInventoryTransaction_Type;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.InventorySourceType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.InventorySource_InventorySourceFlags;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerActionType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ServerboundLoadingScreenPacketType;
import net.raphimc.viabedrock.protocol.data.enums.java.EntityEvent;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEventType;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.CustomChatCompletionsAction;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.protocol.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.protocol.model.inventory.InventorySource;
import net.raphimc.viabedrock.protocol.model.inventory.InventoryTransactionData;
import net.raphimc.viabedrock.protocol.rewriter.InventoryTransactionRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.storage.ItemStackRequestTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketFactory {
    public static void sendJavaBlockDestroyProgress(final UserConnection user, final int id, final BlockPosition position, final int stage) {
        final PacketWrapper blockDestruction = PacketWrapper.create(ClientboundPackets26_1.BLOCK_DESTRUCTION, user);
        blockDestruction.write(Types.VAR_INT, id); // id
        blockDestruction.write(Types.BLOCK_POSITION1_14, position); // position
        blockDestruction.write(Types.BYTE, (byte) stage); // destroy stage
        blockDestruction.send(BedrockProtocol.class);
    }

    public static void sendJavaSystemChat(final UserConnection user, final Tag message) {
        final PacketWrapper systemChat = PacketWrapper.create(ClientboundPackets26_1.SYSTEM_CHAT, user);
        systemChat.write(Types.TAG, message); // message
        systemChat.write(Types.BOOLEAN, false); // overlay
        systemChat.send(BedrockProtocol.class);
    }

    public static void sendJavaBlockEntityData(final UserConnection user, final BlockPosition position, final BlockEntity blockEntity) {
        final PacketWrapper blockEntityData = PacketWrapper.create(ClientboundPackets26_1.BLOCK_ENTITY_DATA, user);
        blockEntityData.write(Types.BLOCK_POSITION1_14, position); // position
        blockEntityData.write(Types.VAR_INT, blockEntity.typeId()); // type
        blockEntityData.write(Types.COMPOUND_TAG, blockEntity.tag()); // block entity tag
        blockEntityData.send(BedrockProtocol.class);
    }

    public static void sendJavaCustomChatCompletions(final UserConnection user, final CustomChatCompletionsAction action, final String[] entries) {
        final PacketWrapper customChatCompletions = PacketWrapper.create(ClientboundPackets26_1.CUSTOM_CHAT_COMPLETIONS, user);
        customChatCompletions.write(Types.VAR_INT, action.ordinal()); // action
        customChatCompletions.write(Types.STRING_ARRAY, entries); // entries
        customChatCompletions.send(BedrockProtocol.class);
    }

    public static void sendJavaContainerSetContent(final UserConnection user, final Container container) {
        final PacketWrapper containerSetContent = PacketWrapper.create(ClientboundPackets26_1.CONTAINER_SET_CONTENT, user);
        writeJavaContainerSetContent(containerSetContent, container);
        containerSetContent.send(BedrockProtocol.class);
    }

    public static void sendJavaGameEvent(final UserConnection user, final GameEventType event, final float value) {
        final PacketWrapper gameEvent = PacketWrapper.create(ClientboundPackets26_1.GAME_EVENT, user);
        gameEvent.write(Types.UNSIGNED_BYTE, (short) event.ordinal()); // event id
        gameEvent.write(Types.FLOAT, value); // value
        gameEvent.send(BedrockProtocol.class);
    }

    public static void sendJavaEntityEvent(final UserConnection user, final Entity entity, final EntityEvent event) {
        final PacketWrapper entityEvent = PacketWrapper.create(ClientboundPackets26_1.ENTITY_EVENT, user);
        entityEvent.write(Types.INT, entity.javaId()); // entity id
        entityEvent.write(Types.BYTE, event.getValue()); // event
        entityEvent.send(BedrockProtocol.class);
    }

    public static void sendJavaContainerClose(final UserConnection user, final int containerId) {
        final PacketWrapper containerClose = PacketWrapper.create(ClientboundPackets26_1.CONTAINER_CLOSE, user);
        containerClose.write(Types.VAR_INT, containerId); // container id
        containerClose.send(BedrockProtocol.class);
    }

    public static void sendJavaRotateHead(final UserConnection user, final Entity entity) {
        final PacketWrapper rotateHead = PacketWrapper.create(ClientboundPackets26_1.ROTATE_HEAD, user);
        rotateHead.write(Types.VAR_INT, entity.javaId()); // entity id
        rotateHead.write(Types.BYTE, MathUtil.float2Byte(entity.rotation().z())); // head yaw
        rotateHead.send(BedrockProtocol.class);
    }

    public static void sendJavaBlockChangedAck(final UserConnection user, final int sequence) {
        final PacketWrapper blockChangedAck = PacketWrapper.create(ClientboundPackets26_1.BLOCK_CHANGED_ACK, user);
        blockChangedAck.write(Types.VAR_INT, sequence); // sequence number
        blockChangedAck.send(BedrockProtocol.class);
    }

    public static void sendJavaBlockUpdate(final UserConnection user, final BlockPosition position, final int blockState) {
        final PacketWrapper blockUpdate = PacketWrapper.create(ClientboundPackets26_1.BLOCK_UPDATE, user);
        blockUpdate.write(Types.BLOCK_POSITION1_14, position); // position
        blockUpdate.write(Types.VAR_INT, blockState); // block state
        blockUpdate.send(BedrockProtocol.class);
    }

    public static void sendJavaLevelParticles(final UserConnection user, final Position3f position, final BedrockMappingData.JavaParticle particle) {
        final PacketWrapper levelParticles = PacketWrapper.create(ClientboundPackets26_1.LEVEL_PARTICLES, user);
        writeJavaLevelParticles(levelParticles, position, particle);
        levelParticles.send(BedrockProtocol.class);
    }

    public static void sendBedrockContainerClose(final UserConnection user, final byte containerId, final ContainerType containerType) {
        final PacketWrapper containerClose = PacketWrapper.create(ServerboundBedrockPackets.CONTAINER_CLOSE, user);
        containerClose.write(Types.BYTE, containerId); // container id
        containerClose.write(Types.BYTE, (byte) containerType.getValue()); // type
        containerClose.write(Types.BOOLEAN, false); // server initiated
        containerClose.sendToServer(BedrockProtocol.class);
    }

    /**
     * Sends the given inventory change to the server using whichever inventory protocol it uses.
     *
     * <p>This has to be called before the change is applied to the containers, because both protocols describe the
     * change relative to the current state.</p>
     *
     * @param newItems   The new content of every slot which changed
     * @param operations The operations which describe the change
     */
    public static void sendBedrockInventoryChange(final UserConnection user, final Map<ContainerSlot, BedrockItem> newItems, final List<InventoryOperation> operations) {
        if (user.get(GameSessionStorage.class).isInventoryServerAuthoritative()) {
            user.get(ItemStackRequestTracker.class).sendRequest(operations);
        } else {
            sendBedrockInventoryTransaction(user, newItems, operations);
        }
    }

    public static void sendBedrockPlayerAction(final UserConnection user, final PlayerActionType actionType, final BlockPosition position, final BlockPosition resultPosition, final int face) {
        final PacketWrapper playerAction = PacketWrapper.create(ServerboundBedrockPackets.PLAYER_ACTION, user);
        playerAction.write(BedrockTypes.UNSIGNED_VAR_LONG, user.get(EntityTracker.class).getClientPlayer().runtimeId()); // entity runtime id
        playerAction.write(BedrockTypes.VAR_INT, actionType.getValue()); // action type
        playerAction.write(BedrockTypes.BLOCK_POSITION, position); // block position
        playerAction.write(BedrockTypes.BLOCK_POSITION, resultPosition); // result position
        playerAction.write(BedrockTypes.VAR_INT, face); // face
        playerAction.sendToServer(BedrockProtocol.class);
    }

    /**
     * Sends the given inventory change to a server which uses client authoritative inventories. Those servers expect
     * the client to tell them what the inventory looked like before and after the change.
     *
     * @param newItems   The new content of every slot which changed
     * @param operations The operations which describe the change
     */
    public static void sendBedrockInventoryTransaction(final UserConnection user, final Map<ContainerSlot, BedrockItem> newItems, final List<InventoryOperation> operations) {
        final List<InventoryActionData> actions = new ArrayList<>(newItems.size() + operations.size());
        for (Map.Entry<ContainerSlot, BedrockItem> entry : newItems.entrySet()) {
            final ContainerSlot containerSlot = entry.getKey();
            actions.add(new InventoryActionData(
                    new InventorySource(InventorySourceType.Container_Inventory, containerSlot.container().containerId(), InventorySource_InventorySourceFlags.No_Flag),
                    containerSlot.slot(),
                    containerSlot.getItem(),
                    entry.getValue()
            ));
        }
        for (InventoryOperation operation : operations) {
            if (!(operation instanceof InventoryOperation.Drop drop)) continue;
            final BedrockItem droppedItem = drop.source().getItem().copy();
            droppedItem.setAmount(drop.count());
            actions.add(new InventoryActionData(
                    new InventorySource(InventorySourceType.World_Interaction, ContainerID.CONTAINER_ID_NONE.getValue(), InventorySource_InventorySourceFlags.No_Flag),
                    0,
                    BedrockItem.empty(),
                    droppedItem
            ));
        }
        if (actions.isEmpty()) {
            return;
        }

        final PacketWrapper inventoryTransaction = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, user);
        inventoryTransaction.write(user.get(InventoryTransactionRewriter.class).getInventoryTransactionType(), new BedrockInventoryTransaction(
                0, // legacy request id
                null,
                actions,
                ComplexInventoryTransaction_Type.NormalTransaction,
                new InventoryTransactionData.NormalTransactionData()
        ));
        inventoryTransaction.sendToServer(BedrockProtocol.class);
    }

    public static void sendBedrockLoadingScreen(final UserConnection user, final ServerboundLoadingScreenPacketType type, final Long loadingScreenId) {
        final PacketWrapper loadingScreen = PacketWrapper.create(ServerboundBedrockPackets.LOADING_SCREEN, user);
        loadingScreen.write(BedrockTypes.VAR_INT, type.getValue()); // type
        loadingScreen.write(Types.BOOLEAN, loadingScreenId != null); // has loading screen id
        if (loadingScreenId != null) {
            loadingScreen.write(BedrockTypes.UNSIGNED_INT_LE, loadingScreenId); // loading screen id
        }
        loadingScreen.sendToServer(BedrockProtocol.class);
    }

    public static void writeJavaDisconnect(final PacketWrapper wrapper, final String reason) {
        switch (wrapper.getPacketType().state()) {
            case LOGIN -> wrapper.write(Types.COMPONENT, reason != null ? TextUtil.stringToGson(reason) : JsonNull.INSTANCE);
            case CONFIGURATION, PLAY -> wrapper.write(Types.TAG, reason != null ? TextUtil.stringToNbt(reason) : null);
            default -> throw new IllegalStateException("Unexpected state: " + wrapper.getPacketType().state());
        }
    }

    public static void writeJavaContainerSetContent(final PacketWrapper wrapper, final Container container) {
        wrapper.write(Types.VAR_INT, (int) container.javaContainerId()); // container id
        wrapper.write(Types.VAR_INT, 0); // revision
        wrapper.write(VersionedTypes.V26_2.itemArray, container.getJavaItems()); // items
        wrapper.write(VersionedTypes.V26_2.item, wrapper.user().get(InventoryTracker.class).getHudContainer().getJavaItem(0)); // cursor item
    }

    public static void writeJavaLevelParticles(final PacketWrapper wrapper, final Position3f position, final BedrockMappingData.JavaParticle particle) {
        wrapper.write(Types.BOOLEAN, false); // override limiter
        wrapper.write(Types.BOOLEAN, false); // always show
        wrapper.write(Types.DOUBLE, (double) position.x()); // x
        wrapper.write(Types.DOUBLE, (double) position.y()); // y
        wrapper.write(Types.DOUBLE, (double) position.z()); // z
        wrapper.write(Types.FLOAT, particle.offsetX()); // offset x
        wrapper.write(Types.FLOAT, particle.offsetY()); // offset y
        wrapper.write(Types.FLOAT, particle.offsetZ()); // offset z
        wrapper.write(Types.FLOAT, particle.speed()); // speed
        wrapper.write(Types.INT, particle.count()); // count
        wrapper.write(VersionedTypes.V26_2.particle, particle.particle().copy()); // particle data
    }

}
