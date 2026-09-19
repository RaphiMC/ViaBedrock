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
package net.raphimc.viabedrock.protocol.rewriter.item;

import com.viaversion.nbt.tag.*;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.util.Unit;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.RegistryUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.Enchant_Type;
import net.raphimc.viabedrock.protocol.data.generated.java.RegistryKeys;
import net.raphimc.viabedrock.protocol.model.BedrockItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Bidirectional item data (NBT <-> component) translation, excluding map items (handled by the experimental map tracker).
 */
public class ItemDataRewriter {

    private static final String VIA_BEDROCK_MARKER_PREFIX = "§7[ViaBedrock]";

    // Bedrock NBT -> Java components
    public static void toJava(final UserConnection user, final BedrockItem bedrockItem, final CompoundTag bedrockTag, final Item javaItem) {
        if (bedrockTag == null) return;

        final StructuredDataContainer data = javaItem.dataContainer();

        if (bedrockTag.get("Damage") instanceof NumberTag durability) {
            data.set(StructuredDataKey.DAMAGE, durability.asInt());
        }
        if (bedrockTag.get("RepairCost") instanceof NumberTag repairCost) {
            data.set(StructuredDataKey.REPAIR_COST, repairCost.asInt());
        }
        if (bedrockTag.getByte("Unbreakable", (byte) 0) != 0) {
            data.set(StructuredDataKey.UNBREAKABLE1_21_5, Unit.INSTANCE);
        }

        if (bedrockTag.get("display") instanceof CompoundTag display) {
            if (display.get("Lore") instanceof ListTag<?> lore) {
                final List<Tag> loreTags = new ArrayList<>(lore.size());
                for (Tag line : lore) {
                    if (line instanceof StringTag stringLine) {
                        loreTags.add(TextUtil.stringToNbt(stringLine.getValue()));
                    }
                }
                if (!loreTags.isEmpty()) {
                    data.set(StructuredDataKey.LORE, loreTags.toArray(new Tag[0]));
                }
            }
        }

        if (bedrockTag.get("ench") instanceof ListTag<?> enchantments) {
            applyEnchantments(user, enchantments, javaItem.dataContainer().getData(StructuredDataKey.ENCHANTMENTS1_21_5), javaItem, StructuredDataKey.ENCHANTMENTS1_21_5);
        }
        if (bedrockTag.get("stored_ench") instanceof ListTag<?> storedEnchantments) {
            applyEnchantments(user, storedEnchantments, javaItem.dataContainer().getData(StructuredDataKey.STORED_ENCHANTMENTS1_21_5), javaItem, StructuredDataKey.STORED_ENCHANTMENTS1_21_5);
        }

        // CanPlaceOn/CanDestroy are skipped: converting them would require Java block registry ids,
        // which ViaBedrock does not have. The raw Bedrock strings stay on the BedrockItem.
    }

    // Java components -> Bedrock NBT
    public static void toBedrock(final UserConnection user, final Item javaItem, final BedrockItem bedrockItem) {
        final StructuredDataContainer data = javaItem.dataContainer();

        CompoundTag tag = bedrockItem.tag();
        if (tag == null) {
            tag = new CompoundTag();
            bedrockItem.setTag(tag);
        }

        final StructuredData<Tag> customName = data.getData(StructuredDataKey.CUSTOM_NAME);
        final StructuredData<Tag[]> lore = data.getData(StructuredDataKey.LORE);
        if (customName != null || lore != null) {
            final CompoundTag display = tag.getCompoundTag("display") != null ? tag.getCompoundTag("display") : new CompoundTag();
            if (customName != null) {
                display.put("Name", new StringTag(nbtComponentToLegacyString(customName.value())));
            }
            if (lore != null && lore.value() != null && lore.value().length != 0) {
                final ListTag<StringTag> loreLines = new ListTag<>(StringTag.class);
                for (Tag line : lore.value()) {
                    final String legacy = nbtComponentToLegacyString(line);
                    if (legacy.startsWith(VIA_BEDROCK_MARKER_PREFIX)) {
                        continue; // Don't write back ViaBedrock's own mapping marker lines
                    }
                    loreLines.add(new StringTag(legacy));
                }
                if (!loreLines.isEmpty()) {
                    display.put("Lore", loreLines);
                }
            }
            if (!display.isEmpty()) {
                tag.put("display", display);
            }
        }

        if (data.getData(StructuredDataKey.DAMAGE) != null) {
            tag.put("Damage", new IntTag(data.getData(StructuredDataKey.DAMAGE).value()));
        }
        if (data.getData(StructuredDataKey.REPAIR_COST) != null) {
            tag.put("RepairCost", new IntTag(data.getData(StructuredDataKey.REPAIR_COST).value()));
        }
        if (data.getData(StructuredDataKey.UNBREAKABLE1_21_5) != null) {
            tag.put("Unbreakable", new ByteTag((byte) 1));
        }

        if (data.getData(StructuredDataKey.ENCHANTMENTS1_21_5) != null && data.getData(StructuredDataKey.ENCHANTMENTS1_21_5).value() != null && !data.getData(StructuredDataKey.ENCHANTMENTS1_21_5).value().enchantments().isEmpty()) {
            tag.put("ench", enchantmentsToNbt(data.getData(StructuredDataKey.ENCHANTMENTS1_21_5).value()));
        }
        final StructuredData<Enchantments> storedEnchantments = data.getData(StructuredDataKey.STORED_ENCHANTMENTS1_21_5);
        if (storedEnchantments != null && storedEnchantments.value() != null && !storedEnchantments.value().enchantments().isEmpty() && !tag.contains("ench")) {
            // Bedrock stores enchantments of enchanted books in the same ench list
            tag.put("ench", enchantmentsToNbt(storedEnchantments.value()));
        }
    }

