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
package net.raphimc.viabedrock.experimental.model.container.block;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.experimental.model.container.ExperimentalContainer;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.model.FullContainerName;

public class SmithingContainer extends ExperimentalContainer {

    public SmithingContainer(UserConnection user, byte containerId, ContainerType type, TextComponent title, BlockPosition position) {
        super(user, containerId, type, title, position, 3, "smithing_table");
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        return switch (slot) {
            case 53 -> new FullContainerName(ContainerEnumName.SmithingTableTemplateContainer, null);
            case 51 -> new FullContainerName(ContainerEnumName.SmithingTableInputContainer, null);
            case 52 -> new FullContainerName(ContainerEnumName.SmithingTableMaterialContainer, null);
            case 50 -> new FullContainerName(ContainerEnumName.SmithingTableResultPreviewContainer, null);
            default -> throw new IllegalArgumentException("Invalid slot for Smithing Container: " + slot);
        };
    }

    @Override
    public int javaSlot(final int slot) {
        return switch (slot) {
            case 53 -> 0;
            case 51 -> 1;
            case 52 -> 2;
            case 50 -> 3;
            default -> super.javaSlot(slot);
        };
    }

    @Override
    public int bedrockSlot(final int slot) {
        return switch (slot) {
            case 0 -> 53;
            case 1 -> 51;
            case 2 -> 52;
            case 3 -> 50;
            default -> super.bedrockSlot(slot);
        };
    }

}

//[22:24:41:011] [SERVER BOUND] - ItemStackRequestPacket(requests=[ItemStackRequest(requestId=-31, actions=[CraftRecipeAction(recipeNetworkId=821, numberOfRequestedCrafts=1), CraftResultsDeprecatedAction(resultItems=[BaseItemData(definition=SimpleItemDefinition(identifier=minecraft:golden_helmet, runtimeId=383, version=LEGACY, componentBased=false, componentData=null), damage=0, count=1, tag={
//        "Damage": 0i,
//        "Trim": {
//        "Material": "iron",
//        "Pattern": "wild"
//        }
//        }, canPlace=[], canBreak=[], blockingTicks=0, blockDefinition=UnknownDefinition[runtimeId=0], usingNetId=false, netId=0)], timesCrafted=1), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=SMITHING_TABLE_TEMPLATE, slot=53, stackNetworkId=11, containerName=FullContainerName(container=SMITHING_TABLE_TEMPLATE, dynamicId=null))), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=SMITHING_TABLE_INPUT, slot=51, stackNetworkId=10, containerName=FullContainerName(container=SMITHING_TABLE_INPUT, dynamicId=null))), ConsumeAction(count=1, source=ItemStackRequestSlotData(container=SMITHING_TABLE_MATERIAL, slot=52, stackNetworkId=8, containerName=FullContainerName(container=SMITHING_TABLE_MATERIAL, dynamicId=null))), TakeAction(count=1, source=ItemStackRequestSlotData(container=CREATED_OUTPUT, slot=50, stackNetworkId=-31, containerName=FullContainerName(container=CREATED_OUTPUT, dynamicId=null)), destination=ItemStackRequestSlotData(container=CURSOR, slot=0, stackNetworkId=0, containerName=FullContainerName(container=CURSOR, dynamicId=null)))], filterStrings=[], textProcessingEventOrigin=null)])
// [22:24:41:055] [CLIENT BOUND] - ItemStackResponsePacket(entries=[ItemStackResponse(result=OK, requestId=-31, containers=[ItemStackResponseContainer(container=SMITHING_TABLE_TEMPLATE, items=[ItemStackResponseSlot(slot=53, hotbarSlot=53, count=63, stackNetworkId=11, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=SMITHING_TABLE_TEMPLATE, dynamicId=null)), ItemStackResponseContainer(container=SMITHING_TABLE_INPUT, items=[ItemStackResponseSlot(slot=51, hotbarSlot=51, count=0, stackNetworkId=0, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=SMITHING_TABLE_INPUT, dynamicId=null)), ItemStackResponseContainer(container=SMITHING_TABLE_MATERIAL, items=[ItemStackResponseSlot(slot=52, hotbarSlot=52, count=59, stackNetworkId=8, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=SMITHING_TABLE_MATERIAL, dynamicId=null)), ItemStackResponseContainer(container=CURSOR, items=[ItemStackResponseSlot(slot=0, hotbarSlot=0, count=1, stackNetworkId=12, customName=, durabilityCorrection=0, filteredCustomName=)], containerName=FullContainerName(container=CURSOR, dynamicId=null))])])
