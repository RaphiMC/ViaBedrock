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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.lenni0451.mcstructs_bedrock.forms.AForm;
import net.lenni0451.mcstructs_bedrock.forms.serializer.FormSerializer;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.model.inventory.Container;
import net.raphimc.viabedrock.api.model.inventory.fake.FakeContainer;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.MenuType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.java.ClickType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.provider.FormProvider;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.logging.Level;

public class InventoryPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.CONTAINER_OPEN, ClientboundPackets1_21.OPEN_SCREEN, wrapper -> {
            final byte windowId = wrapper.read(Types.BYTE); // window id
            final byte rawType = wrapper.read(Types.BYTE); // type
            final BlockPosition position = wrapper.read(BedrockTypes.BLOCK_POSITION); // position
            wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
            final ContainerType type = ContainerType.getByValue(rawType);
            if (type == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown container type: " + rawType);
                return;
            }
            final MenuType menuType = MenuType.getByBedrockContainerType(type);
            if (menuType == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to open unimplemented container: " + type);
                wrapper.cancel();
                PacketFactory.sendContainerClose(wrapper.user(), windowId, ContainerType.NONE);
                return;
            }
            if (menuType.equals(MenuType.INVENTORY)) {
                // TODO: Implement
                wrapper.cancel();
                return;
            }

            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            final BlockStateRewriter blockStateRewriter = wrapper.user().get(BlockStateRewriter.class);
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final BedrockBlockEntity blockEntity = chunkTracker.getBlockEntity(position);
            ATextComponent title = new TranslationComponent("container." + blockStateRewriter.tag(chunkTracker.getBlockState(position)));
            if (blockEntity != null && blockEntity.tag().get("CustomName") instanceof StringTag customNameTag) {
                title = TextUtil.stringToComponent(wrapper.user().get(ResourcePacksStorage.class).translate(customNameTag.getValue()));
            }

            final Container container = menuType.createContainer(windowId);
            inventoryTracker.trackContainer(position, container);

            wrapper.write(Types.VAR_INT, (int) windowId); // window id
            wrapper.write(Types.VAR_INT, menuType.javaMenuTypeId()); // type
            wrapper.write(Types.TAG, TextUtil.componentToNbt(title)); // title
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CONTAINER_CLOSE, ClientboundPackets1_21.CONTAINER_CLOSE, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.BYTE, Types.UNSIGNED_BYTE); // window id
                handler(wrapper -> {
                    final ContainerType containerType = ContainerType.getByValue(wrapper.read(Types.BYTE)); // type
                    final boolean serverInitiated = wrapper.read(Types.BOOLEAN); // server initiated

                    final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    final Container container = serverInitiated ? inventoryTracker.getCurrentContainer() : inventoryTracker.getPendingCloseContainer();
                    if (container != null) {
                        if (containerType != ContainerType.NONE && containerType != container.menuType().bedrockContainerType()) {
                            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Server tried to close container, but container type was not correct");
                            wrapper.cancel();
                            return;
                        }

                        wrapper.send(BedrockProtocol.class);
                        wrapper.cancel();
                        inventoryTracker.setCurrentContainerClosed();

                        if (serverInitiated) {
                            PacketFactory.sendContainerClose(wrapper.user(), container.windowId(), ContainerType.NONE);
                        }
                    } else if (inventoryTracker.getCurrentFakeContainer() != null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Server tried to close container, but no container was open");
                        wrapper.cancel();
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.INVENTORY_CONTENT, ClientboundPackets1_21.CONTAINER_SET_CONTENT, wrapper -> {
            final int windowId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // window id
            final BedrockItem[] items = wrapper.read(wrapper.user().get(ItemRewriter.class).itemArrayType()); // items

            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final Container container = inventoryTracker.getContainer((byte) windowId);
            if (container == null) {
                wrapper.cancel();
                return;
            }

            try {
                container.setItems(items);
            } catch (IllegalArgumentException e) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to set items for " + container + ", but items array length was not correct");
                wrapper.cancel();
                return;
            }

            inventoryTracker.writeWindowItems(wrapper, container);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MODAL_FORM_REQUEST, null, wrapper -> {
            wrapper.cancel();
            final int id = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // id
            final String data = wrapper.read(BedrockTypes.STRING); // data

            final FormProvider formProvider = Via.getManager().getProviders().get(FormProvider.class);
            if (formProvider.isAnyScreenOpen(wrapper.user())) {
                formProvider.sendModalFormResponse(wrapper.user(), id, null);
                return;
            }

            final AForm form;
            try {
                form = FormSerializer.deserialize(data);
            } catch (Throwable e) { // Mojang client shows error modal form
                throw new IllegalArgumentException("Error while deserializing form data: " + data, e);
            }
            form.setTranslator(wrapper.user().get(ResourcePacksStorage.class)::translate);
            Via.getManager().getProviders().get(FormProvider.class).openModalForm(wrapper.user(), id, form);
        });

        protocol.registerServerbound(ServerboundPackets1_20_5.CONTAINER_CLICK, null, wrapper -> {
            wrapper.cancel();
            final byte windowId = wrapper.read(Types.UNSIGNED_BYTE).byteValue(); // window id
            final int revision = wrapper.read(Types.VAR_INT); // revision
            final short slot = wrapper.read(Types.SHORT); // slot
            final byte button = wrapper.read(Types.BYTE); // button
            final ClickType action = ClickType.values()[wrapper.read(Types.VAR_INT)]; // action

            wrapper.user().get(InventoryTracker.class).handleWindowClick(windowId, revision, slot, button, action);
        });
        protocol.registerServerbound(ServerboundPackets1_20_5.CONTAINER_CLOSE, ServerboundBedrockPackets.CONTAINER_CLOSE, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.UNSIGNED_BYTE, Types.BYTE); // window id
                create(Types.BYTE, (byte) ContainerType.NONE.getValue()); // type
                create(Types.BOOLEAN, false); // server initiated
                handler(wrapper -> {
                    final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    if (wrapper.get(Types.BYTE, 0) == ContainerID.CONTAINER_ID_INVENTORY.getValue()) {
                        if (inventoryTracker.getCurrentContainer() != null) {
                            wrapper.set(Types.BYTE, 0, inventoryTracker.getCurrentContainer().windowId());
                        } else {
                            wrapper.cancel();
                            return;
                        }
                    }

                    if (!inventoryTracker.markPendingClose(true)) {
                        wrapper.cancel();
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_20_5.RENAME_ITEM, null, wrapper -> {
            wrapper.cancel();
            final String name = wrapper.read(Types.STRING); // name
            final FakeContainer fakeContainer = wrapper.user().get(InventoryTracker.class).getCurrentFakeContainer();
            if (fakeContainer != null) {
                fakeContainer.onAnvilRename(name);
            }
        });
    }

}
