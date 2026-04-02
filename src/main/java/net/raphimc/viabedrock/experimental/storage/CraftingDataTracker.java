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
package net.raphimc.viabedrock.experimental.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.experimental.model.recipe.ItemDescriptor;
import net.raphimc.viabedrock.experimental.model.recipe.ShapelessRecipe;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;

import java.util.ArrayList;
import java.util.List;

public class CraftingDataTracker extends StoredObject {

    private List<CraftingDataStorage> craftingDataList = new ArrayList<>();

    public CraftingDataTracker(UserConnection user) {
        super(user);
    }

    public List<CraftingDataStorage> getCraftingDataList() {
        return craftingDataList;
    }

    public void updateCraftingDataList(List<CraftingDataStorage> craftingDataList) {
        this.craftingDataList = craftingDataList;
    }

    public void sendJavaUpdateRecipes(final UserConnection user) {
        //TODO: Fix up this mess
        if (craftingDataList.isEmpty()) {
            ViaBedrock.getPlatform().getLogger().warning("No crafting data available to update.");
            return;
        }
        ItemRewriter itemRewriter = user.get(ItemRewriter.class);

        PacketWrapper packet = PacketWrapper.create(ClientboundPackets26_1.UPDATE_RECIPES, user);
        packet.write(Types.VAR_INT, 0); // Property Sets (Prefixed array) TODO: What is this?
        List<CraftingDataStorage> stonecutterList = craftingDataList.stream()
                .filter(c -> c.recipe().getRecipeTag().equals("stonecutter"))
                .filter(c -> c.recipe() instanceof ShapelessRecipe)
                .toList();
        packet.write(Types.VAR_INT, stonecutterList.size()); // Number of recipes
        for (CraftingDataStorage craftingData : stonecutterList) {
            //ID Set
            packet.write(Types.VAR_INT, 2); // Type (Size + 1)
            packet.write(Types.BOOLEAN, false); // Tag Name
            packet.write(Types.BOOLEAN, true); // IDs
            packet.write(Types.VAR_INT_ARRAY_PRIMITIVE, ((ShapelessRecipe)craftingData.recipe()).getIngredients().stream()
                    .map(itemDescriptor -> ((ItemDescriptor.DefaultDescriptor)itemDescriptor).itemId())
                    .map(i -> itemRewriter.javaItem(new BedrockItem(i)).identifier())
                    .mapToInt(Integer::intValue)
                    .toArray());

            //Slot Display
            Item javaOutput = itemRewriter.javaItem(((ShapelessRecipe)craftingData.recipe()).getResults().get(0));
            packet.write(Types.VAR_INT, 2); // Type (Item)
            packet.write(Types.VAR_INT, javaOutput.identifier());
        }
        packet.send(BedrockProtocol.class);
    }

    public void sendJavaRecipeBook(final UserConnection user) {
        if (craftingDataList.isEmpty()) {
            ViaBedrock.getPlatform().getLogger().warning("No crafting data available to send Java recipe book.");
            return;
        }

        PacketWrapper packet = PacketWrapper.create(ClientboundPackets26_1.RECIPE_BOOK_ADD, user);
        packet.write(Types.VAR_INT, craftingDataList.size()); // Number of recipes
        for (CraftingDataStorage craftingData : craftingDataList) {
            packet.write(Types.VAR_INT, craftingData.networkId()); // Recipe ID
            craftingData.recipe().writeJavaRecipeData(packet, user);
            packet.write(Types.VAR_INT, craftingData.networkId()); //TODO: Group Id
            packet.write(Types.VAR_INT, 1); // TODO: Category ID
            packet.write(Types.BOOLEAN, false); // Optional Ingredients list
            packet.write(Types.BYTE, (byte) 0x00); // Recipe Flags (0x01: show notification; 0x02: highlight as new)
        }
        packet.write(Types.BOOLEAN, false); //  Replace or Add
        packet.send(BedrockProtocol.class);
    }
}
