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
package net.raphimc.viabedrock.protocol.rewriter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.model.resourcepack.ItemDefinitions;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.BedrockMappingData;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.ItemEntry;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.CustomAttachableResourceRewriter;
import net.raphimc.viabedrock.protocol.rewriter.resourcepack.CustomItemTextureResourceRewriter;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.array.ArrayType;
import net.raphimc.viabedrock.protocol.types.item.BedrockCreativeItemType;
import net.raphimc.viabedrock.protocol.types.item.BedrockItemType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ItemRewriter extends StoredObject {

    private static final Map<String, NbtRewriter> ITEM_NBT_REWRITERS = new HashMap<>();

    private final BiMap<String, Integer> items;
    private final Set<String> componentItems;
    private final Type<BedrockItem> itemType;
    private final Type<BedrockItem[]> itemArrayType;
    private final Type<BedrockItem> creativeItemType;
    private final Type<BedrockItem[]> creativeItemArrayType;

    static {
        // TODO: Add missing item nbt rewriters
    }

    public ItemRewriter(final UserConnection user, final ItemEntry[] itemEntries) {
        super(user);

        this.items = HashBiMap.create(BedrockProtocol.MAPPINGS.getBedrockItems());
        this.componentItems = new HashSet<>();
        for (ItemEntry itemEntry : itemEntries) {
            this.items.inverse().remove(itemEntry.id());
            this.items.put(Key.namespaced(itemEntry.identifier()), itemEntry.id());
            if (itemEntry.componentBased()) {
                this.componentItems.add(Key.namespaced(itemEntry.identifier()));
            }
        }
        this.itemType = new BedrockItemType(this.items.get("minecraft:shield"), true);
        this.itemArrayType = new ArrayType<>(this.itemType, BedrockTypes.UNSIGNED_VAR_INT);
        this.creativeItemType = new BedrockCreativeItemType(this.items.get("minecraft:shield"));
        this.creativeItemArrayType = new ArrayType<>(this.creativeItemType, BedrockTypes.UNSIGNED_VAR_INT);
    }

    public Item javaItem(final BedrockItem bedrockItem) {
        if (bedrockItem.isEmpty()) return StructuredItem.empty();

        String identifier = this.items.inverse().get(bedrockItem.identifier());
        if (identifier == null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing item identifier for id: " + bedrockItem.identifier());
            return StructuredItem.empty();
        }

        final BedrockMappingData.JavaItemMapping javaItemMapping;
        final Map<BlockState, BedrockMappingData.JavaItemMapping> blockItemMappings = BedrockProtocol.MAPPINGS.getBedrockToJavaBlockItems().get(identifier);
        if (blockItemMappings != null) {
            BlockState blockState = this.user().get(BlockStateRewriter.class).blockState(bedrockItem.blockRuntimeId());
            if (!blockItemMappings.containsKey(blockState)) {
                if (bedrockItem.blockRuntimeId() != 0) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + bedrockItem.blockRuntimeId() + " for item: " + identifier);
                }
                blockState = blockItemMappings.keySet().iterator().next();
            }
            javaItemMapping = blockItemMappings.get(blockState);
        } else {
            final int meta = bedrockItem.data() & 0xFFFF;
            final String newIdentifier = BedrockProtocol.MAPPINGS.getBedrockItemUpgrader().upgradeMetaItem(identifier, meta);
            if (newIdentifier != null) {
                identifier = newIdentifier;
            }
            final Map<Integer, BedrockMappingData.JavaItemMapping> metaItemMappings = BedrockProtocol.MAPPINGS.getBedrockToJavaMetaItems().get(identifier);
            if (metaItemMappings != null) {
                if (!metaItemMappings.containsKey(meta)) {
                    if (metaItemMappings.size() != 1 || meta != 0) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing meta: " + meta + " for item: " + identifier);
                    }
                    javaItemMapping = metaItemMappings.get(null);
                } else {
                    javaItemMapping = metaItemMappings.get(meta);
                }
            } else {
                javaItemMapping = null;
            }
        }

        final Item javaItem;
        if (javaItemMapping != null) {
            final StructuredDataContainer data = ProtocolConstants.createStructuredDataContainer();
            if (javaItemMapping.overrideTag() != null) {
                // javaTag.setValue(this.overrideTag.copy().getValue());
                // TODO: Update: Fix this
            }
            if (javaItemMapping.displayName() != null) {
                data.set(StructuredDataKey.ITEM_NAME, TextUtil.stringToNbt("Bedrock " + javaItemMapping.displayName()));
            }
            javaItem = new StructuredItem(javaItemMapping.id(), bedrockItem.amount(), data);
        } else {
            final ResourcePacksStorage resourcePacksStorage = this.user().get(ResourcePacksStorage.class);
            final ItemDefinitions.ItemDefinition itemDefinition = resourcePacksStorage.getItems().get(identifier);
            final StructuredDataContainer data = ProtocolConstants.createStructuredDataContainer();

            if (itemDefinition != null) {
                if (itemDefinition.displayNameComponent() != null) {
                    data.set(StructuredDataKey.ITEM_NAME, TextUtil.stringToNbt(resourcePacksStorage.getTexts().translate(itemDefinition.displayNameComponent())));
                } else {
                    data.set(StructuredDataKey.ITEM_NAME, TextUtil.stringToNbt(resourcePacksStorage.getTexts().get("item." + Key.stripMinecraftNamespace(identifier) + ".name")));
                }
            }

            if (!resourcePacksStorage.isLoadedOnJavaClient()) {
                data.set(StructuredDataKey.LORE, new Tag[]{TextUtil.stringToNbt("§7[ViaBedrock] Custom item: " + identifier)});
                javaItem = new StructuredItem(BedrockProtocol.MAPPINGS.getJavaItems().get(Key.namespaced(CustomItemTextureResourceRewriter.ITEM)), bedrockItem.amount(), data);
            } else {
                if (resourcePacksStorage.getAttachables().attachables().containsKey(identifier)) {
                    data.set(StructuredDataKey.CUSTOM_MODEL_DATA, CustomAttachableResourceRewriter.getCustomModelData("attachable_" + identifier + "_default"));
                    javaItem = new StructuredItem(BedrockProtocol.MAPPINGS.getJavaItems().get(Key.namespaced(CustomAttachableResourceRewriter.ITEM)), bedrockItem.amount(), data);
                } else if (itemDefinition != null && itemDefinition.iconComponent() != null) {
                    data.set(StructuredDataKey.CUSTOM_MODEL_DATA, CustomItemTextureResourceRewriter.getCustomModelData(itemDefinition.iconComponent()));
                    javaItem = new StructuredItem(BedrockProtocol.MAPPINGS.getJavaItems().get(Key.namespaced(CustomItemTextureResourceRewriter.ITEM)), bedrockItem.amount(), data);
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing bedrock -> java item mapping for " + identifier);
                    data.set(StructuredDataKey.ITEM_NAME, TextUtil.stringToNbt("§cMissing item: " + identifier));
                    javaItem = new StructuredItem(BedrockProtocol.MAPPINGS.getJavaItems().get("minecraft:paper"), bedrockItem.amount(), data);
                }
            }
        }

        final CompoundTag bedrockTag = bedrockItem.tag();
        if (bedrockTag != null) {
            if (bedrockTag.get("display") instanceof CompoundTag display) {
                if (display.contains("Name")) { // Bedrock client defaults to empty string if the type is wrong
                    javaItem.dataContainer().set(StructuredDataKey.CUSTOM_NAME, TextUtil.stringToNbt(display.getString("Name", "")));
                }
            }
        }

        final String tag = BedrockProtocol.MAPPINGS.getBedrockItemTags().get(identifier);
        if (ITEM_NBT_REWRITERS.containsKey(tag)) {
            ITEM_NBT_REWRITERS.get(tag).toJava(this.user(), bedrockItem, javaItem);
        }

        return javaItem;
    }

    public CompoundTag javaItem(final CompoundTag bedrockTag) {
        final CompoundTag javaTag = new CompoundTag();
        javaTag.putString("id", "minecraft:stone");
        return javaTag; // TODO: Support converting nbt items
    }

    public Item[] javaItems(final BedrockItem[] bedrockItems) {
        final Item[] javaItems = new Item[bedrockItems.length];
        for (int i = 0; i < bedrockItems.length; i++) {
            javaItems[i] = this.javaItem(bedrockItems[i]);
        }
        return javaItems;
    }

    public BedrockItem bedrockItem(final Item javaItem) {
        throw new UnsupportedOperationException("Translating Java items to Bedrock is not yet supported");
    }

    public BedrockItem[] bedrockItems(final Item[] javaItems) {
        final BedrockItem[] bedrockItems = new BedrockItem[javaItems.length];
        for (int i = 0; i < javaItems.length; i++) {
            bedrockItems[i] = this.bedrockItem(javaItems[i]);
        }
        return bedrockItems;
    }

    public BiMap<String, Integer> getItems() {
        return this.items;
    }

    public Set<String> getComponentItems() {
        return this.componentItems;
    }

    public Type<BedrockItem> itemType() {
        return this.itemType;
    }

    public Type<BedrockItem[]> itemArrayType() {
        return this.itemArrayType;
    }

    public Type<BedrockItem> creativeItemType() {
        return this.creativeItemType;
    }

    public Type<BedrockItem[]> creativeItemArrayType() {
        return this.creativeItemArrayType;
    }

    public interface NbtRewriter {

        void toJava(final UserConnection user, final BedrockItem bedrockItem, final Item javaItem);

    }

}
