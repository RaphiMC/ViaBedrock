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
package net.raphimc.viabedrock.protocol.types.inventory;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequest;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestSlot;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Wire layout per CloudburstMC BedrockCodecHelper_v2168 (ViaBedrock targets protocol 2169).
 */
public class ItemStackRequestPacketType extends Type<ItemStackRequest> {

    public ItemStackRequestPacketType() {
        super("ItemStackRequest", ItemStackRequest.class);
    }

    @Override
    public ItemStackRequest read(final ByteBuf buffer) {
        final int requestId = BedrockTypes.VAR_INT.read(buffer); // client request id (signed varint)
        final int actionsCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // actions count
        final List<ItemStackRequestAction> actions = new ArrayList<>(actionsCount);
        for (int i = 0; i < actionsCount; i++) {
            actions.add(readAction(buffer));
        }
        final int stringsToFilterCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // strings to filter count
        final List<String> stringsToFilter = new ArrayList<>(stringsToFilterCount);
        for (int i = 0; i < stringsToFilterCount; i++) {
            stringsToFilter.add(BedrockTypes.STRING.read(buffer)); // string to filter
        }
        final int stringsToFilterOrigin = buffer.readIntLE(); // strings to filter origin (little endian int, -1 = none)
        return new ItemStackRequest(requestId, actions, stringsToFilter, stringsToFilterOrigin);
    }

