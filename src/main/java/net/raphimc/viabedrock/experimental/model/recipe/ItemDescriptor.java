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
package net.raphimc.viabedrock.experimental.model.recipe;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;

public interface ItemDescriptor {

    ItemDescriptorType getType();
    default void writeJavaIngredientData(final PacketWrapper packet, final UserConnection user) {
        throw new UnsupportedOperationException("Not implemented for " + getType());
    }

    record ComplexAliasDescriptor(String name) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.COMPLEX_ALIAS;
        }
        @Override
        public void writeJavaIngredientData(final PacketWrapper packet, final UserConnection user) {
            //TODO
            packet.write(Types.VAR_INT, 0); // Slot Display Type (empty)
        }
    }

    record DefaultDescriptor(int itemId, int auxValue) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.DEFAULT;
        }

        @Override
        public void writeJavaIngredientData(final PacketWrapper packet, final UserConnection user) {
            ItemRewriter itemRewriter = user.get(ItemRewriter.class);
            Item javaItem = itemRewriter.javaItem(new BedrockItem(itemId));
            if (javaItem == null) {
                throw new IllegalStateException("Could not find Java item for Bedrock ID: " + itemId);
            }

            packet.write(Types.VAR_INT, 2); // Slot Display Type (Item)
            packet.write(Types.VAR_INT, javaItem.identifier()); // Item ID
        }
    }

    record DeferredDescriptor(String fullName, int auxValue) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.DEFERRED;
        }

        @Override
        public void writeJavaIngredientData(final PacketWrapper packet, final UserConnection user) {
            ItemRewriter itemRewriter = user.get(ItemRewriter.class);
            int itemId = itemRewriter.getItems().get(fullName); //TODO: Check if this is correct
            Item javaItem = itemRewriter.javaItem(new BedrockItem(itemId));
            if (javaItem == null) {
                throw new IllegalStateException("Could not find Java item for Bedrock ID: " + itemId);
            }

            packet.write(Types.VAR_INT, 2); // Slot Display Type (Item)
            packet.write(Types.VAR_INT, javaItem.identifier()); // Item ID
        }
    }

    record InvalidDescriptor() implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.INVALID;
        }

        @Override
        public void writeJavaIngredientData(final PacketWrapper packet, final UserConnection user) {
            packet.write(Types.VAR_INT, 0); // Slot Display Type (empty)
        }
    }

    record ItemTagDescriptor(String itemTag) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.ITEM_TAG;
        }

        @Override
        public void writeJavaIngredientData(final PacketWrapper packet, final UserConnection user) {
            packet.write(Types.VAR_INT, 4); // Slot Display Type (Tag)
            //TODO: Convert to Java Tag properly
            packet.write(Types.IDENTIFIER, Key.of(itemTag)); // Item Tag
        }
    }

    record MolangDescriptor(String tagExpression, int molangVersion) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.MOLANG;
        }
    }

}