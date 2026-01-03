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

public interface ItemDescriptor {

    ItemDescriptorType getType();

    record ComplexAliasDescriptor(String name) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.COMPLEX_ALIAS;
        }
    }

    record DefaultDescriptor(int itemId, int auxValue) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.DEFAULT;
        }
    }

    record DeferredDescriptor(String fullName, int auxValue) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.DEFERRED;
        }
    }

    record InvalidDescriptor() implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.INVALID;
        }
    }

    record ItemTagDescriptor(String itemTag) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.ITEM_TAG;
        }
    }

    record MolangDescriptor(String tagExpression, int molangVersion) implements ItemDescriptor {
        @Override
        public ItemDescriptorType getType() {
            return ItemDescriptorType.MOLANG;
        }
    }

}