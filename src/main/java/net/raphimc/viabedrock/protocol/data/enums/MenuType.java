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
package net.raphimc.viabedrock.protocol.data.enums;

import net.raphimc.viabedrock.api.model.inventory.ChestContainer;
import net.raphimc.viabedrock.api.model.inventory.Container;
import net.raphimc.viabedrock.api.model.inventory.InventoryContainer;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public enum MenuType {

    INVENTORY(ContainerType.INVENTORY, null, InventoryContainer::new),
    CONTAINER(ContainerType.CONTAINER, "minecraft:generic_9x3", windowId -> new ChestContainer(windowId, 27), "chest", "trapped_chest"),
    DO_NOT_USE_ANVIL(ContainerType.ANVIL, "minecraft:anvil", windowId -> null, "anvil");

    // TODO: Add remaining menu types

    private final ContainerType bedrockContainerType;
    private final int javaMenuTypeId;
    private final Function<Byte, Container> containerSupplier;
    private final Set<String> acceptedTags;

    MenuType(final ContainerType bedrockContainerType, final String javaMenuType, final Function<Byte, Container> containerSupplier, final String... acceptedTags) {
        this.bedrockContainerType = bedrockContainerType;
        this.containerSupplier = containerSupplier;
        this.acceptedTags = new HashSet<>(Arrays.asList(acceptedTags));

        if (javaMenuType != null) {
            this.javaMenuTypeId = BedrockProtocol.MAPPINGS.getJavaMenus().getOrDefault(javaMenuType, -1);
            if (this.javaMenuTypeId == -1) {
                throw new IllegalArgumentException("Unknown java menu type: " + javaMenuType);
            }
        } else {
            this.javaMenuTypeId = -1;
        }
    }

    public static MenuType getByBedrockContainerType(final ContainerType containerType) {
        for (final MenuType menuType : values()) {
            if (menuType.bedrockContainerType() == containerType) {
                return menuType;
            }
        }

        return null;
    }

    public ContainerType bedrockContainerType() {
        return this.bedrockContainerType;
    }

    public int javaMenuTypeId() {
        return this.javaMenuTypeId;
    }

    public Container createContainer(final byte windowId) {
        return this.containerSupplier.apply(windowId);
    }

    public boolean isAcceptedTag(final String tag) {
        return this.acceptedTags.contains(tag);
    }

}
