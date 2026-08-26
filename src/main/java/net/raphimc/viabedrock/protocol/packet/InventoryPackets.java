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
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
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
import net.raphimc.viabedrock.api.inventory.ContainerClickResolver;
import net.raphimc.viabedrock.api.inventory.InventoryOperation;
import net.raphimc.viabedrock.api.model.container.BlockMenuContainer;
import net.raphimc.viabedrock.api.model.container.ChestContainer;
import net.raphimc.viabedrock.api.model.container.CrafterContainer;
import net.raphimc.viabedrock.api.model.container.UiContainer;
import net.raphimc.viabedrock.api.model.container.UiContainerLayout;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.ContainerSlot;
import net.raphimc.viabedrock.api.model.container.MenuContainer;
import net.raphimc.viabedrock.api.model.container.player.HudContainer;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.RegistryUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.ContainerInput;
import net.raphimc.viabedrock.protocol.data.generated.java.RegistryKeys;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.EquipmentSlot;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.EnchantOption;
import net.raphimc.viabedrock.protocol.model.CraftingRecipe;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.model.TradeOffers;
import net.raphimc.viabedrock.protocol.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.protocol.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackRequestAction;
import net.raphimc.viabedrock.protocol.model.inventory.ItemStackResponse;
import net.raphimc.viabedrock.protocol.data.generated.bedrock.CustomBlockTags;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.InventoryTransactionRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class InventoryPackets {

    private static final int DIALOG_BUTTON_WIDTH = 200;
    private static final int DIALOG_FAKE_BUTTON_WIDTH = 300;
    /**
     * The largest Java screen has 90 slots, so a click can never legitimately change more slots than that.
     */
    private static final int MAX_CHANGED_SLOTS = 128;
    /**
     * Bedrock sends the brewing stand slots as ingredient, bottle 1-3, fuel, while Java expects bottle 1-3, ingredient, fuel.
     */
    private static final ContainerEnumName[] BREWING_STAND_SLOT_NAMES = {ContainerEnumName.BrewingStandInputContainer, ContainerEnumName.BrewingStandResultContainer, ContainerEnumName.BrewingStandResultContainer, ContainerEnumName.BrewingStandResultContainer, ContainerEnumName.BrewingStandFuelContainer};
    private static final int[] BREWING_STAND_JAVA_SLOTS = {3, 0, 1, 2, 4};
    private static final String DIALOG_FAKE_BUTTON_TEXT = "This is not actually a button, but has to be one because dialogs don't support adding text only elements. Clicking it has the same effect as closing the dialog.";

    /**
     * Resends the contents of every container which is part of the currently open Java screen. Used to undo predictions
     * the Java client made for interactions which couldn't be translated.
     */
    private static void resyncOpenScreen(final UserConnection user) {
        final InventoryTracker inventoryTracker = user.get(InventoryTracker.class);
        final MenuContainer menuContainer = inventoryTracker.getCurrentMenuContainer();
        // Menus contain the player inventory, so resending them covers every slot of the screen
        PacketFactory.sendJavaContainerSetContent(user, menuContainer != null ? menuContainer : inventoryTracker.getInventoryContainer());
    }

    /**
     * Creates the container for the screen the server asked to open.
     *
     * @param blockTag    The block entity tag of the block the container belongs to, if there is one
     * @param doubleChest Whether the chest at the given position is paired with a second one
     * @return The container or null if this screen has no Java Edition equivalent
     */
    private static Container createContainer(final UserConnection user, final byte containerId, final ContainerType type, final BlockPosition position, final String blockTag, final boolean doubleChest) {
        return switch (type) {
            case CONTAINER -> new ChestContainer(user, containerId, chestTitle(blockTag, doubleChest), position, doubleChest ? ChestContainer.DOUBLE_CHEST_SIZE : ChestContainer.SINGLE_CHEST_SIZE);
            // Minecarts and boats carry their inventory around, so there is no block to check
            case MINECART_CHEST, CHEST_BOAT -> new ChestContainer(user, containerId, type, new TranslationComponent("container.chest"), ChestContainer.SINGLE_CHEST_SIZE);
            case DISPENSER -> new BlockMenuContainer(user, containerId, type, new TranslationComponent("container.dispenser"), position, 9, ContainerEnumName.LevelEntityContainer, CustomBlockTags.DISPENSER);
            case DROPPER -> new BlockMenuContainer(user, containerId, type, new TranslationComponent("container.dropper"), position, 9, ContainerEnumName.LevelEntityContainer, CustomBlockTags.DROPPER);
            case HOPPER -> new BlockMenuContainer(user, containerId, type, new TranslationComponent("container.hopper"), position, 5, ContainerEnumName.LevelEntityContainer, CustomBlockTags.HOPPER);
            case MINECART_HOPPER -> new BlockMenuContainer(user, containerId, type, new TranslationComponent("container.hopper"), null, 5, ContainerEnumName.LevelEntityContainer);
            case FURNACE -> createFurnaceContainer(user, containerId, type, "container.furnace", position, ContainerEnumName.FurnaceIngredientContainer, CustomBlockTags.FURNACE);
            case BLAST_FURNACE -> createFurnaceContainer(user, containerId, type, "container.blast_furnace", position, ContainerEnumName.BlastFurnaceIngredientContainer, CustomBlockTags.BLAST_FURNACE);
            case SMOKER -> createFurnaceContainer(user, containerId, type, "container.smoker", position, ContainerEnumName.SmokerIngredientContainer, CustomBlockTags.SMOKER);
            case BREWING_STAND -> new BlockMenuContainer(user, containerId, type, new TranslationComponent("container.brewing"), position, BREWING_STAND_SLOT_NAMES, BREWING_STAND_JAVA_SLOTS, CustomBlockTags.BREWING_STAND);
            case LECTERN -> new BlockMenuContainer(user, containerId, type, new TranslationComponent("container.lectern"), position, 1, ContainerEnumName.LevelEntityContainer, CustomBlockTags.LECTERN);
            case CRAFTER -> new CrafterContainer(user, containerId, new TranslationComponent("container.crafter"), position);
            case WORKBENCH -> new UiContainer(user, containerId, type, new TranslationComponent("container.crafting"), position, UiContainerLayout.WORKBENCH);
            case ANVIL -> new UiContainer(user, containerId, type, new TranslationComponent("container.repair"), position, UiContainerLayout.ANVIL);
            case ENCHANTMENT -> new UiContainer(user, containerId, type, new TranslationComponent("container.enchant"), position, UiContainerLayout.ENCHANTMENT, CustomBlockTags.ENCHANTING_TABLE);
            case LOOM -> new UiContainer(user, containerId, type, new TranslationComponent("container.loom"), position, UiContainerLayout.LOOM);
            case STONECUTTER -> new UiContainer(user, containerId, type, new TranslationComponent("container.stonecutter"), position, UiContainerLayout.STONECUTTER);
            case GRINDSTONE -> new UiContainer(user, containerId, type, new TranslationComponent("container.grindstone_title"), position, UiContainerLayout.GRINDSTONE);
            case CARTOGRAPHY -> new UiContainer(user, containerId, type, new TranslationComponent("container.cartography_table"), position, UiContainerLayout.CARTOGRAPHY);
            case SMITHING_TABLE -> new UiContainer(user, containerId, type, new TranslationComponent("container.upgrade"), position, UiContainerLayout.SMITHING_TABLE);
            case BEACON -> new UiContainer(user, containerId, type, new TranslationComponent("container.beacon"), position, UiContainerLayout.BEACON, CustomBlockTags.BEACON);
            // Trading is bound to the villager, not to a block
            case TRADE -> new UiContainer(user, containerId, type, new TranslationComponent("entity.minecraft.villager"), null, UiContainerLayout.TRADE);
            default -> null;
        };
    }

    private static Container createFurnaceContainer(final UserConnection user, final byte containerId, final ContainerType type, final String titleKey, final BlockPosition position, final ContainerEnumName ingredientContainer, final String blockTag) {
        final ContainerEnumName[] slotNames = {ingredientContainer, ContainerEnumName.FurnaceFuelContainer, ContainerEnumName.FurnaceResultContainer};
        return new BlockMenuContainer(user, containerId, type, new TranslationComponent(titleKey), position, slotNames, null, blockTag);
    }

    private static TextComponent chestTitle(final String blockTag, final boolean doubleChest) {
        if (CustomBlockTags.BARREL.equals(blockTag)) {
            return new TranslationComponent("container.barrel");
        } else if (CustomBlockTags.ENDER_CHEST.equals(blockTag)) {
            return new TranslationComponent("container.enderchest");
        } else if (CustomBlockTags.SHULKER_BOX.equals(blockTag)) {
            return new TranslationComponent("container.shulkerBox");
        } else {
            return new TranslationComponent(doubleChest ? "container.chestDouble" : "container.chest");
        }
    }

    /**
     * Sends the request for taking an item out of a result slot, if that is what the player did.
     *
     * <p>Bedrock Edition doesn't let the client just take the item, it has to name what it wants to craft first.</p>
     *
     * @return Whether the click was handled as a crafting request
     */
    private static boolean sendCraftRequest(final UserConnection user, final ContainerClickResolver.Result result) {
        if (!user.get(GameSessionStorage.class).isInventoryServerAuthoritative()) {
            // Client authoritative servers apply whatever the client tells them, so no recipe has to be named
            return false;
        }
        if (!(user.get(InventoryTracker.class).getCurrentMenuContainer() instanceof UiContainer uiContainer)) {
            return false;
        }
        final UiContainerLayout layout = uiContainer.layout();
        if (layout.resultSlot() == -1) {
            return false;
        }

        final ContainerSlot resultSlot = new ContainerSlot(uiContainer, layout.resultSlot());
        final BedrockItem craftedItem = resultSlot.getItem();
        int craftedAmount = 0;
        for (InventoryOperation operation : result.operations()) {
            if (operation instanceof InventoryOperation.Transfer transfer && transfer.source().equals(resultSlot)) {
                craftedAmount += transfer.count();
            } else if (operation instanceof InventoryOperation.Drop drop && drop.source().equals(resultSlot)) {
                craftedAmount += drop.count();
            }
        }
        if (craftedAmount == 0 || craftedItem.isEmpty()) {
            return false;
        }

        final int repetitions = Math.max(1, Math.min(255, craftedAmount / Math.max(1, craftedItem.amount())));
        final InventoryTracker inventoryTracker = user.get(InventoryTracker.class);
        final String renameText = layout.craftType() == UiContainerLayout.CraftType.OPTIONAL ? inventoryTracker.getItemRenameText() : null;
        final List<ItemStackRequestAction> craftActions = createCraftActions(user, uiContainer, craftedItem, repetitions, renameText != null ? 0 : -1);
        if (craftActions == null) {
            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Don't know how to craft the result of " + uiContainer.type());
            resyncOpenScreen(user);
            return true;
        }

        final ItemStackRequestTracker itemStackRequestTracker = user.get(ItemStackRequestTracker.class);
        final Set<Container> affectedContainers = new HashSet<>();
        final List<ItemStackRequestAction> actions = new ArrayList<>(craftActions);
        actions.addAll(itemStackRequestTracker.toActions(result.operations(), affectedContainers, true));
        affectedContainers.add(uiContainer);

        itemStackRequestTracker.sendRequest(actions, affectedContainers, renameText != null ? new String[]{renameText} : new String[0]);
        inventoryTracker.setItemRenameText(null);
        return true;
    }

    private static List<ItemStackRequestAction> createCraftActions(final UserConnection user, final UiContainer uiContainer, final BedrockItem craftedItem, final int repetitions, final int filterStringIndex) {
        final UiContainerLayout layout = uiContainer.layout();
        return switch (layout.craftType()) {
            case NONE -> null;
            case RECIPE -> {
                final CraftingRecipe recipe = findRecipe(user, uiContainer, craftedItem);
                if (recipe == null) {
                    yield null;
                }
                yield List.of(
                        new ItemStackRequestAction.CraftRecipe(recipe.netId(), repetitions),
                        new ItemStackRequestAction.CraftResultsDeprecated(List.of(craftedItem), repetitions)
                );
            }
            case OPTIONAL -> List.of(
                    // The anvil produces whatever it produces, the server already knows what that is
                    new ItemStackRequestAction.CraftRecipeOptional(0, filterStringIndex),
                    new ItemStackRequestAction.CraftResultsDeprecated(List.of(craftedItem), repetitions)
            );
            case REPAIR_AND_DISENCHANT -> List.of(
                    new ItemStackRequestAction.CraftRepairAndDisenchant(0, 0, repetitions),
                    new ItemStackRequestAction.CraftResultsDeprecated(List.of(craftedItem), repetitions)
            );
            case TRADE -> {
                final TradeOffers.Offer trade = user.get(InventoryTracker.class).getSelectedTrade();
                if (trade == null) {
                    yield null;
                }
                yield List.of(
                        new ItemStackRequestAction.CraftRecipe(trade.netId(), repetitions),
                        new ItemStackRequestAction.CraftResultsDeprecated(List.of(craftedItem), repetitions)
                );
            }
            case LOOM -> {
                final String pattern = getLoomPattern(user, uiContainer);
                if (pattern == null) {
                    yield null;
                }
                yield List.of(
                        new ItemStackRequestAction.CraftLoom(pattern, repetitions),
                        new ItemStackRequestAction.CraftResultsDeprecated(List.of(craftedItem), repetitions)
                );
            }
        };
    }

    private static CraftingRecipe findRecipe(final UserConnection user, final UiContainer uiContainer, final BedrockItem craftedItem) {
        final UiContainerLayout layout = uiContainer.layout();
        final List<BedrockItem> inputs = new ArrayList<>();
        for (int slot = 0; slot < uiContainer.size(); slot++) {
            if (slot == layout.resultSlot()) continue;
            inputs.add(uiContainer.getItem(slot));
        }

        final String blockName = switch (layout) {
            case WORKBENCH -> "crafting_table";
            case STONECUTTER -> "stonecutter";
            case SMITHING_TABLE -> "smithing_table";
            default -> null;
        };
        if (blockName == null) {
            return null;
        }

        final int width = layout == UiContainerLayout.WORKBENCH ? 3 : inputs.size();
        final CraftingRecipe recipe = user.get(RecipeTracker.class).findRecipe(blockName, craftedItem, inputs, width);
        if (recipe == null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Could not find a " + blockName + " recipe producing " + craftedItem);
        }
        return recipe;
    }

    /**
     * The loom names the banner pattern it should apply instead of a recipe. The pattern is determined by the item in
     * the pattern slot, or taken from the dye if no banner pattern item is used.
     */
    private static String getLoomPattern(final UserConnection user, final UiContainer uiContainer) {
        final BedrockItem resultItem = uiContainer.getItem(uiContainer.layout().resultSlot());
        if (resultItem.tag() == null || !(resultItem.tag().get("Patterns") instanceof ListTag<?> patterns) || patterns.isEmpty()) {
            return null;
        }
        if (patterns.get(patterns.size() - 1) instanceof CompoundTag lastPattern && lastPattern.get("Pattern") instanceof StringTag pattern) {
            return pattern.getValue();
        }
        return null;
    }

    private static TradeOffers readTradeOffers(final UserConnection user, final byte containerId, final int tier, final Tag offersTag) {
        final List<TradeOffers.Offer> offers = new ArrayList<>();
        if (offersTag instanceof CompoundTag compoundTag && compoundTag.get("Recipes") instanceof ListTag<?> recipes) {
            final ItemRewriter itemRewriter = user.get(ItemRewriter.class);
            for (Tag recipe : recipes) {
                if (!(recipe instanceof CompoundTag recipeTag)) continue;

                final BedrockItem bedrockCostA = readTradeItem(itemRewriter, recipeTag, "buyA", "buyCountA");
                final BedrockItem bedrockCostB = readTradeItem(itemRewriter, recipeTag, "buyB", "buyCountB");
                final BedrockItem bedrockResult = readTradeItem(itemRewriter, recipeTag, "sell", null);
                if (bedrockCostA == null || bedrockResult == null) continue;

                offers.add(new TradeOffers.Offer(
                        recipeTag.getInt("netId", 0),
                        itemRewriter.javaItem(bedrockCostA),
                        bedrockCostB != null ? itemRewriter.javaItem(bedrockCostB) : null,
                        itemRewriter.javaItem(bedrockResult),
                        bedrockCostA,
                        bedrockCostB,
                        bedrockResult,
                        recipeTag.getInt("uses", 0),
                        recipeTag.getInt("maxUses", Integer.MAX_VALUE),
                        recipeTag.getInt("traderExp", 0),
                        recipeTag.getFloat("priceMultiplierA", 0F),
                        recipeTag.getInt("demand", 0)
                ));
            }
        }
        // Only villagers can level up, wandering traders don't have experience requirements
        final boolean leveled = offersTag instanceof CompoundTag compoundTag && compoundTag.contains("TierExpRequirements");
        return new TradeOffers(containerId, tier, leveled, offers);
    }

    private static BedrockItem readTradeItem(final ItemRewriter itemRewriter, final CompoundTag recipeTag, final String itemKey, final String countKey) {
        if (!(recipeTag.get(itemKey) instanceof CompoundTag itemTag)) {
            return null;
        }
        final BedrockItem item = itemRewriter.bedrockItem(itemTag);
        if (item == null) {
            return null;
        }
        // Bedrock stores the amount the trade costs separately from the item itself
        if (countKey != null && recipeTag.get(countKey) instanceof NumberTag countTag && countTag.asInt() > 0) {
            item.setAmount(countTag.asInt());
        }
        return item;
    }

    private static void sendJavaMerchantOffers(final UserConnection user, final Container container, final TradeOffers tradeOffers) {
        final PacketWrapper merchantOffers = PacketWrapper.create(ClientboundPackets26_1.MERCHANT_OFFERS, user);
        merchantOffers.write(Types.VAR_INT, (int) container.javaContainerId()); // container id
        merchantOffers.write(Types.VAR_INT, tradeOffers.offers().size()); // offer count
        for (TradeOffers.Offer offer : tradeOffers.offers()) {
            merchantOffers.write(VersionedTypes.V26_2.itemCost, offer.costA()); // first cost
            merchantOffers.write(VersionedTypes.V26_2.item, offer.result()); // result
            merchantOffers.write(VersionedTypes.V26_2.optionalItemCost, offer.costB()); // second cost
            merchantOffers.write(Types.BOOLEAN, offer.uses() >= offer.maxUses()); // out of stock
            merchantOffers.write(Types.INT, offer.uses()); // uses
            merchantOffers.write(Types.INT, offer.maxUses()); // max uses
            merchantOffers.write(Types.INT, offer.experience()); // experience
            merchantOffers.write(Types.INT, 0); // special price
            merchantOffers.write(Types.FLOAT, offer.priceMultiplier()); // price multiplier
            merchantOffers.write(Types.INT, offer.demand()); // demand
        }
        // Bedrock counts the levels from zero, Java from one
        merchantOffers.write(Types.VAR_INT, tradeOffers.leveled() ? tradeOffers.tier() + 1 : 0); // villager level
        merchantOffers.write(Types.VAR_INT, 0); // villager experience
        merchantOffers.write(Types.BOOLEAN, tradeOffers.leveled()); // show progress
        merchantOffers.write(Types.BOOLEAN, true); // can restock
        merchantOffers.send(BedrockProtocol.class);
    }

    private static void sendJavaEnchantOptions(final UserConnection user, final Container container, final EnchantOption[] enchantOptions) {
        final CompoundTag enchantmentRegistry = (CompoundTag) BedrockProtocol.MAPPINGS.getJavaRegistries().get(RegistryKeys.ENCHANTMENT);
        for (int i = 0; i < 3; i++) {
            final EnchantOption enchantOption = i < enchantOptions.length ? enchantOptions[i] : null;
            int javaEnchantment = -1;
            int level = 0;
            if (enchantOption != null) {
                for (Int2IntMap.Entry entry : enchantOption.enchantments().int2IntEntrySet()) {
                    final Enchant_Type bedrockEnchantment = Enchant_Type.getByValue(entry.getIntKey());
                    final String javaIdentifier = bedrockEnchantment != null ? BedrockProtocol.MAPPINGS.getBedrockToJavaEnchantments().get(bedrockEnchantment) : null;
                    if (javaIdentifier == null) continue;
                    final int registryIndex = RegistryUtil.getRegistryIndex(enchantmentRegistry, javaIdentifier);
                    if (registryIndex == -1) continue;

                    // Java can only show a single enchantment per option
                    javaEnchantment = registryIndex;
                    level = entry.getIntValue();
                    break;
                }
            }

            sendJavaContainerSetData(user, container, i, enchantOption != null ? enchantOption.cost() : 0); // level requirement
            sendJavaContainerSetData(user, container, 4 + i, javaEnchantment); // enchantment
            sendJavaContainerSetData(user, container, 7 + i, level); // enchantment level
        }
    }

    private static void sendJavaContainerSetData(final UserConnection user, final Container container, final int property, final int value) {
        final PacketWrapper containerSetData = PacketWrapper.create(ClientboundPackets26_1.CONTAINER_SET_DATA, user);
        containerSetData.write(Types.VAR_INT, (int) container.javaContainerId()); // container id
        containerSetData.write(Types.SHORT, (short) property); // property
        containerSetData.write(Types.SHORT, (short) value); // value
        containerSetData.send(BedrockProtocol.class);
    }

    /**
     * Reads the creative items the server offers.
     *
     * @throws RuntimeException If the packet doesn't have the expected layout
     */
    private static void readCreativeContent(final PacketWrapper wrapper) {
        final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);

        final int groupCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // group count
        for (int i = 0; i < groupCount; i++) {
            wrapper.read(BedrockTypes.INT_LE); // category id
            wrapper.read(BedrockTypes.STRING); // category name
            wrapper.read(itemRewriter.itemTypeWithoutNetId()); // icon
        }

        final int itemCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // item count
        final Map<Integer, BedrockItem> creativeItems = new LinkedHashMap<>();
        for (int i = 0; i < itemCount; i++) {
            final int netId = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // creative net id
            final BedrockItem item = wrapper.read(itemRewriter.itemTypeWithoutNetId()); // item
            wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // group id
            creativeItems.put(netId, item);
        }
        wrapper.user().get(CreativeContentTracker.class).setCreativeItems(creativeItems);
    }

    public static void register(final BedrockProtocol protocol) {
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

            if (type == ContainerType.INVENTORY) {
                inventoryTracker.setCurrentContainer(new InventoryContainer(wrapper.user(), containerId, position, inventoryTracker.getInventoryContainer()));
                wrapper.cancel();
                return;
            }

            final String blockTag = blockStateRewriter.tag(chunkTracker.getBlockState(position));
            final BedrockBlockEntity blockEntity = chunkTracker.getBlockEntity(position);
            final boolean doubleChest = type == ContainerType.CONTAINER && blockEntity != null && ChestContainer.getPairedChestPosition(blockEntity) != null;

            final Container container = createContainer(wrapper.user(), containerId, type, position, blockTag, doubleChest);
            if (container == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to open unimplemented container: " + type);
                PacketFactory.sendBedrockContainerClose(wrapper.user(), containerId, ContainerType.NONE);
                wrapper.cancel();
                return;
            }

            TextComponent title = container.title();
            if (blockEntity != null && blockEntity.tag().get("CustomName") instanceof StringTag customNameTag) {
                title = TextUtil.stringToTextComponent(wrapper.user().get(ResourcePackStorage.class).getTexts().translate(customNameTag.getValue()));
            }
            inventoryTracker.setCurrentContainer(container);

            wrapper.write(Types.VAR_INT, (int) containerId); // container id
            wrapper.write(Types.VAR_INT, container.javaMenuType()); // type
            wrapper.write(Types.TAG, TextUtil.textComponentToNbt(title)); // title
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();

            // These have to be sent after the screen was opened, because they refer to the menu the Java client just created.
            // The server only sends the container's own contents, so the player inventory part of the menu has to be
            // filled in here or it would stay empty until something changes.
            PacketFactory.sendJavaContainerSetContent(wrapper.user(), container);
            switch (type) {
                case FURNACE, BLAST_FURNACE, SMOKER ->
                        // Bedrock doesn't tell the client how long the current recipe takes, so the vanilla durations are used
                        sendJavaContainerSetData(wrapper.user(), container, 3, type == ContainerType.FURNACE ? 200 : 100);
                case TRADE -> {
                    if (inventoryTracker.getTradeOffers() != null) {
                        sendJavaMerchantOffers(wrapper.user(), container, inventoryTracker.getTradeOffers());
                    }
                }
                case ENCHANTMENT -> sendJavaEnchantOptions(wrapper.user(), container, inventoryTracker.getEnchantOptions());
                default -> {
                }
            }
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
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_TRADE, null, wrapper -> {
            wrapper.cancel();
            final byte containerId = wrapper.read(Types.BYTE); // container id
            wrapper.read(Types.BYTE); // container type
            wrapper.read(BedrockTypes.VAR_INT); // slot count
            final int tier = wrapper.read(BedrockTypes.VAR_INT); // trade tier
            wrapper.read(BedrockTypes.VAR_LONG); // trader entity unique id
            wrapper.read(BedrockTypes.VAR_LONG); // player entity unique id
            wrapper.read(BedrockTypes.STRING); // display name
            wrapper.read(Types.BOOLEAN); // new trading ui
            wrapper.read(Types.BOOLEAN); // economic trades
            final Tag offersTag = wrapper.read(BedrockTypes.NETWORK_TAG); // offers

            final TradeOffers tradeOffers;
            try {
                tradeOffers = readTradeOffers(wrapper.user(), containerId, tier, offersTag);
            } catch (Throwable e) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to read the villager trades", e);
                return;
            }
            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            inventoryTracker.setTradeOffers(tradeOffers);

            // The server can send the trades before or after opening the screen
            final Container container = inventoryTracker.getCurrentContainer();
            if (container != null && container.type() == ContainerType.TRADE) {
                sendJavaMerchantOffers(wrapper.user(), container, tradeOffers);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CONTAINER_SET_DATA, ClientboundPackets26_1.CONTAINER_SET_DATA, wrapper -> {
            final byte containerId = wrapper.read(Types.BYTE); // container id
            final int property = wrapper.read(BedrockTypes.VAR_INT); // property
            final int value = wrapper.read(BedrockTypes.VAR_INT); // value

            final Container container = wrapper.user().get(InventoryTracker.class).getContainerClientbound(containerId, FullContainerName.EMPTY, BedrockItem.empty());
            if (container == null) {
                wrapper.cancel();
                return;
            }

            final int javaProperty = switch (container.type()) {
                case FURNACE, BLAST_FURNACE, SMOKER -> switch (property) {
                    case 0 -> 2; // Smelt progress
                    case 1 -> 0; // Remaining fuel time
                    case 2 -> 1; // Max fuel time
                    default -> -1; // Stored xp and fuel aux have no Java equivalent
                };
                case BREWING_STAND -> switch (property) {
                    case 0 -> 0; // Brew time
                    case 1 -> 1; // Fuel amount
                    default -> -1; // Java derives the fuel bar from a fixed maximum
                };
                default -> -1;
            };
            if (javaProperty == -1) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, (int) container.javaContainerId()); // container id
            wrapper.write(Types.SHORT, (short) javaProperty); // property
            wrapper.write(Types.SHORT, (short) value); // value
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_ENCHANT_OPTIONS, null, wrapper -> {
            wrapper.cancel();
            final EnchantOption[] enchantOptions;
            try {
                enchantOptions = wrapper.read(BedrockTypes.ENCHANT_OPTION_ARRAY); // options
            } catch (Throwable e) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to read the enchantment options", e);
                return;
            }

            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            inventoryTracker.setEnchantOptions(enchantOptions);
            final Container container = inventoryTracker.getCurrentContainer();
            if (container != null && container.type() == ContainerType.ENCHANTMENT) {
                sendJavaEnchantOptions(wrapper.user(), container, enchantOptions);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.INVENTORY_TRANSACTION, null, wrapper -> {
            // Servers which use client authoritative inventories send this back to correct the client's prediction
            wrapper.cancel();
            final BedrockInventoryTransaction inventoryTransaction = wrapper.read(wrapper.user().get(InventoryTransactionRewriter.class).getInventoryTransactionType()); // transaction
            if (inventoryTransaction.actions() == null || inventoryTransaction.actions().isEmpty()) {
                return;
            }

            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final Set<Container> changedContainers = new LinkedHashSet<>();
            for (InventoryActionData actionData : inventoryTransaction.actions()) {
                if (actionData.source().type() != InventorySourceType.Container_Inventory) continue;

                final Container container = inventoryTracker.getContainerClientbound((byte) actionData.source().containerId(), FullContainerName.EMPTY, BedrockItem.empty());
                if (container == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received inventory action for unknown container id: " + actionData.source().containerId());
                    continue;
                }
                if (container.setItem(actionData.slot(), actionData.toItem())) {
                    changedContainers.add(container);
                }
            }
            for (Container container : changedContainers) {
                PacketFactory.sendJavaContainerSetContent(wrapper.user(), container);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CRAFTING_DATA, null, wrapper -> {
            wrapper.cancel();
            try {
                wrapper.user().get(RecipeTracker.class).readRecipes(wrapper);
            } catch (Throwable e) {
                // Losing the recipes only means that result slots can't be used, so this shouldn't kill the connection
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to read the crafting data", e);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CREATIVE_CONTENT, null, wrapper -> {
            wrapper.cancel();
            try {
                readCreativeContent(wrapper);
            } catch (Throwable e) {
                // Without the creative items the player just can't create items in creative mode, which is a lot
                // better than dropping the connection over it
                wrapper.user().get(CreativeContentTracker.class).clear();
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to read the creative content", e);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.ITEM_STACK_RESPONSE, null, wrapper -> {
            wrapper.cancel();
            final ItemStackResponse[] responses;
            try {
                responses = wrapper.read(BedrockTypes.ITEM_STACK_RESPONSE_ARRAY); // responses
            } catch (Throwable e) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to read an item stack response", e);
                resyncOpenScreen(wrapper.user());
                return;
            }

            final ItemStackRequestTracker itemStackRequestTracker = wrapper.user().get(ItemStackRequestTracker.class);
            for (ItemStackResponse response : responses) {
                itemStackRequestTracker.handleResponse(response);
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
            final int containerId = wrapper.read(Types.VAR_INT); // container id
            wrapper.read(Types.VAR_INT); // revision
            final short slot = wrapper.read(Types.SHORT); // slot
            wrapper.read(Types.BYTE); // button
            final ContainerInput action = ContainerInput.values()[wrapper.read(Types.VAR_INT)]; // action
            final int changedSlotCount = wrapper.read(Types.VAR_INT); // changed slot count
            if (changedSlotCount < 0 || changedSlotCount > MAX_CHANGED_SLOTS) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received container click with an invalid amount of changed slots: " + changedSlotCount);
                resyncOpenScreen(wrapper.user());
                return;
            }
            final Map<Integer, HashedItem> changedSlots = new LinkedHashMap<>();
            for (int i = 0; i < changedSlotCount; i++) {
                final int changedSlot = wrapper.read(Types.SHORT); // slot
                changedSlots.put(changedSlot, wrapper.read(Types.HASHED_ITEM)); // item
            }
            final HashedItem carriedItem = wrapper.read(Types.HASHED_ITEM); // carried item

            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
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

            final ContainerSlot clickedSlot = slot >= 0 ? inventoryTracker.resolveJavaSlot(slot) : null;
            final ContainerClickResolver.Result result = new ContainerClickResolver(wrapper.user()).resolve(changedSlots, carriedItem, clickedSlot, slot < 0 || action == ContainerInput.THROW);
            if (result == null) { // The click couldn't be translated, so the Java client's prediction has to be undone
                resyncOpenScreen(wrapper.user());
                return;
            }
            if (result.operations().isEmpty()) {
                return;
            }

            // The change refers to the items by the state they currently have, so it has to be sent before the
            // predicted result is applied.
            if (!sendCraftRequest(wrapper.user(), result)) {
                PacketFactory.sendBedrockInventoryChange(wrapper.user(), result.newItems(), result.operations());
            }
            for (Map.Entry<ContainerSlot, BedrockItem> entry : result.newItems().entrySet()) {
                entry.getKey().setItem(entry.getValue());
            }
        });
        protocol.registerServerbound(ServerboundPackets26_1.SELECT_TRADE, null, wrapper -> {
            wrapper.cancel();
            final int tradeIndex = wrapper.read(Types.VAR_INT); // trade index

            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final TradeOffers tradeOffers = inventoryTracker.getTradeOffers();
            final Container container = inventoryTracker.getCurrentContainer();
            if (tradeOffers == null || container == null || container.type() != ContainerType.TRADE) {
                return;
            }
            if (tradeIndex < 0 || tradeIndex >= tradeOffers.offers().size()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Java client selected a trade which the server didn't offer: " + tradeIndex);
                return;
            }

            // The Bedrock client fills the trade slots itself and only tells the server about the trade once the
            // player takes the result, so the same has to be done here.
            final TradeOffers.Offer trade = tradeOffers.offers().get(tradeIndex);
            inventoryTracker.setSelectedTrade(trade);
            container.setItem(0, trade.bedrockCostA());
            container.setItem(1, trade.bedrockCostB() != null ? trade.bedrockCostB() : BedrockItem.empty());
            container.setItem(2, trade.bedrockResult());
            PacketFactory.sendJavaContainerSetContent(wrapper.user(), container);
        });
        protocol.registerServerbound(ServerboundPackets26_1.RENAME_ITEM, null, wrapper -> {
            wrapper.cancel();
            // Bedrock doesn't send the name on its own, it is attached to the request which takes the renamed item
            wrapper.user().get(InventoryTracker.class).setItemRenameText(wrapper.read(Types.STRING)); // name
        });
        protocol.registerServerbound(ServerboundPackets26_1.CONTAINER_BUTTON_CLICK, null, wrapper -> {
            wrapper.cancel();
            final int containerId = wrapper.read(Types.VAR_INT); // container id
            final int buttonId = wrapper.read(Types.VAR_INT); // button id

            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            final Container container = inventoryTracker.getContainerServerbound((byte) containerId);
            if (container == null || container.type() != ContainerType.ENCHANTMENT) {
                // Only the enchanting table uses buttons on Bedrock, every other Java menu with buttons (like the
                // stonecutter and the loom) selects its recipe through the result slot instead.
                return;
            }

            final EnchantOption[] enchantOptions = inventoryTracker.getEnchantOptions();
            if (buttonId < 0 || buttonId >= enchantOptions.length) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Java client selected an enchantment which the server didn't offer: " + buttonId);
                return;
            }

            // The Bedrock client requests the enchantment by the net id the server assigned to it
            wrapper.user().get(ItemStackRequestTracker.class).sendRequest(
                    List.of(new ItemStackRequestAction.CraftRecipe(enchantOptions[buttonId].optionId(), 1)),
                    Set.of(container, inventoryTracker.getInventoryContainer())
            );
        });
        protocol.registerServerbound(ServerboundPackets26_1.SET_CREATIVE_MODE_SLOT, null, wrapper -> {
            wrapper.cancel();
            final short slot = wrapper.read(Types.SHORT); // slot
            final Item item = wrapper.read(VersionedTypes.V26_2.lengthPrefixedItem); // item

            final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
            if (inventoryTracker.getPendingCloseContainer() != null) {
                return;
            }
            if (!wrapper.user().get(GameSessionStorage.class).isInventoryServerAuthoritative()) {
                // Client authoritative servers don't have a creative protocol, the client simply tells them the new
                // inventory content, which is not something the Java client gives us enough information for.
                resyncOpenScreen(wrapper.user());
                return;
            }

            final ContainerSlot targetSlot = slot < 0 ? null : inventoryTracker.resolveJavaSlot(slot);
            if (slot >= 0 && targetSlot == null) {
                resyncOpenScreen(wrapper.user());
                return;
            }

            final CreativeContentTracker creativeContentTracker = wrapper.user().get(CreativeContentTracker.class);
            final ItemStackRequestTracker itemStackRequestTracker = wrapper.user().get(ItemStackRequestTracker.class);
            if (item == null || item.isEmpty()) { // Deleting the content of a slot
                if (targetSlot == null || targetSlot.getItem().isEmpty()) {
                    return;
                }
                itemStackRequestTracker.sendRequest(List.of(new InventoryOperation.Destroy(targetSlot, targetSlot.getItem().amount())));
                targetSlot.setItem(BedrockItem.empty());
                return;
            }

            final int creativeNetId = creativeContentTracker.findCreativeNetId(item);
            if (creativeNetId == 0) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Could not find a creative item for " + item.identifier());
                resyncOpenScreen(wrapper.user());
                return;
            }
            final BedrockItem creativeItem = creativeContentTracker.getCreativeItem(creativeNetId).copy();
            creativeItem.setAmount(item.amount());

            // The Bedrock client first crafts the item into the creative output slot and then moves it to its
            // destination, so the same sequence of actions has to be replayed here.
            final ContainerSlot createdOutputSlot = new ContainerSlot(inventoryTracker.getHudContainer(), HudContainer.CRAFTING_RESULT);
            final List<ItemStackRequestAction> actions = new ArrayList<>();
            actions.add(new ItemStackRequestAction.CraftCreative(creativeNetId, 1));
            actions.add(new ItemStackRequestAction.CraftResultsDeprecated(List.of(creativeItem), 1));
            if (targetSlot == null) { // Dropping the item into the world
                actions.add(new ItemStackRequestAction.Drop(item.amount(), createdOutputSlot.requestSlotInfo(), false));
                itemStackRequestTracker.sendRequest(actions, Set.of(inventoryTracker.getInventoryContainer()));
                return;
            }

            actions.add(new ItemStackRequestAction.Place(item.amount(), createdOutputSlot.requestSlotInfo(), targetSlot.requestSlotInfo()));
            itemStackRequestTracker.sendRequest(actions, Set.of(targetSlot.container()));
            targetSlot.setItem(creativeItem);
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

}
