/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.opennbt.stringified.SNBT;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.ItemEntry;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.array.ArrayType;
import net.raphimc.viabedrock.protocol.types.item.BedrockItemType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class ItemRewriter extends StoredObject {

    private static final Map<String, RewriterCreator> ITEM_REWRITERS = new HashMap<>();

    private final BiMap<String, Integer> items;
    private final Type<BedrockItem> itemType;
    private final Type<BedrockItem[]> itemArrayType;

    static {
        // TODO: Add missing item rewriters
        ITEM_REWRITERS.put(null, Rewriter::new);
    }

    public ItemRewriter(final UserConnection user, final ItemEntry[] itemEntries) {
        super(user);

        this.items = HashBiMap.create(BedrockProtocol.MAPPINGS.getBedrockItems());
        for (ItemEntry itemEntry : itemEntries) {
            String namespace = "minecraft";
            String identifier = itemEntry.identifier();
            if (identifier.contains(":")) {
                final String[] namespaceAndIdentifier = identifier.split(":", 2);
                namespace = namespaceAndIdentifier[0];
                identifier = namespaceAndIdentifier[1].toLowerCase(Locale.ROOT);
            } else {
                identifier = identifier.toLowerCase(Locale.ROOT);
            }

            this.items.inverse().remove(itemEntry.id());
            this.items.put(namespace + ":" + identifier, itemEntry.id());
        }
        this.itemType = new BedrockItemType(this.items.getOrDefault("minecraft:shield", -1));
        this.itemArrayType = new ArrayType<>(this.itemType, BedrockTypes.UNSIGNED_VAR_INT);
    }

    public Item javaItem(final BedrockItem bedrockItem) {
        if (bedrockItem == null) return null;

        String identifier = this.items.inverse().get(bedrockItem.identifier());
        if (identifier == null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing item identifier for id: " + bedrockItem.identifier());
            return null;
        }

        final Rewriter rewriter;
        final Map<BlockState, Rewriter> blockItemRewriter = BedrockProtocol.MAPPINGS.getBedrockToJavaBlockItems().get(identifier);
        if (blockItemRewriter != null) {
            BlockState blockState = this.getUser().get(BlockStateRewriter.class).blockState(bedrockItem.blockRuntimeId());
            if (!blockItemRewriter.containsKey(blockState)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + bedrockItem.blockRuntimeId() + " for item: " + identifier);
                blockState = blockItemRewriter.keySet().iterator().next();
            }
            rewriter = blockItemRewriter.get(blockState);
        } else {
            final int meta = bedrockItem.data() & 0xFFFF;
            final String newIdentifier = BedrockProtocol.MAPPINGS.getBedrockItemUpgrader().upgradeMetaItem(identifier, meta);
            if (newIdentifier != null) {
                identifier = newIdentifier;
            }
            final Map<Integer, Rewriter> metaItemRewriter = BedrockProtocol.MAPPINGS.getBedrockToJavaMetaItems().get(identifier);
            if (metaItemRewriter != null) {
                if (!metaItemRewriter.containsKey(meta)) {
                    if (metaItemRewriter.size() != 1 || meta != 0) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing meta: " + meta + " for item: " + identifier);
                    }
                    rewriter = metaItemRewriter.get(null);
                } else {
                    rewriter = metaItemRewriter.get(meta);
                }
            } else {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing bedrock -> java item mapping for " + identifier);

                final ATextComponent nameComponent = TextUtil.stringToComponent("§cMissing item: " + identifier);
                nameComponent.getStyle().setItalic(false);

                final CompoundTag tag = new CompoundTag();
                final CompoundTag displayTag = new CompoundTag();
                displayTag.put("Name", new StringTag(TextUtil.componentToJson(nameComponent)));
                tag.put("display", displayTag);

                return new DataItem(BedrockProtocol.MAPPINGS.getJavaItems().get("minecraft:paper"), (byte) MathUtil.clamp(bedrockItem.amount(), 0, 127), (short) 0, tag);
            }
        }

        return rewriter.toJava(this.getUser(), bedrockItem);
    }

    public CompoundTag javaItem(final CompoundTag bedrockTag) {
        return new CompoundTag(); // TODO: Support converting nbt items
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

    public Type<BedrockItem> itemType() {
        return this.itemType;
    }

    public Type<BedrockItem[]> itemArrayType() {
        return this.itemArrayType;
    }

    public static class Rewriter {

        private final String identifier;
        private final String displayName;
        private final CompoundTag overrideTag;

        private Rewriter(final String identifier, final String displayName, final CompoundTag overrideTag) {
            this.identifier = identifier;
            this.displayName = displayName;
            this.overrideTag = overrideTag;
        }

        public static Rewriter fromJson(final String bedrockIdentifier, final JsonObject javaMapping) {
            final String javaIdentifier = javaMapping.get("java_id").getAsString();
            if (!BedrockProtocol.MAPPINGS.getJavaItems().containsKey(javaIdentifier)) {
                throw new RuntimeException("Unknown java item: " + javaIdentifier);
            }

            final String javaDisplayName = javaMapping.has("java_display_name") ? javaMapping.get("java_display_name").getAsString() : null;
            CompoundTag javaTag = null;
            try {
                if (javaMapping.has("java_tag")) {
                    javaTag = SNBT.deserializeCompoundTag(javaMapping.get("java_tag").getAsString());
                }
            } catch (Throwable e) {
                throw new RuntimeException("Failed to parse java tag for " + javaIdentifier, e);
            }
            final String tag = BedrockProtocol.MAPPINGS.getBedrockItemTags().get(bedrockIdentifier);

            return ITEM_REWRITERS.get(tag).create(javaIdentifier, javaDisplayName, javaTag);
        }

        public Item toJava(final UserConnection user, final BedrockItem bedrockItem) {
            final int javaId = BedrockProtocol.MAPPINGS.getJavaItems().get(this.identifier);

            final CompoundTag javaTag = new CompoundTag();
            if (this.overrideTag != null) {
                javaTag.setValue(this.overrideTag.clone().getValue());
            }
            if (this.displayName != null) {
                final String newName = "Bedrock " + this.displayName;
                final ATextComponent nameComponent = TextUtil.stringToComponent(newName);
                nameComponent.getStyle().setItalic(false);

                final CompoundTag displayTag = new CompoundTag();
                displayTag.put("Name", new StringTag(TextUtil.componentToJson(nameComponent)));
                javaTag.put("display", displayTag);
            }

            return new DataItem(javaId, (byte) MathUtil.clamp(bedrockItem.amount(), 0, 127), (short) 0, javaTag.isEmpty() ? null : javaTag);
        }

        public String identifier() {
            return this.identifier;
        }

    }

    private interface RewriterCreator {

        Rewriter create(final String identifier, final String displayName, final CompoundTag overrideTag);

    }

}