    private static ListTag<CompoundTag> enchantmentsToNbt(final Enchantments enchantments) {
        final ListTag<CompoundTag> enchTag = new ListTag<>(CompoundTag.class);
        if (enchantments != null) {
            for (Map.Entry<Integer, Integer> entry : enchantments.enchantments().entrySet()) {
                final Enchant_Type bedrockEnchantment = getBedrockEnchantment(entry.getKey());
                if (bedrockEnchantment == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.FINE, "Skipping unknown enchantment index " + entry.getKey() + " in bedrock item conversion");
                    continue;
                }
                final CompoundTag ench = new CompoundTag();
                ench.put("id", new ShortTag((short) bedrockEnchantment.getValue()));
                ench.put("lvl", new ShortTag(entry.getValue().shortValue()));
                enchTag.add(ench);
            }
        }
        return enchTag;
    }

    private static void applyEnchantments(final UserConnection user, final ListTag<?> bedrockEnchantments, final StructuredData<Enchantments> existingData, final Item javaItem, final StructuredDataKey<Enchantments> key) {
        Enchantments javaEnchantments;
        if (existingData != null && !existingData.isEmpty() && existingData.value() != null) {
            javaEnchantments = existingData.value();
        } else {
            javaEnchantments = new Enchantments(new Int2IntOpenHashMap(), true);
        }

        if (bedrockEnchantments.isEmpty()) {
            // An empty enchantment list doesn't grant an enchantment glint
            javaEnchantments = new Enchantments(new Int2IntOpenHashMap(), false);
        } else {
            for (Tag enchantment : bedrockEnchantments) {
                if (!(enchantment instanceof CompoundTag compoundTag)) continue;

                // Bedrock falls back to protection level 0 when the NBT types are wrong
                if (!(compoundTag.get("id") instanceof ShortTag idTag) || !(compoundTag.get("lvl") instanceof ShortTag levelTag)) {
                    final int protectionIndex = getJavaEnchantmentIndex(Enchant_Type.Protection);
                    if (protectionIndex != -1) {
                        javaEnchantments.add(protectionIndex, 0);
                    }
                    continue;
                }

                final Enchant_Type bedrockId = Enchant_Type.getByValue(idTag.asInt());
                if (bedrockId == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.FINE, "Unknown enchantment with id " + idTag.asInt() + " and level " + levelTag.asInt());
                    continue;
                }

                final int javaId = getJavaEnchantmentIndex(bedrockId);
                if (javaId == -1) {
                    ViaBedrock.getPlatform().getLogger().log(Level.FINE, "Missing java enchantment mapping for " + bedrockId);
                    continue;
                }
                javaEnchantments.add(javaId, levelTag.asInt());
            }
        }

        javaItem.dataContainer().set(key, javaEnchantments);
    }

    private static int getJavaEnchantmentIndex(final Enchant_Type bedrockEnchantment) {
        final String javaEnchantmentId = BedrockProtocol.MAPPINGS.getBedrockToJavaEnchantments().get(bedrockEnchantment);
        if (javaEnchantmentId == null) {
            return -1;
        }
        final CompoundTag registry = (CompoundTag) BedrockProtocol.MAPPINGS.getJavaRegistries().get(RegistryKeys.ENCHANTMENT);
        if (registry == null) {
            return -1;
        }
        final Tag entry = registry.get(javaEnchantmentId);
        if (!(entry instanceof CompoundTag compoundEntry)) {
            return -1;
        }
        return RegistryUtil.getRegistryIndex(registry, compoundEntry);
    }

    private static Enchant_Type getBedrockEnchantment(final int javaIndex) {
        final CompoundTag registry = (CompoundTag) BedrockProtocol.MAPPINGS.getJavaRegistries().get(RegistryKeys.ENCHANTMENT);
        if (registry == null) {
            return null;
        }
        final Map.Entry<String, Tag> entry = RegistryUtil.getRegistryEntry(registry, javaIndex);
        if (entry == null) {
            return null;
        }
        for (Map.Entry<Enchant_Type, String> mapping : BedrockProtocol.MAPPINGS.getBedrockToJavaEnchantments().entrySet()) {
            if (mapping.getValue().equals(entry.getKey())) {
                return mapping.getKey();
            }
        }
        return null;
    }

    private static String nbtComponentToLegacyString(final Tag tag) {
        return ProtocolConstants.JAVA_TEXT_COMPONENT_SERIALIZER.deserializeNbtTree(tag).asLegacyFormatString();
    }

}