    @Override
    public void write(final ByteBuf buffer, final ItemStackRequest request) {
        BedrockTypes.VAR_INT.write(buffer, request.requestId()); // client request id (signed varint)
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, request.actions().size()); // actions count
        for (ItemStackRequestAction action : request.actions()) {
            writeAction(buffer, action);
        }
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, request.stringsToFilter() != null ? request.stringsToFilter().size() : 0); // strings to filter count
        if (request.stringsToFilter() != null) {
            for (String stringToFilter : request.stringsToFilter()) {
                BedrockTypes.STRING.write(buffer, stringToFilter); // string to filter
            }
        }
        buffer.writeIntLE(request.stringsToFilterOrigin()); // strings to filter origin (little endian int)
    }

    private ItemStackRequestAction readAction(final ByteBuf buffer) {
        final int actionType = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // action type (unsigned varint)
        final net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackRequestActionType type =
                net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackRequestActionType.getByValue(actionType);
        if (type == null) {
            throw new IllegalArgumentException("Unknown item stack request action type: " + actionType);
        }

        Integer amount = null;
        ItemStackRequestSlot source = null;
        ItemStackRequestSlot destination = null;
        Boolean throwRandomly = null;
        Integer createSlot = null;
        Integer primaryEffect = null;
        Integer secondaryEffect = null;
        Integer recipeNetId = null;
        Integer timesCrafted = null;
        Integer creativeItemNetId = null;
        Integer filteredStringIndex = null;
        Integer repairCost = null;
        String patternId = null;
        Integer hotbarSlot = null;
        Integer predictedDurability = null;
        Integer mineStackNetworkId = null;

        switch (type) {
            case Take, Place, PlaceInItemContainer, TakeFromItemContainer -> {
                amount = (int) Types.BYTE.read(buffer); // count (unsigned byte)
                source = readSlot(buffer);
                destination = readSlot(buffer);
            }
            case Swap -> {
                source = readSlot(buffer);
                destination = readSlot(buffer);
            }
            case Drop -> {
                amount = (int) Types.BYTE.read(buffer); // count (unsigned byte)
                source = readSlot(buffer);
                throwRandomly = Types.BOOLEAN.read(buffer); // throw randomly
            }
            case Destroy, Consume -> {
                amount = (int) Types.BYTE.read(buffer); // count (unsigned byte)
                source = readSlot(buffer);
            }
            case Create -> createSlot = (int) Types.UNSIGNED_BYTE.read(buffer); // slot (unsigned byte)
            case ScreenBeaconPayment -> {
                primaryEffect = BedrockTypes.VAR_INT.read(buffer); // primary effect
                secondaryEffect = BedrockTypes.VAR_INT.read(buffer); // secondary effect
            }
            case ScreenHUDMineBlock -> {
                hotbarSlot = BedrockTypes.VAR_INT.read(buffer); // hotbar slot
                predictedDurability = BedrockTypes.VAR_INT.read(buffer); // predicted durability
                mineStackNetworkId = buffer.readIntLE(); // stack network id (little endian int)
            }
            case CraftRecipe -> {
                recipeNetId = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // recipe net id
                timesCrafted = (int) Types.BYTE.read(buffer); // number of requested crafts
            }
            case CraftRecipeAuto -> {
                recipeNetId = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // recipe net id
                timesCrafted = (int) Types.BYTE.read(buffer); // number of requested crafts
                // Ingredient descriptors follow (varint type + byte type + payload + u16 count);
                // reading them requires the item definition registry, which requests never need on this side
                throw new UnsupportedOperationException("Reading CraftRecipeAuto actions is not supported");
            }
            case CraftCreative -> {
                creativeItemNetId = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // creative item network id
                timesCrafted = (int) Types.BYTE.read(buffer); // number of requested crafts
            }
            case CraftRecipeOptional -> {
                recipeNetId = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // recipe net id
                filteredStringIndex = buffer.readIntLE(); // filtered string index (little endian int)
            }
            case CraftRepairAndDisenchant -> {
                recipeNetId = buffer.readIntLE(); // recipe net id (little endian int)
                timesCrafted = (int) Types.BYTE.read(buffer); // number of requested crafts
                repairCost = BedrockTypes.VAR_INT.read(buffer); // repair cost
            }
            case CraftLoom -> {
                patternId = BedrockTypes.STRING.read(buffer); // pattern id
                timesCrafted = (int) Types.UNSIGNED_BYTE.read(buffer); // times crafted
            }
            case ScreenLabTableCombine, CraftNonImplemented -> {
                // No additional payload
            }
            case CraftResults -> {
                timesCrafted = (int) Types.BYTE.read(buffer); // times crafted
                // The result item instances are skipped: reading them requires the item definition registry
                final int itemsCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
                if (itemsCount > 0) {
                    throw new UnsupportedOperationException("Reading craft result item instances is not supported");
                }
            }
            default -> throw new IllegalArgumentException("Unhandled item stack request action type: " + type);
        }

        return new ItemStackRequestAction(type, amount, source, destination, throwRandomly, createSlot, primaryEffect, secondaryEffect, recipeNetId, timesCrafted, creativeItemNetId, filteredStringIndex, repairCost, patternId, hotbarSlot, predictedDurability, mineStackNetworkId);
    }

    private void writeAction(final ByteBuf buffer, final ItemStackRequestAction action) {
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, action.type().getValue()); // action type (unsigned varint)

        switch (action.type()) {
            case Take, Place, PlaceInItemContainer, TakeFromItemContainer -> {
                Types.BYTE.write(buffer, action.amount().byteValue()); // count
                writeSlot(buffer, action.source());
                writeSlot(buffer, action.destination());
            }
            case Swap -> {
                writeSlot(buffer, action.source());
                writeSlot(buffer, action.destination());
            }
            case Drop -> {
                Types.BYTE.write(buffer, action.amount().byteValue()); // count
                writeSlot(buffer, action.source());
                Types.BOOLEAN.write(buffer, action.throwRandomly()); // throw randomly
            }
            case Destroy, Consume -> {
                Types.BYTE.write(buffer, action.amount().byteValue()); // count
                writeSlot(buffer, action.source());
            }
            case Create -> Types.UNSIGNED_BYTE.write(buffer, action.createSlot().shortValue()); // slot
            case ScreenBeaconPayment -> {
                BedrockTypes.VAR_INT.write(buffer, action.primaryEffect()); // primary effect
                BedrockTypes.VAR_INT.write(buffer, action.secondaryEffect()); // secondary effect
            }
            case ScreenHUDMineBlock -> {
                BedrockTypes.VAR_INT.write(buffer, action.hotbarSlot()); // hotbar slot
                BedrockTypes.VAR_INT.write(buffer, action.predictedDurability()); // predicted durability
                buffer.writeIntLE(action.mineStackNetworkId()); // stack network id
            }
            case CraftRecipe -> {
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, action.recipeNetId()); // recipe net id
                Types.BYTE.write(buffer, action.timesCrafted().byteValue()); // number of requested crafts
            }
            case CraftRecipeAuto -> {
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, action.recipeNetId()); // recipe net id
                Types.BYTE.write(buffer, action.timesCrafted().byteValue()); // number of requested crafts
                // Ingredient descriptors are not emitted by ViaBedrock; sending this action without them is invalid
                ViaBedrock.getPlatform().getLogger().log(Level.FINE, "CraftRecipeAuto action written without ingredient descriptors");
            }
            case CraftCreative -> {
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, action.creativeItemNetId()); // creative item network id
                Types.BYTE.write(buffer, action.timesCrafted().byteValue()); // number of requested crafts
            }
            case CraftRecipeOptional -> {
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, action.recipeNetId()); // recipe net id
                buffer.writeIntLE(action.filteredStringIndex()); // filtered string index
            }
            case CraftRepairAndDisenchant -> {
                buffer.writeIntLE(action.recipeNetId()); // recipe net id
                Types.BYTE.write(buffer, action.timesCrafted().byteValue()); // number of requested crafts
                BedrockTypes.VAR_INT.write(buffer, action.repairCost()); // repair cost
            }
            case CraftLoom -> {
                BedrockTypes.STRING.write(buffer, action.patternId()); // pattern id
                Types.UNSIGNED_BYTE.write(buffer, action.timesCrafted().shortValue()); // times crafted
            }
            case ScreenLabTableCombine, CraftNonImplemented -> {
                // No additional payload
            }
            case CraftResults -> {
                // Vanilla clients send the crafted result items here; an empty list is accepted as a no-op acknowledgment
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, 0); // result items count
                Types.BYTE.write(buffer, action.timesCrafted() != null ? action.timesCrafted().byteValue() : 0); // times crafted
            }
            default -> throw new IllegalArgumentException("Unhandled item stack request action type: " + action.type());
        }
    }

    private ItemStackRequestSlot readSlot(final ByteBuf buffer) {
        final FullContainerName containerName = BedrockTypes.FULL_CONTAINER_NAME.read(buffer); // full container name
        final byte slot = Types.BYTE.read(buffer); // slot
        final int netId = buffer.readIntLE(); // stack network id (little endian int)
        return new ItemStackRequestSlot(containerName, slot, netId);
    }

    private void writeSlot(final ByteBuf buffer, final ItemStackRequestSlot slot) {
        BedrockTypes.FULL_CONTAINER_NAME.write(buffer, slot.containerName()); // full container name
        Types.BYTE.write(buffer, slot.slot()); // slot
        buffer.writeIntLE(slot.netId()); // stack network id (little endian int)
    }

}
