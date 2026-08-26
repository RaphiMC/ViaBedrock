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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemStackRequestActionType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequest;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.List;

/**
 * Serializes the Bedrock ItemStackRequest structure. Only writing is implemented because this structure is only ever
 * sent to the server.
 */
public class ItemStackRequestType extends Type<ItemStackRequest> {

    private final UserConnection user;

    public ItemStackRequestType(final UserConnection user) {
        super(ItemStackRequest.class);

        this.user = user;
    }

    @Override
    public ItemStackRequest read(final ByteBuf buffer) {
        throw new UnsupportedOperationException("Reading item stack requests is not supported");
    }

    @Override
    public void write(final ByteBuf buffer, final ItemStackRequest value) {
        BedrockTypes.VAR_INT.write(buffer, value.requestId()); // request id
        final List<ItemStackRequestAction> actions = value.actions();
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, actions.size()); // actions count
        for (ItemStackRequestAction action : actions) {
            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, wireTypeId(action.type())); // action type
            buffer.writeByte(action.type().getValue()); // legacy action type
            this.writeAction(buffer, action);
        }
        BedrockTypes.STRING_ARRAY.write(buffer, value.filterStrings()); // filter strings
        buffer.writeIntLE(value.filterStringCause()); // filter string cause
    }

    /**
     * 1.26.40 dropped the two deprecated item container actions from the enum, which shifted every id after them
     * down by two. The generated enum still holds the old numbering, which is now only used for the legacy id.
     *
     * @param type The action type
     * @return The id the current protocol uses for it
     */
    private static int wireTypeId(final ItemStackRequestActionType type) {
        final int legacyId = type.getValue();
        return legacyId >= ItemStackRequestActionType.ScreenLabTableCombine.getValue() ? legacyId - 2 : legacyId;
    }

    private void writeAction(final ByteBuf buffer, final ItemStackRequestAction action) {
        if (action instanceof ItemStackRequestAction.Take take) {
            buffer.writeByte(take.count()); // count
            BedrockTypes.ITEM_STACK_REQUEST_SLOT_INFO.write(buffer, take.source()); // source
            BedrockTypes.ITEM_STACK_REQUEST_SLOT_INFO.write(buffer, take.destination()); // destination
        } else if (action instanceof ItemStackRequestAction.Place place) {
            buffer.writeByte(place.count()); // count
            BedrockTypes.ITEM_STACK_REQUEST_SLOT_INFO.write(buffer, place.source()); // source
            BedrockTypes.ITEM_STACK_REQUEST_SLOT_INFO.write(buffer, place.destination()); // destination
        } else if (action instanceof ItemStackRequestAction.Swap swap) {
            BedrockTypes.ITEM_STACK_REQUEST_SLOT_INFO.write(buffer, swap.slot1()); // slot 1
            BedrockTypes.ITEM_STACK_REQUEST_SLOT_INFO.write(buffer, swap.slot2()); // slot 2
        } else if (action instanceof ItemStackRequestAction.Drop drop) {
            buffer.writeByte(drop.count()); // count
            BedrockTypes.ITEM_STACK_REQUEST_SLOT_INFO.write(buffer, drop.source()); // source
            buffer.writeBoolean(drop.randomly()); // randomly
        } else if (action instanceof ItemStackRequestAction.Destroy destroy) {
            buffer.writeByte(destroy.count()); // count
            BedrockTypes.ITEM_STACK_REQUEST_SLOT_INFO.write(buffer, destroy.source()); // source
        } else if (action instanceof ItemStackRequestAction.Consume consume) {
            buffer.writeByte(consume.count()); // count
            BedrockTypes.ITEM_STACK_REQUEST_SLOT_INFO.write(buffer, consume.source()); // source
        } else if (action instanceof ItemStackRequestAction.Create create) {
            buffer.writeByte(create.resultSlot()); // result slot
        } else if (action instanceof ItemStackRequestAction.CraftCreative craftCreative) {
            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, craftCreative.creativeItemNetworkId()); // creative item network id
            buffer.writeByte(craftCreative.repetitions()); // repetitions
        } else if (action instanceof ItemStackRequestAction.CraftRecipe craftRecipe) {
            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, craftRecipe.recipeNetworkId()); // recipe network id
            buffer.writeByte(craftRecipe.repetitions()); // repetitions
        } else if (action instanceof ItemStackRequestAction.CraftRecipeAuto craftRecipeAuto) {
            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, craftRecipeAuto.recipeNetworkId()); // recipe network id
            buffer.writeByte(craftRecipeAuto.repetitions()); // repetitions
            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, 0); // ingredient count
        } else if (action instanceof ItemStackRequestAction.CraftRecipeOptional craftRecipeOptional) {
            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, craftRecipeOptional.recipeNetworkId()); // recipe network id
            buffer.writeIntLE(craftRecipeOptional.filterStringIndex()); // filter string index
        } else if (action instanceof ItemStackRequestAction.CraftRepairAndDisenchant craftRepairAndDisenchant) {
            buffer.writeIntLE(craftRepairAndDisenchant.recipeNetworkId()); // recipe network id
            buffer.writeByte(craftRepairAndDisenchant.repetitions()); // repetitions
            BedrockTypes.VAR_INT.write(buffer, craftRepairAndDisenchant.repairCost()); // repair cost
        } else if (action instanceof ItemStackRequestAction.CraftLoom craftLoom) {
            BedrockTypes.STRING.write(buffer, craftLoom.patternId()); // pattern id
            buffer.writeByte(craftLoom.repetitions()); // repetitions
        } else if (action instanceof ItemStackRequestAction.CraftResultsDeprecated craftResults) {
            this.user.get(ItemRewriter.class).itemArrayTypeWithoutNetId().write(buffer, craftResults.results().toArray(new BedrockItem[0])); // results
            buffer.writeByte(craftResults.iterations()); // iterations
        } else if (action instanceof ItemStackRequestAction.MineBlock mineBlock) {
            BedrockTypes.VAR_INT.write(buffer, mineBlock.hotbarSlot()); // hotbar slot
            BedrockTypes.VAR_INT.write(buffer, mineBlock.predictedDurability()); // predicted durability
            buffer.writeIntLE(mineBlock.stackNetworkId()); // stack network id
        } else {
            throw new IllegalArgumentException("Unhandled item stack request action: " + action.getClass().getSimpleName());
        }
    }

}
