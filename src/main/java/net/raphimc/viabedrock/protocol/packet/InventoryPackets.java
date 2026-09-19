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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.fastutil.ints.IntObjectPair;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_21_5.NbtConverter_v1_21_5;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.dialog.ActionButton;
import com.viaversion.viaversion.libs.mcstructs.dialog.AfterAction;
import com.viaversion.viaversion.libs.mcstructs.dialog.Dialog;
import com.viaversion.viaversion.libs.mcstructs.dialog.Input;
import com.viaversion.viaversion.libs.mcstructs.dialog.action.CustomAllAction;
import com.viaversion.viaversion.libs.mcstructs.dialog.body.PlainMessageBody;
import com.viaversion.viaversion.libs.mcstructs.dialog.impl.MultiActionDialog;
import com.viaversion.viaversion.libs.mcstructs.dialog.impl.NoticeDialog;
import com.viaversion.viaversion.libs.mcstructs.dialog.input.BooleanInput;
import com.viaversion.viaversion.libs.mcstructs.dialog.input.NumberRangeInput;
import com.viaversion.viaversion.libs.mcstructs.dialog.input.SingleOptionInput;
import com.viaversion.viaversion.libs.mcstructs.dialog.input.TextInput;
import com.viaversion.viaversion.libs.mcstructs.dialog.serializer.DialogSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import net.lenni0451.mcstructs_bedrock.forms.Form;
import net.lenni0451.mcstructs_bedrock.forms.elements.*;
import net.lenni0451.mcstructs_bedrock.forms.serializer.FormSerializer;
import net.lenni0451.mcstructs_bedrock.forms.types.ActionForm;
import net.lenni0451.mcstructs_bedrock.forms.types.CustomForm;
import net.lenni0451.mcstructs_bedrock.forms.types.ModalForm;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTextUtils;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.model.container.ChestContainer;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.SimpleContainer;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ComplexInventoryTransaction_Type;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.ContainerInput;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.EquipmentSlot;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.protocol.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.protocol.model.inventory.InventorySource;
import net.raphimc.viabedrock.protocol.model.inventory.InventoryTransactionData;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequest;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestSlot;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackResponse;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.InventoryTransactionRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class InventoryPackets {

    private static final int DIALOG_BUTTON_WIDTH = 200;
    private static final int DIALOG_FAKE_BUTTON_WIDTH = 300;
    private static final String DIALOG_FAKE_BUTTON_TEXT = "This is not actually a button, but has to be one because dialogs don't support adding text only elements. Clicking it has the same effect as closing the dialog.";
    // Fallback max stack size for merge predictions. Items with smaller stacks (e.g. ender pearls)
    // get rejected by the server and fall back to a resync, which keeps the inventory consistent
    private static final int MAX_STACK_SIZE = 64;

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.INVENTORY_TRANSACTION, null, wrapper -> {
            final InventoryTransactionRewriter inventoryTransactionRewriter = wrapper.user().get(InventoryTransactionRewriter.class);
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);

            wrapper.cancel();
            final BedrockInventoryTransaction inventoryTransaction = wrapper.read(inventoryTransactionRewriter.getInventoryTransactionType());

            if (inventoryTransaction.legacyRequestId() != 0) {
                // Ignore legacy inventory transactions for now
                return;
            }

            if (inventoryTransaction.actions() != null && !inventoryTransaction.actions().isEmpty()) {
                // Apply all container-inventory actions, then send one content packet per changed container
                final List<Container> changedContainers = new ArrayList<>();
                for (InventoryActionData action : inventoryTransaction.actions()) {
                    if (action.source().type() == InventorySourceType.Container_Inventory) {
                        final Container container = inventoryTracker.getContainerClientbound((byte) action.source().containerId(), null, null);

                        if (container != null) {
                            container.setItem(action.slot(), action.toItem());
                            if (!changedContainers.contains(container)) {
                                changedContainers.add(container);
                            }
                        } else {
                            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received inventory action for unknown container ID: " + action.source().containerId());
                        }
                    }
                }
                for (Container container : changedContainers) {
                    PacketFactory.sendJavaContainerSetContent(wrapper.user(), container);
                }
            }

            if (inventoryTransaction.transactionType() != ComplexInventoryTransaction_Type.NormalTransaction) {
                ViaBedrock.getPlatform().getLogger().log(Level.FINE, "Received unsupported inventory transaction type: " + inventoryTransaction.transactionType());
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ITEM_STACK_RESPONSE, null, wrapper -> {
            wrapper.cancel();
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);

            final ItemStackResponse response = wrapper.read(BedrockTypes.ITEM_STACK_RESPONSE);
            if (response == null) {
                return;
            }

            if (response.result() == ItemStackResponse.RESULT_OK) {
                // The response is the only authoritative sync for accepted requests: apply the returned
                // net ids + amounts to the tracked containers (vanilla sends no follow-up inventory packets)
                if (response.containers() != null) {
                    final List<Container> changedContainers = new ArrayList<>();
                    for (ItemStackResponse.Container responseContainer : response.containers()) {
                        final Container container = resolveResponseContainer(wrapper.user(), inventoryTracker, responseContainer.containerName());
                        if (container == null) {
                            ViaBedrock.getPlatform().getLogger().log(Level.FINE, "Received item stack response for unknown container: " + responseContainer.containerName());
                            continue;
                        }
                        for (ItemStackResponse.Slot responseSlot : responseContainer.slots()) {
                            final int slotIndex = responseSlot.slot() & 0xFF; // The second slot field is the authoritative slot index
                            final BedrockItem tracked = container.getItem(slotIndex);
                            if (responseSlot.amount() <= 0 || (tracked.isEmpty() && responseSlot.serverNetId() == 0)) {
                                if (!tracked.isEmpty()) {
                                    container.setItem(slotIndex, BedrockItem.empty());
                                    if (!changedContainers.contains(container)) {
                                        changedContainers.add(container);
                                    }
                                }
                                continue;
                            }
                            if (tracked.isEmpty()) {
                                continue; // Can't reconcile an item we don't track
                            }
                            final BedrockItem updated = tracked.copy();
                            updated.setAmount(responseSlot.amount());
                            updated.setNetId(responseSlot.serverNetId() > 0 ? responseSlot.serverNetId() : tracked.netId());
                            container.setItem(slotIndex, updated);
                            if (!changedContainers.contains(container)) {
                                changedContainers.add(container);
                            }
                        }
                    }
                    for (Container container : changedContainers) {
                        PacketFactory.sendJavaContainerSetContent(wrapper.user(), container);
                    }
                }
                return;
            }

            // The request was rejected: resync the inventory + open container + cursor to the server state
            ViaBedrock.getPlatform().getLogger().log(Level.FINE, "Item stack request " + response.requestId() + " rejected with result " + response.result());
            PacketFactory.sendJavaContainerSetContent(wrapper.user(), inventoryTracker.getInventoryContainer());
            if (inventoryTracker.getCurrentContainer() != null) {
                PacketFactory.sendJavaContainerSetContent(wrapper.user(), inventoryTracker.getCurrentContainer());
            }
            final PacketWrapper cursorPacket = PacketWrapper.create(ClientboundPackets26_1.SET_CURSOR_ITEM, wrapper.user());
            cursorPacket.write(VersionedTypes.V26_2.item, inventoryTracker.getHudContainer().getJavaItem(0)); // cursor item
            cursorPacket.send(BedrockProtocol.class);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CONTAINER_SET_DATA, ClientboundPackets26_1.CONTAINER_SET_DATA, wrapper -> {
            final int containerId = wrapper.read(Types.UNSIGNED_BYTE); // container id
            final int id = wrapper.read(BedrockTypes.VAR_INT); // property id
            final int value = wrapper.read(BedrockTypes.VAR_INT); // value

            final Container container = wrapper.user().get(InventoryTracker.class).getContainerClientbound((byte) containerId, null, null);
            if (container == null) {
                wrapper.cancel();
                return;
            }

            // Map Bedrock container data properties to Java container data ids. The property ids are
            // overloaded per container type (brewing and furnace use 0-2 differently)
            final int javaId;
            switch (container.type()) {
                case BREWING_STAND -> javaId = switch (id) {
                    case 0 -> 0; // Brew time -> Java: brew time
                    case 1 -> 1; // Brew fuel amount -> Java: fuel
                    default -> -1; // Fuel total is not synced by the Java client
                };
                case FURNACE, BLAST_FURNACE, SMOKER -> javaId = switch (id) {
                    case 0 -> 2; // Furnace tick count -> Java: cooking progress
                    case 1 -> 0; // Furnace lit time -> Java: lit time remaining
                    case 2 -> 1; // Furnace lit duration -> Java: lit duration
                    default -> -1; // Stored XP / fuel aux are not synced by the Java client
                };
                default -> javaId = -1;
            }
            if (javaId == -1) {
                ViaBedrock.getPlatform().getLogger().log(Level.FINE, "Dropping container data property " + id + " for " + container.type());
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, (int) container.javaContainerId()); // container id
            wrapper.write(Types.SHORT, (short) javaId); // property id
            wrapper.write(Types.SHORT, (short) value); // value
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CREATIVE_CONTENT, null, wrapper -> {
            wrapper.cancel();
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);

            final int groupsCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // item groups count
            for (int i = 0; i < groupsCount; i++) {
                wrapper.read(BedrockTypes.VAR_INT); // category
                wrapper.read(BedrockTypes.STRING); // name
            }

            final int itemsCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // item entries count
            final List<InventoryTracker.CreativeItem> creativeItems = new ArrayList<>(itemsCount);
            for (int i = 0; i < itemsCount; i++) {
                final BedrockItem item = wrapper.read(itemRewriter.itemType()); // item
                final int netId = wrapper.read(BedrockTypes.VAR_INT); // net id
                if (!item.isEmpty()) {
                    creativeItems.add(new InventoryTracker.CreativeItem(item, netId));
                }
            }
            wrapper.user().get(InventoryTracker.class).setCreativeItems(creativeItems);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CONTAINER_OPEN, ClientboundPackets26_1.OPEN_SCREEN, wrapper -> {
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            final BlockStateRewriter blockStateRewriter = wrapper.user().get(BlockStateRewriter.class);
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final byte containerId = wrapper.read(Types.BYTE); // container id
            final byte rawType = wrapper.read(Types.BYTE); // type
            final ContainerType type = ContainerType.getByValue(rawType);
            if (type == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown ContainerType: " + rawType);
                wrapper.cancel();
                return;
            }
            final BlockPosition position = wrapper.read(BedrockTypes.BLOCK_POSITION); // position
            wrapper.read(BedrockTypes.VAR_LONG); // entity unique id

            if (inventoryTracker.isAnyScreenOpen()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Server tried to open container while another container is open");
                PacketFactory.sendBedrockContainerClose(wrapper.user(), (byte) -1, ContainerType.NONE);
                wrapper.cancel();
                return;
            }
            final BedrockBlockEntity blockEntity = chunkTracker.getBlockEntity(position);
            TextComponent title = new TranslationComponent("container." + blockStateRewriter.tag(chunkTracker.getBlockState(position)));
            if (blockEntity != null && blockEntity.tag().get("CustomName") instanceof StringTag customNameTag) {
                title = TextUtil.stringToTextComponent(wrapper.user().get(ResourcePackStorage.class).getTexts().translate(customNameTag.getValue()));
            }

            final Container container;
            switch (type) {
                case INVENTORY -> {
                    inventoryTracker.setCurrentContainer(new InventoryContainer(wrapper.user(), containerId, position, inventoryTracker.getInventoryContainer()));
                    wrapper.cancel();
                    return;
                }
                case CONTAINER -> container = new ChestContainer(wrapper.user(), containerId, title, position, 27);
                case MINECART_CHEST, CHEST_BOAT -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 27);
                case WORKBENCH -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 9, 1, "CRAFTING_TABLE"); // Java slot 0 is the result slot
                case CRAFTER -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 10, "CRAFTER");
                case FURNACE -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 3, "FURNACE");
                case BLAST_FURNACE -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 3, "BLAST_FURNACE");
                case SMOKER -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 3, "SMOKER");
                case ANVIL -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 3, "ANVIL");
                case GRINDSTONE -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 3, "GRINDSTONE");
                case ENCHANTMENT -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 2, "ENCHANTING_TABLE");
                case BREWING_STAND -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 5, "BREWING_STAND");
                case DISPENSER -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 9, "DISPENSER");
                case DROPPER -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 9, "DROPPER");
                case HOPPER, MINECART_HOPPER -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 5, "HOPPER");
                case BEACON -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 1, "BEACON");
                case TRADE -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 3);
                case LOOM -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 4, "LOOM");
                case LECTERN -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 1, "LECTERN");
                case STONECUTTER -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 2, "STONECUTTER");
                case CARTOGRAPHY -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 3, "CARTOGRAPHY_TABLE");
                case SMITHING_TABLE -> container = new SimpleContainer(wrapper.user(), containerId, type, title, position, 4, "SMITHING_TABLE");
                case NONE, CAULDRON, JUKEBOX, ARMOR, HAND, HUD, DECORATED_POT -> { // Bedrock client can't open these containers
                    wrapper.cancel();
                    return;
                }
                default -> {
                    wrapper.cancel();
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to open unimplemented container: " + type);
                    PacketFactory.sendBedrockContainerClose(wrapper.user(), containerId, ContainerType.NONE);
                    return;
                }
            }
            inventoryTracker.setCurrentContainer(container);

            wrapper.write(Types.VAR_INT, (int) containerId); // container id
            wrapper.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getBedrockToJavaContainers().get(type)); // type
            wrapper.write(Types.TAG, TextUtil.textComponentToNbt(title)); // title
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CONTAINER_CLOSE, ClientboundPackets26_1.CONTAINER_CLOSE, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.BYTE, Types.VAR_INT); // container id
                handler(wrapper -> {
                    final ContainerType containerType = ContainerType.getByValue(wrapper.read(Types.BYTE)); // type
                    final boolean serverInitiated = wrapper.read(Types.BOOLEAN); // server initiated

                    final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    final Container container = serverInitiated ? inventoryTracker.getCurrentContainer() : inventoryTracker.getPendingCloseContainer();
                    if (container == null) {
                        wrapper.cancel();
                        return;
                    }

                    if (serverInitiated && containerType != container.type()) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Server tried to close container, but container type was not correct");
                        wrapper.cancel();
                        return;
                    }
                    inventoryTracker.setCurrentContainerClosed(serverInitiated);
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.INVENTORY_CONTENT, ClientboundPackets26_1.CONTAINER_SET_CONTENT, wrapper -> {
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);
            final int containerId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // container id
            final BedrockItem[] items = wrapper.read(itemRewriter.newItemArrayType()); // items
            final FullContainerName containerName = wrapper.read(BedrockTypes.FULL_CONTAINER_NAME); // container name
            final BedrockItem storageItem = wrapper.read(itemRewriter.newItemType()); // storage item

            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final Container container = inventoryTracker.getContainerClientbound((byte) containerId, containerName, storageItem);
            if (container != null && container.setItems(items)) {
                PacketFactory.writeJavaContainerSetContent(wrapper, container);
            } else {
                wrapper.cancel();
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.INVENTORY_SLOT, ClientboundPackets26_1.CONTAINER_SET_SLOT, wrapper -> {
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);
            final int containerId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // container id
            final int slot = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // slot
            final FullContainerName containerName = wrapper.read(BedrockTypes.OPTIONAL_FULL_CONTAINER_NAME); // container name
            final BedrockItem storageItem = wrapper.read(itemRewriter.optionalNewItemType()); // storage item
            final BedrockItem item = wrapper.read(itemRewriter.newItemType()); // item

            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final Container container = inventoryTracker.getContainerClientbound((byte) containerId, containerName, storageItem);
            if (container != null && container.setItem(slot, item)) {
                if (container.type() == ContainerType.HUD && slot == 0) { // cursor item
                    wrapper.setPacketType(ClientboundPackets26_1.SET_CURSOR_ITEM);
                } else {
                    wrapper.write(Types.VAR_INT, (int) container.javaContainerId()); // container id
                    wrapper.write(Types.VAR_INT, 0); // revision
                    wrapper.write(Types.SHORT, (short) container.javaSlot(slot)); // slot
                }
                wrapper.write(VersionedTypes.V26_2.item, container.getJavaItem(slot)); // item
            } else {
                wrapper.cancel();
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MODAL_FORM_REQUEST, ClientboundPackets26_1.SHOW_DIALOG, wrapper -> {
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final int id = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // id
            final String data = wrapper.read(BedrockTypes.STRING); // data

            if (inventoryTracker.getCurrentContainer() != null || inventoryTracker.getCurrentForm() != null) {
                final PacketWrapper modalFormResponse = PacketWrapper.create(ServerboundBedrockPackets.MODAL_FORM_RESPONSE, wrapper.user());
                modalFormResponse.write(BedrockTypes.UNSIGNED_VAR_INT, id); // id
                modalFormResponse.write(Types.BOOLEAN, false); // has response
                modalFormResponse.write(Types.BOOLEAN, true); // has cancel reason
                modalFormResponse.write(Types.BYTE, (byte) ModalFormCancelReason.UserBusy.getValue()); // cancel reason
                modalFormResponse.sendToServer(BedrockProtocol.class);
                wrapper.cancel();
                return;
            }

            final Form form;
            try {
                form = FormSerializer.deserialize(data);
            } catch (Throwable e) { // Bedrock client shows error modal form
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error while deserializing form data: " + data, e);
                wrapper.cancel();
                return;
            }
            final ResourcePackStorage resourcePackStorage = wrapper.user().get(ResourcePackStorage.class);
            form.setTranslator(resourcePackStorage.getTexts()::translate);
            inventoryTracker.setCurrentForm(IntObjectPair.of(id, form));

            final Identifier responseIdentifier = Identifier.of("viabedrock", "form/" + id);
            final CompoundTag exitButtonAdditions = new CompoundTag();
            exitButtonAdditions.putBoolean("exit", true);
            final ActionButton exitButton = new ActionButton(new StringComponent(resourcePackStorage.getTexts().get("gui.close")), DIALOG_BUTTON_WIDTH, new CustomAllAction(responseIdentifier, exitButtonAdditions));

            final Dialog dialog;
            if (form instanceof ModalForm modalForm) {
                final MultiActionDialog actionDialog = new MultiActionDialog(TextUtil.stringToTextComponent(form.getTitle()), true, false, AfterAction.CLOSE, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), exitButton, 1);
                addTextToDialog(wrapper.user(), actionDialog, modalForm.getText());
                final CompoundTag button1Additions = new CompoundTag();
                button1Additions.putInt("button_id", 0);
                actionDialog.getActions().add(new ActionButton(TextUtil.stringToTextComponent(modalForm.getButton1()), DIALOG_BUTTON_WIDTH, new CustomAllAction(responseIdentifier, button1Additions)));
                final CompoundTag button2Additions = new CompoundTag();
                button2Additions.putInt("button_id", 1);
                actionDialog.getActions().add(new ActionButton(TextUtil.stringToTextComponent(modalForm.getButton2()), DIALOG_BUTTON_WIDTH, new CustomAllAction(responseIdentifier, button2Additions)));
                dialog = actionDialog;
            } else if (form instanceof ActionForm actionForm) {
                if (actionForm.getElements().length == 0) { // Text only form
                    final NoticeDialog noticeDialog = new NoticeDialog(TextUtil.stringToTextComponent(form.getTitle()), true, false, AfterAction.CLOSE, new ArrayList<>(), new ArrayList<>(), exitButton);
                    addTextToDialog(wrapper.user(), noticeDialog, actionForm.getText());
                    dialog = noticeDialog;
                } else {
                    final MultiActionDialog actionDialog = new MultiActionDialog(TextUtil.stringToTextComponent(form.getTitle()), true, false, AfterAction.CLOSE, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), exitButton, 1);
                    addTextToDialog(wrapper.user(), actionDialog, actionForm.getText());
                    int buttonIndex = 0;
                    for (int elementIndex = 0; elementIndex < actionForm.getElements().length; elementIndex++) {
                        final FormElement element = actionForm.getElements()[elementIndex];
                        if (element instanceof ButtonFormElement button) {
                            final CompoundTag buttonAdditions = new CompoundTag();
                            buttonAdditions.putInt("button_id", buttonIndex);
                            actionDialog.getActions().add(new ActionButton(TextUtil.stringToTextComponent(button.getText()), DIALOG_BUTTON_WIDTH, new CustomAllAction(responseIdentifier, buttonAdditions)));
                            buttonIndex++;
                        } else if (element instanceof HeaderFormElement header) {
                            actionDialog.getActions().add(new ActionButton(TextUtil.stringToTextComponent(header.getText()), new StringComponent(DIALOG_FAKE_BUTTON_TEXT), DIALOG_FAKE_BUTTON_WIDTH, exitButton.getAction()));
                        } else if (element instanceof LabelFormElement label) {
                            actionDialog.getActions().add(new ActionButton(TextUtil.stringToTextComponent(label.getText()), new StringComponent(DIALOG_FAKE_BUTTON_TEXT), DIALOG_FAKE_BUTTON_WIDTH, exitButton.getAction()));
                        } else if (element instanceof DividerFormElement) {
                        } else {
                            throw new IllegalArgumentException("Unhandled form element type: " + element.getClass().getSimpleName());
                        }
                    }
                    dialog = actionDialog;
                }
            } else if (form instanceof CustomForm customForm) {
                final MultiActionDialog actionDialog = new MultiActionDialog(TextUtil.stringToTextComponent(form.getTitle()), true, false, AfterAction.CLOSE, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), exitButton, 1);
                for (int elementIndex = 0; elementIndex < customForm.getElements().length; elementIndex++) {
                    final FormElement element = customForm.getElements()[elementIndex];
                    final String inputKey = String.valueOf(elementIndex);
                    if (element instanceof CheckboxFormElement checkbox) {
                        final BooleanInput booleanInput = new BooleanInput(TextUtil.stringToTextComponent(checkbox.getText()));
                        booleanInput.setInitial(checkbox.getDefaultValue());
                        actionDialog.getInputs().add(new Input(inputKey, booleanInput));
                    } else if (element instanceof DropdownFormElement dropdown) {
                        final SingleOptionInput singleOptionInput = new SingleOptionInput(new ArrayList<>(dropdown.getOptions().length), TextUtil.stringToTextComponent(dropdown.getText()));
                        for (int dropdownIndex = 0; dropdownIndex < dropdown.getOptions().length; dropdownIndex++) {
                            final String option = dropdown.getOptions()[dropdownIndex];
                            singleOptionInput.getOptions().add(new SingleOptionInput.Entry(String.valueOf(dropdownIndex), TextUtil.stringToTextComponent(option), dropdownIndex == dropdown.getDefaultOption()));
                        }
                        actionDialog.getInputs().add(new Input(inputKey, singleOptionInput));
                    } else if (element instanceof SliderFormElement slider) {
                        final NumberRangeInput numberRangeInput = new NumberRangeInput(TextUtil.stringToTextComponent(slider.getText()), new NumberRangeInput.Range(slider.getMin(), slider.getMax(), slider.getDefaultValue(), slider.getStep()));
                        actionDialog.getInputs().add(new Input(inputKey, numberRangeInput));
                    } else if (element instanceof StepSliderFormElement stepSlider) {
                        final SingleOptionInput singleOptionInput = new SingleOptionInput(new ArrayList<>(stepSlider.getSteps().length), TextUtil.stringToTextComponent(stepSlider.getText()));
                        for (int stepIndex = 0; stepIndex < stepSlider.getSteps().length; stepIndex++) {
                            final String step = stepSlider.getSteps()[stepIndex];
                            final String stepKey = String.valueOf(stepIndex);
                            singleOptionInput.getOptions().add(new SingleOptionInput.Entry(stepKey, TextUtil.stringToTextComponent(step), stepIndex == stepSlider.getDefaultStep()));
                        }
                        actionDialog.getInputs().add(new Input(inputKey, singleOptionInput));
                    } else if (element instanceof TextFieldFormElement textField) {
                        final TextInput textInput = new TextInput(TextUtil.stringToTextComponent(textField.getText()));
                        textInput.setMaxLength(100);
                        textInput.setInitial(textField.getDefaultValue());
                        actionDialog.getInputs().add(new Input(inputKey, textInput));
                    } else if (element instanceof HeaderFormElement header) {
                        addTextToDialog(wrapper.user(), actionDialog, header.getText());
                    } else if (element instanceof LabelFormElement label) {
                        addTextToDialog(wrapper.user(), actionDialog, label.getText());
                    } else if (element instanceof DividerFormElement) {
                        if (wrapper.user().getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_6)) {
                            final TextInput textInput = new TextInput(new StringComponent());
                            textInput.setLabelVisible(false);
                            textInput.setMaxLength(Integer.MAX_VALUE);
                            textInput.setMultiline(new TextInput.MultilineOptions(null, 1));
                            actionDialog.getInputs().add(new Input("dummy", textInput));
                        }
                    } else {
                        throw new IllegalArgumentException("Unhandled form element type: " + element.getClass().getSimpleName());
                    }
                }
                actionDialog.getActions().add(new ActionButton(TextUtil.stringToTextComponent(resourcePackStorage.getTexts().get("gui.submit")), DIALOG_BUTTON_WIDTH, new CustomAllAction(responseIdentifier, null)));
                dialog = actionDialog;
            } else {
                throw new IllegalArgumentException("Unhandled form type: " + form.getClass().getSimpleName());
            }

            wrapper.write(Types.TRUSTED_COMPOUND_TAG_HOLDER, Holder.of((CompoundTag) DialogSerializer.V1_21_6.getDirectCodec().serialize(NbtConverter_v1_21_5.INSTANCE, dialog).get())); // dialog data
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CLOSE_FORM, ClientboundPackets26_1.CLEAR_DIALOG, wrapper -> {
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            if (inventoryTracker.getCurrentForm() != null) {
                inventoryTracker.closeCurrentForm();
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_HOTBAR, ClientboundPackets26_1.SET_HELD_SLOT, wrapper -> {
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();
            final int slot = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // selected slot
            final byte containerId = wrapper.read(Types.BYTE); // container id
            final boolean shouldSelectSlot = wrapper.read(Types.BOOLEAN); // should select slot
            if (slot >= 0 && slot < 9 && containerId == inventoryContainer.containerId() && shouldSelectSlot) {
                wrapper.write(Types.VAR_INT, slot); // slot
            } else {
                wrapper.cancel();
                if (containerId != inventoryContainer.containerId()) { // Bedrock client doesn't render hotbar selection and held item anymore
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to set hotbar slot with wrong container id: " + containerId);
                }
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CONTAINER_REGISTRY_CLEANUP, null, wrapper -> {
            wrapper.cancel();
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final FullContainerName[] removedContainers = wrapper.read(BedrockTypes.FULL_CONTAINER_NAME_ARRAY); // removed containers
            for (FullContainerName containerName : removedContainers) {
                inventoryTracker.removeDynamicContainer(containerName);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_ARMOR_DAMAGE, ClientboundPackets26_1.SET_EQUIPMENT, wrapper -> {
            if (!wrapper.user().get(GameSessionStorage.class).isInventoryServerAuthoritative()) {
                wrapper.cancel();
                return;
            }
            final int size = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // size
            if (size <= 0) {
                wrapper.cancel();
                return;
            }
            final Container armorContainer = wrapper.user().get(InventoryTracker.class).getArmorContainer();

            wrapper.write(Types.VAR_INT, wrapper.user().get(EntityTracker.class).getClientPlayer().javaId()); // entity id
            for (int i = 0; i < size; i++) {
                final int rawArmorSlot = wrapper.read(BedrockTypes.VAR_INT); // armor slot
                final SharedTypes_Legacy_ArmorSlot armorSlot = SharedTypes_Legacy_ArmorSlot.getByValue(rawArmorSlot);
                if (armorSlot == null) { // Bedrock client ignores the whole packet if an unknown armor slot is sent
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown SharedTypes_Legacy_ArmorSlot: " + rawArmorSlot);
                    wrapper.cancel();
                    return;
                }
                final short damage = wrapper.read(BedrockTypes.SHORT_LE); // damage

                final BedrockItem item = armorSlot.getValue() < armorContainer.size() ? armorContainer.getItem(armorSlot.getValue()) : BedrockItem.empty();
                if (item.tag() == null) {
                    item.setTag(new CompoundTag());
                }
                item.tag().putInt("Damage", damage);

                final EquipmentSlot equipmentSlot = switch (armorSlot) {
                    case Head -> EquipmentSlot.HEAD;
                    case Torso -> EquipmentSlot.CHEST;
                    case Legs -> EquipmentSlot.LEGS;
                    case Feet -> EquipmentSlot.FEET;
                    case Body -> EquipmentSlot.BODY;
                };
                wrapper.write(Types.BYTE, (byte) (equipmentSlot.ordinal() | (i < (size - 1) ? Byte.MIN_VALUE : 0))); // slot
                wrapper.write(VersionedTypes.V26_2.item, wrapper.user().get(ItemRewriter.class).javaItem(item)); // item
            }
        });

        protocol.registerServerbound(ServerboundPackets26_1.CONTAINER_CLICK, null, wrapper -> {
            wrapper.cancel();
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final int containerId = wrapper.read(Types.VAR_INT); // container id
            final int revision = wrapper.read(Types.VAR_INT); // state id
            final short slot = wrapper.read(Types.SHORT); // slot
            final byte button = wrapper.read(Types.BYTE); // button
            final ContainerInput[] containerInputs = ContainerInput.values();
            final int actionOrdinal = wrapper.read(Types.VAR_INT); // action
            if (actionOrdinal < 0 || actionOrdinal >= containerInputs.length) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown container input action: " + actionOrdinal);
                resyncClick(wrapper.user(), inventoryTracker, inventoryTracker.getInventoryContainer());
                return;
            }
            final ContainerInput action = containerInputs[actionOrdinal];

            if (inventoryTracker.getPendingCloseContainer() != null) {
                return;
            }
            final Container container = inventoryTracker.getContainerServerbound((byte) containerId);
            if (container == null) {
                if (containerId == ContainerID.CONTAINER_ID_INVENTORY.getValue()) {
                    // Bedrock client can send multiple OpenInventory requests if the server doesn't respond, so this is fine here
                    final PacketWrapper interact = PacketWrapper.create(ServerboundBedrockPackets.INTERACT, wrapper.user());
                    interact.write(Types.UNSIGNED_BYTE, (short) InteractPacketPayload_Action.OpenInventory.getValue()); // action
                    interact.write(BedrockTypes.UNSIGNED_VAR_LONG, wrapper.user().get(EntityTracker.class).getClientPlayer().runtimeId()); // target entity runtime id
                    interact.write(BedrockTypes.OPTIONAL_POSITION_3F, null); // position
                    interact.sendToServer(BedrockProtocol.class);
                    PacketFactory.sendJavaContainerSetContent(wrapper.user(), inventoryTracker.getInventoryContainer());
                }
                return;
            }

            final List<ItemStackRequestAction> actions;
            if (gameSession.isInventoryServerAuthoritative()) {
                actions = buildItemStackRequestActions(inventoryTracker, container, slot, button, action);
            } else {
                // Client-authoritative: clicks are communicated with legacy inventory transactions
                actions = null;
                if (!translateClickToInventoryTransaction(wrapper.user(), inventoryTracker, container, slot, button, action)) {
                    resyncClick(wrapper.user(), inventoryTracker, container);
                }
            }
            if (actions != null && !actions.isEmpty()) {
                final ItemStackRequest request = new ItemStackRequest(inventoryTracker.nextItemStackRequestId(), actions, new ArrayList<>(), 0);
                final PacketWrapper requestPacket = PacketWrapper.create(ServerboundBedrockPackets.ITEM_STACK_REQUEST, wrapper.user());
                requestPacket.write(BedrockTypes.ITEM_STACK_REQUEST, request);
                requestPacket.sendToServer(BedrockProtocol.class);
            } else if (gameSession.isInventoryServerAuthoritative() && actions == null) {
                resyncClick(wrapper.user(), inventoryTracker, container);
            }
        });
        protocol.registerServerbound(ServerboundPackets26_1.SET_CREATIVE_MODE_SLOT, null, wrapper -> {
            wrapper.cancel();
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final short slot = wrapper.read(Types.SHORT); // slot
            final Item item = wrapper.read(VersionedTypes.V26_2.lengthPrefixedItem); // item

            if (inventoryTracker.getPendingCloseContainer() != null) {
                return;
            }

            if (gameSession.isInventoryServerAuthoritative() && !item.isEmpty()) {
                // Translate to a craft creative request using the creative content cache
                final int creativeIndex = inventoryTracker.findCreativeItemIndex(wrapper.user().get(ItemRewriter.class), item);
                if (creativeIndex != -1) {
                    final int creativeNetId = inventoryTracker.getCreativeItemNetId(creativeIndex);
                    final ItemStackRequestSlot destination = inventoryRequestSlot(inventoryTracker, slot & 0xFFFF);
                    if (destination != null) {
                        final int amount = Math.max(1, item.amount());
                        // The crafted item materializes in the created output container; its net id is unknown until the server responds
                        final ItemStackRequestSlot createdOutput = new ItemStackRequestSlot(new FullContainerName(ContainerEnumName.CreatedOutputContainer, null), (byte) 0, 0);
                        final ItemStackRequest request = new ItemStackRequest(inventoryTracker.nextItemStackRequestId(), List.of(
                                ItemStackRequestAction.craftCreative(creativeNetId, 1),
                                // Vanilla clients acknowledge the craft with an empty deprecated craft results action
                                ItemStackRequestAction.craftResultsDeprecated(),
                                ItemStackRequestAction.take(amount, createdOutput, destination)
                        ), new ArrayList<>(), 0);
                        final PacketWrapper requestPacket = PacketWrapper.create(ServerboundBedrockPackets.ITEM_STACK_REQUEST, wrapper.user());
                        requestPacket.write(BedrockTypes.ITEM_STACK_REQUEST, request);
                        requestPacket.sendToServer(BedrockProtocol.class);
                        return;
                    }
                }
            }

            PacketFactory.sendJavaContainerSetContent(wrapper.user(), inventoryTracker.getInventoryContainer());
        });
        protocol.registerServerbound(ServerboundPackets26_1.CUSTOM_CLICK_ACTION, ServerboundBedrockPackets.MODAL_FORM_RESPONSE, wrapper -> {
            final String id = wrapper.read(Types.STRING); // id
            final CompoundTag payload = (CompoundTag) wrapper.read(Types.CUSTOM_CLICK_ACTION_TAG); // payload
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            if (inventoryTracker.getCurrentForm() == null) {
                wrapper.cancel();
                return;
            }

            final Form form = inventoryTracker.getCurrentForm().right();
            final int formId = inventoryTracker.getCurrentForm().leftInt();
            if (!id.equals("viabedrock:form/" + formId)) {
                wrapper.cancel();
                return;
            }

            inventoryTracker.setCurrentForm(null);
            if (payload.contains("exit") && payload.getBoolean("exit")) {
                wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, formId); // id
                wrapper.write(Types.BOOLEAN, false); // has response
                wrapper.write(Types.BOOLEAN, true); // has cancel reason
                wrapper.write(Types.BYTE, (byte) ModalFormCancelReason.UserClosed.getValue()); // cancel reason
                return;
            }

            if (form instanceof ModalForm modalForm) {
                modalForm.setClickedButton(payload.getInt("button_id"));
            } else if (form instanceof ActionForm actionForm) {
                actionForm.setClickedButton(payload.getInt("button_id"));
            } else if (form instanceof CustomForm customForm) {
                for (int elementIndex = 0; elementIndex < customForm.getElements().length; elementIndex++) {
                    final String inputKey = String.valueOf(elementIndex);
                    if (!payload.contains(inputKey)) continue;
                    final FormElement element = customForm.getElements()[elementIndex];
                    if (element instanceof CheckboxFormElement checkbox) {
                        checkbox.setChecked(payload.getBoolean(inputKey));
                    } else if (element instanceof DropdownFormElement dropdown) {
                        dropdown.setSelected(Integer.parseInt(payload.getString(inputKey)));
                    } else if (element instanceof SliderFormElement slider) {
                        slider.setCurrent(payload.getFloat(inputKey));
                    } else if (element instanceof StepSliderFormElement stepSlider) {
                        stepSlider.setSelected(Integer.parseInt(payload.getString(inputKey)));
                    } else if (element instanceof TextFieldFormElement textField) {
                        textField.setValue(payload.getString(inputKey));
                    }
                }
            } else {
                throw new IllegalArgumentException("Unhandled form type: " + form.getClass().getSimpleName());
            }

            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, formId); // id
            wrapper.write(Types.BOOLEAN, true); // has response
            wrapper.write(BedrockTypes.STRING, form.serializeResponse() + '\n'); // response
            wrapper.write(Types.BOOLEAN, false); // has cancel reason
        });
        protocol.registerServerbound(ServerboundPackets26_1.CONTAINER_CLOSE, ServerboundBedrockPackets.CONTAINER_CLOSE, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.VAR_INT, Types.BYTE); // container id
                create(Types.BYTE, (byte) ContainerType.NONE.getValue()); // type
                create(Types.BOOLEAN, false); // server initiated
                handler(wrapper -> {
                    final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    final byte containerId = wrapper.get(Types.BYTE, 0);
                    final Container container = inventoryTracker.getContainerServerbound(containerId);
                    if (container == null) {
                        wrapper.cancel();
                        return;
                    }

                    if (container.javaContainerId() != container.containerId()) {
                        wrapper.set(Types.BYTE, 0, container.containerId());
                    }
                    inventoryTracker.markPendingClose(container);
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets26_1.SET_CARRIED_ITEM, ServerboundBedrockPackets.MOB_EQUIPMENT, wrapper -> {
            final short slot = wrapper.read(Types.SHORT); // slot
            wrapper.user().get(InventoryTracker.class).getInventoryContainer().setSelectedHotbarSlot((byte) slot, wrapper); // slot
        });
        protocol.registerServerbound(ServerboundPackets26_1.PICK_ITEM_FROM_BLOCK, ServerboundBedrockPackets.BLOCK_PICK_REQUEST, wrapper -> {
            wrapper.passthroughAndMap(Types.BLOCK_POSITION1_14, BedrockTypes.BLOCK_POSITION); // position
            wrapper.passthrough(Types.BOOLEAN); // include data
            wrapper.write(Types.UNSIGNED_BYTE, (short) 9); // number of empty hotbar slots (vanilla client always sends 9)
        });
        protocol.registerServerbound(ServerboundPackets26_1.PICK_ITEM_FROM_ENTITY, ServerboundBedrockPackets.ENTITY_PICK_REQUEST, wrapper -> {
            final int entityId = wrapper.read(Types.VAR_INT); // entity id
            final boolean includeData = wrapper.read(Types.BOOLEAN); // include data

            final Entity entity = wrapper.user().get(EntityTracker.class).getEntityByJid(entityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(BedrockTypes.LONG_LE, entity.uniqueId()); // entity unique id
            wrapper.write(Types.UNSIGNED_BYTE, (short) 9); // number of empty hotbar slots (vanilla client always sends 9)
            wrapper.write(Types.BOOLEAN, includeData); // include data
        });
    }

    private static void addTextToDialog(final UserConnection userConnection, final Dialog dialog, final String text) {
        if (dialog.getInputs().isEmpty()) {
            for (String line : BedrockTextUtils.split(text, "\n")) {
                dialog.getBody().add(new PlainMessageBody(TextUtil.stringToTextComponent(line)));
            }
        } else {
            if (userConnection.getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_6)) {
                for (String line : BedrockTextUtils.split(text, "\n")) {
                    final TextInput textInput = new TextInput(TextUtil.stringToTextComponent(line));
                    textInput.setMaxLength(Integer.MAX_VALUE);
                    textInput.setMultiline(new TextInput.MultilineOptions(null, 1));
                    dialog.getInputs().add(new Input("dummy", textInput));
                }
            } else { // VB compatibility
                dialog.getInputs().add(new Input("dummy", new BooleanInput(TextUtil.stringToTextComponent(text))));
            }
        }
    }

    /**
     * Translates a Java container click into item stack request actions (server-auth inventory).
     * Returns null when the click can't be mapped and the containers need a resync instead.
     */
    private static List<ItemStackRequestAction> buildItemStackRequestActions(final InventoryTracker inventoryTracker, final Container container, final int javaSlot, final byte button, final ContainerInput action) {
        final ItemStackRequestSlot source = requestSlotInfo(inventoryTracker, container, javaSlot & 0xFFFF);
        final ItemStackRequestSlot cursor = cursorSlot(inventoryTracker);
        final BedrockItem cursorItem = inventoryTracker.getHudContainer().getItem(0);
        final int clickedBedrockSlot = container.bedrockSlot(javaSlot & 0xFFFF);
        final BedrockItem clicked = clickedBedrockSlot >= 0 && clickedBedrockSlot < container.size() ? container.getItem(clickedBedrockSlot) : BedrockItem.empty();

        switch (action) {
            case PICKUP -> {
                if (source == null) {
                    return null;
                }
                if (cursorItem.isEmpty() && clicked.isEmpty()) {
                    return new ArrayList<>(); // No-op click: don't spam the server or the Java client with resyncs
                }
                if (button == 0) {
                    if (cursorItem.isEmpty() && !clicked.isEmpty()) {
                        return List.of(ItemStackRequestAction.take(clicked.amount(), source, cursor));
                    } else if (!cursorItem.isEmpty() && clicked.isEmpty()) {
                        return List.of(ItemStackRequestAction.place(cursorItem.amount(), cursor, source));
                    } else if (!cursorItem.isEmpty() && !cursorItem.isDifferent(clicked)) {
                        // Placing onto the same item type: cap at the max stack size, the server syncs any remainder
                        final int movable = Math.min(cursorItem.amount(), Math.max(0, MAX_STACK_SIZE - clicked.amount()));
                        if (movable <= 0) {
                            return new ArrayList<>();
                        }
                        return List.of(ItemStackRequestAction.place(movable, cursor, source));
                    } else if (!cursorItem.isEmpty() && cursorItem.isDifferent(clicked)) {
                        return List.of(ItemStackRequestAction.swap(cursor, source));
                    }
                } else if (button == 1) {
                    if (cursorItem.isEmpty() && !clicked.isEmpty()) {
                        return List.of(ItemStackRequestAction.take((clicked.amount() + 1) / 2, source, cursor));
                    } else if (!cursorItem.isEmpty() && clicked.isEmpty()) {
                        return List.of(ItemStackRequestAction.place(1, cursor, source));
                    } else if (!cursorItem.isEmpty() && !cursorItem.isDifferent(clicked)) {
                        final int movable = Math.min(1, Math.max(0, MAX_STACK_SIZE - clicked.amount()));
                        if (movable <= 0) {
                            return new ArrayList<>();
                        }
                        return List.of(ItemStackRequestAction.place(1, cursor, source));
                    } else if (!cursorItem.isEmpty() && cursorItem.isDifferent(clicked)) {
                        return List.of(ItemStackRequestAction.swap(cursor, source));
                    }
                }
                return null;
            }
            case SWAP -> {
                if (source == null) {
                    return null;
                }
                if (button >= 0 && button <= 8) {
                    return List.of(ItemStackRequestAction.swap(source, hotbarRequestSlot(button, inventoryTracker.getInventoryContainer().getItem(button))));
                } else if (button == 40) {
                    return List.of(ItemStackRequestAction.swap(source, new ItemStackRequestSlot(new FullContainerName(ContainerEnumName.OffhandContainer, null), (byte) 1, netIdOf(inventoryTracker.getOffhandContainer().getItem(0)))));
                }
                return null;
            }
            case THROW -> {
                if (source == null || clicked.isEmpty()) {
                    return null;
                }
                if (button == 0) {
                    return List.of(ItemStackRequestAction.drop(1, source, false));
                } else if (button == 1) {
                    return List.of(ItemStackRequestAction.drop(clicked.amount(), source, false));
                }
                return null;
            }
            default -> {
                // QUICK_MOVE, CLONE, QUICK_CRAFT and PICKUP_ALL need destination computation and fall back to a resync
                return null;
            }
        }
    }

    private static void resyncClick(final UserConnection user, final InventoryTracker inventoryTracker, final Container container) {
        if (container.type() != ContainerType.INVENTORY) {
            PacketFactory.sendJavaContainerSetContent(user, inventoryTracker.getInventoryContainer());
        }
        PacketFactory.sendJavaContainerSetContent(user, container);
    }

    /**
     * Resolves a container from a response FullContainerName to the tracked container.
     */
    private static Container resolveResponseContainer(final UserConnection user, final InventoryTracker inventoryTracker, final FullContainerName containerName) {
        if (containerName == null) {
            return null;
        }
        return switch (containerName.name()) {
            case InventoryContainer, HotbarContainer, CombinedHotbarAndInventoryContainer -> inventoryTracker.getInventoryContainer();
            case CursorContainer -> inventoryTracker.getHudContainer();
            case LevelEntityContainer, CrafterLevelEntityContainer -> inventoryTracker.getCurrentContainer();
            default -> {
                // Per-type container names (anvil input, furnace fuel, ...) all address the open container
                final Container currentContainer = inventoryTracker.getCurrentContainer();
                yield currentContainer != null ? currentContainer : inventoryTracker.getContainerClientbound((byte) ContainerID.CONTAINER_ID_REGISTRY.getValue(), containerName, null);
            }
        };
    }

    /**
     * Java player inventory slot -> Bedrock item stack request slot info.
     * Container names follow the vanilla client: INVENTORY for the main inventory, HOTBAR for the hotbar.
     */
    private static ItemStackRequestSlot inventoryRequestSlot(final InventoryTracker inventoryTracker, final int javaSlot) {
        if (javaSlot >= 9 && javaSlot <= 35) {
            return new ItemStackRequestSlot(new FullContainerName(ContainerEnumName.InventoryContainer, null), (byte) javaSlot, netIdOf(inventoryTracker.getInventoryContainer().getItem(javaSlot)));
        } else if (javaSlot >= 36 && javaSlot <= 44) {
            return new ItemStackRequestSlot(new FullContainerName(ContainerEnumName.HotbarContainer, null), (byte) (javaSlot - 36), netIdOf(inventoryTracker.getInventoryContainer().getItem(javaSlot - 36)));
        } else if (javaSlot >= 5 && javaSlot <= 8) {
            return new ItemStackRequestSlot(new FullContainerName(ContainerEnumName.ArmorContainer, null), (byte) (javaSlot - 5), netIdOf(inventoryTracker.getArmorContainer().getItem(javaSlot - 5)));
        } else if (javaSlot == 45) {
            // The vanilla client sends slot 1 for the offhand (a known client quirk since 1.19.70)
            return new ItemStackRequestSlot(new FullContainerName(ContainerEnumName.OffhandContainer, null), (byte) 1, netIdOf(inventoryTracker.getOffhandContainer().getItem(0)));
        } else if (javaSlot >= 1 && javaSlot <= 4) {
            // The vanilla client uses the UI slot offsets 28-31 for the 2x2 crafting input
            return new ItemStackRequestSlot(new FullContainerName(ContainerEnumName.CraftingInputContainer, null), (byte) (28 + javaSlot - 1), netIdOf(inventoryTracker.getHudContainer().getItem(28 + javaSlot - 1)));
        }
        return null; // Crafting result slot and unknown slots
    }

    private static ItemStackRequestSlot requestSlotInfo(final InventoryTracker inventoryTracker, final Container container, final int javaSlot) {
        if (container.type() == ContainerType.INVENTORY || container == inventoryTracker.getInventoryContainer()) {
            return inventoryRequestSlot(inventoryTracker, javaSlot);
        }
        // Open containers are anchored to block entities: Bedrock networked as level entity containers
        final ContainerEnumName containerName = container.type() == ContainerType.CRAFTER ? ContainerEnumName.CrafterLevelEntityContainer : ContainerEnumName.LevelEntityContainer;
        final int bedrockSlot = container.bedrockSlot(javaSlot);
        if (bedrockSlot < 0 || bedrockSlot >= container.size()) {
            return null;
        }
        return new ItemStackRequestSlot(new FullContainerName(containerName, null), (byte) bedrockSlot, netIdOf(container.getItem(bedrockSlot)));
    }

    private static ItemStackRequestSlot cursorSlot(final InventoryTracker inventoryTracker) {
        return new ItemStackRequestSlot(new FullContainerName(ContainerEnumName.CursorContainer, null), (byte) 0, netIdOf(inventoryTracker.getHudContainer().getItem(0)));
    }

    private static ItemStackRequestSlot hotbarRequestSlot(final int hotbarSlot, final BedrockItem item) {
        return new ItemStackRequestSlot(new FullContainerName(ContainerEnumName.HotbarContainer, null), (byte) hotbarSlot, netIdOf(item));
    }

    private static int netIdOf(final BedrockItem item) {
        return item == null || item.isEmpty() || item.netId() == null ? 0 : item.netId();
    }

    /**
     * Client-authoritative path: translates a Java container click into a legacy inventory transaction.
     * Returns false when the click can't be mapped and the containers need a resync instead.
     */
    private static boolean translateClickToInventoryTransaction(final UserConnection user, final InventoryTracker inventoryTracker, final Container container, final int javaSlot, final byte button, final ContainerInput action) {
        final int bedSlot = container.bedrockSlot(javaSlot & 0xFFFF);
        if (bedSlot < 0 || bedSlot >= container.size()) {
            return false;
        }
        final BedrockItem clicked = container.getItem(bedSlot);
        final BedrockItem cursorItem = inventoryTracker.getHudContainer().getItem(0);
        final InventorySource slotSource = new InventorySource(InventorySourceType.Container_Inventory, container.containerId(), InventorySource_InventorySourceFlags.No_Flag);
        final InventorySource cursorSource = new InventorySource(InventorySourceType.Container_Inventory, ContainerID.CONTAINER_ID_PLAYER_ONLY_UI.getValue(), InventorySource_InventorySourceFlags.No_Flag);

        final List<InventoryActionData> actions = new ArrayList<>();
        switch (action) {
            case PICKUP -> {
                if (cursorItem.isEmpty() && clicked.isEmpty()) {
                    return true; // No-op click
                }
                if (button == 0) {
                    if (cursorItem.isEmpty() && !clicked.isEmpty()) {
                        actions.add(new InventoryActionData(slotSource, bedSlot, clicked, BedrockItem.empty()));
                        actions.add(new InventoryActionData(cursorSource, 0, BedrockItem.empty(), clicked.copy()));
                        container.setItem(bedSlot, BedrockItem.empty());
                        inventoryTracker.getHudContainer().setItem(0, clicked.copy());
                    } else if (!cursorItem.isEmpty() && clicked.isEmpty()) {
                        actions.add(new InventoryActionData(cursorSource, 0, cursorItem, BedrockItem.empty()));
                        actions.add(new InventoryActionData(slotSource, bedSlot, clicked, cursorItem.copy()));
                        container.setItem(bedSlot, cursorItem.copy());
                        inventoryTracker.getHudContainer().setItem(0, BedrockItem.empty());
                    } else if (!cursorItem.isEmpty() && !cursorItem.isDifferent(clicked)) {
                        // Placing onto the same item type: cap at the max stack size
                        final int movable = Math.min(cursorItem.amount(), Math.max(0, MAX_STACK_SIZE - clicked.amount()));
                        if (movable <= 0) {
                            return true;
                        }
                        final BedrockItem newCursor;
                        if (cursorItem.amount() > movable) {
                            newCursor = cursorItem.copy();
                            newCursor.setAmount(cursorItem.amount() - movable);
                        } else {
                            newCursor = BedrockItem.empty();
                        }
                        final BedrockItem newSlot = clicked.copy();
                        newSlot.setAmount(clicked.amount() + movable);
                        actions.add(new InventoryActionData(cursorSource, 0, cursorItem, newCursor));
                        actions.add(new InventoryActionData(slotSource, bedSlot, clicked, newSlot));
                        container.setItem(bedSlot, newSlot);
                        inventoryTracker.getHudContainer().setItem(0, newCursor);
                    } else if (!cursorItem.isEmpty() && cursorItem.isDifferent(clicked)) {
                        actions.add(new InventoryActionData(cursorSource, 0, cursorItem, clicked.copy()));
                        actions.add(new InventoryActionData(slotSource, bedSlot, clicked, cursorItem.copy()));
                        container.setItem(bedSlot, cursorItem.copy());
                        inventoryTracker.getHudContainer().setItem(0, clicked.copy());
                    } else {
                        return false;
                    }
                } else if (button == 1) {
                    if (cursorItem.isEmpty() && !clicked.isEmpty()) {
                        final BedrockItem half = clicked.copy();
                        half.setAmount((clicked.amount() + 1) / 2);
                        final BedrockItem remaining = clicked.copy();
                        remaining.setAmount(clicked.amount() - half.amount());
                        actions.add(new InventoryActionData(slotSource, bedSlot, clicked, remaining));
                        actions.add(new InventoryActionData(cursorSource, 0, BedrockItem.empty(), half));
                        container.setItem(bedSlot, remaining);
                        inventoryTracker.getHudContainer().setItem(0, half);
                    } else if (!cursorItem.isEmpty() && (clicked.isEmpty() || !cursorItem.isDifferent(clicked))) {
                        if (clicked.amount() >= MAX_STACK_SIZE) {
                            return true; // Can't place more onto a full stack
                        }
                        // Place one item from the cursor
                        final BedrockItem newCursor;
                        if (cursorItem.amount() > 1) {
                            newCursor = cursorItem.copy();
                            newCursor.setAmount(cursorItem.amount() - 1);
                        } else {
                            newCursor = BedrockItem.empty();
                        }
                        final BedrockItem newSlot = clicked.isEmpty() ? cursorItem.copy() : clicked.copy();
                        newSlot.setAmount(clicked.amount() + 1);
                        actions.add(new InventoryActionData(cursorSource, 0, cursorItem, newCursor));
                        actions.add(new InventoryActionData(slotSource, bedSlot, clicked, newSlot));
                        container.setItem(bedSlot, newSlot);
                        inventoryTracker.getHudContainer().setItem(0, newCursor);
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            case THROW -> {
                if (clicked.isEmpty()) {
                    return true;
                }
                final BedrockItem dropped = clicked.copy();
                dropped.setAmount(button == 0 ? 1 : Math.max(1, clicked.amount()));
                final BedrockItem predictedTo;
                if (button == 0 && clicked.amount() > 1) {
                    predictedTo = clicked.copy();
                    predictedTo.setAmount(clicked.amount() - 1);
                } else {
                    predictedTo = BedrockItem.empty();
                }
                actions.add(new InventoryActionData(new InventorySource(InventorySourceType.World_Interaction, ContainerID.CONTAINER_ID_NONE.getValue(), InventorySource_InventorySourceFlags.No_Flag), 0, BedrockItem.empty(), dropped));
                actions.add(new InventoryActionData(slotSource, bedSlot, clicked, predictedTo));
                container.setItem(bedSlot, predictedTo);
            }
            default -> {
                return false;
            }
        }

        final BedrockInventoryTransaction inventoryTransaction = new BedrockInventoryTransaction(
                0, // legacy request id
                null,
                actions,
                ComplexInventoryTransaction_Type.NormalTransaction,
                new InventoryTransactionData.NormalTransactionData()
        );
        final PacketWrapper transactionPacket = PacketWrapper.create(ServerboundBedrockPackets.INVENTORY_TRANSACTION, user);
        transactionPacket.write(user.get(InventoryTransactionRewriter.class).getInventoryTransactionType(), inventoryTransaction);
        transactionPacket.sendToServer(BedrockProtocol.class);
        return true;
    }

}
