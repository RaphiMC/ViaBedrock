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
package net.raphimc.viabedrock.protocol.model;

import com.viaversion.viaversion.api.minecraft.item.Item;
import net.raphimc.viabedrock.protocol.model.BedrockItem;

import java.util.List;

/**
 * The trades a villager offers.
 *
 * @param containerId  The container the trades belong to
 * @param tier         The villager's level
 * @param leveled      Whether the trader can level up, which is only the case for villagers
 * @param offers       The offered trades
 */
public record TradeOffers(byte containerId, int tier, boolean leveled, List<Offer> offers) {

    /**
     * @param netId           The id used to tell the server which trade the player picked
     * @param costA           The first item the player has to pay
     * @param costB           The second item the player has to pay, may be null
     * @param result          The item the player receives
     * @param bedrockCostA    The first cost as the server sent it, needed to fill the Bedrock trade slots
     * @param bedrockCostB    The second cost as the server sent it, may be null
     * @param bedrockResult   The result as the server sent it
     * @param uses            How often this trade was already used
     * @param maxUses         How often this trade can be used before it locks
     * @param experience      The experience the villager gains from this trade
     * @param priceMultiplier How strongly the villager's reputation affects the price
     * @param demand          The current demand, which increases the price
     */
    public record Offer(int netId, Item costA, Item costB, Item result, BedrockItem bedrockCostA, BedrockItem bedrockCostB, BedrockItem bedrockResult,
                        int uses, int maxUses, int experience, float priceMultiplier, int demand) {
    }

}
