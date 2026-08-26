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
package net.raphimc.viabedrock.protocol.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrim;
import com.viaversion.viaversion.api.minecraft.item.data.BlockPredicate;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPatternLayer;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.Fireworks;
import com.viaversion.viaversion.api.minecraft.item.data.WritableBook;
import com.viaversion.viaversion.api.minecraft.item.data.BlockStateProperties;
import com.viaversion.viaversion.api.minecraft.item.data.PotionContents;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffect;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffectData;
import com.viaversion.viaversion.api.minecraft.item.data.WrittenBook;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Unit;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.RegistryUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.DyeColor;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.Enchant_Type;
import net.raphimc.viabedrock.protocol.data.generated.java.RegistryKeys;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.storage.ResourcePackStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;

/**
 * Translates the NBT data of a Bedrock item into the Java Edition item components.
 */
public class ItemDataRewriter {

    private static final int MAX_BOOK_PAGES = 100;

    /**
     * Applies the item data which the bedrock -> java item mapping defines for an item.
     *
     * <p>The mappings still describe it in the pre 1.20.5 NBT format, so it has to be converted to the item components
     * the Java client expects.</p>
     */
    public static void applyMappedData(final CompoundTag overrideTag, final StructuredDataContainer data) {
        if (overrideTag.get("Potion") instanceof StringTag potion) {
            final Integer potionId = BedrockProtocol.MAPPINGS.getJavaPotions().get(potion.getValue());
            if (potionId != null) {
                data.set(StructuredDataKey.POTION_CONTENTS1_21_2, new PotionContents(potionId, null, new PotionEffect[0]));
            } else {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing java potion: " + potion.getValue());
            }
        }

        if (overrideTag.get("custom_potion_effects") instanceof ListTag<?> customEffects) {
            final List<PotionEffect> effects = new ArrayList<>(customEffects.size());
            for (Tag effect : customEffects) {
                if (!(effect instanceof CompoundTag effectTag) || !(effectTag.get("id") instanceof StringTag id)) continue;
                final Integer effectId = BedrockProtocol.MAPPINGS.getJavaEffects().get(id.getValue());
                if (effectId == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing java effect: " + id.getValue());
                    continue;
                }
                effects.add(new PotionEffect(effectId, new PotionEffectData(
                        effectTag.getInt("amplifier", 0),
                        effectTag.getInt("duration", 0),
                        false, // ambient
                        true, // show particles
                        true, // show icon
                        null // hidden effect
                )));
            }

            final Integer customColor = overrideTag.get("CustomPotionColor") instanceof NumberTag color ? color.asInt() : null;
            data.set(StructuredDataKey.POTION_CONTENTS1_21_2, new PotionContents(null, customColor, effects.toArray(new PotionEffect[0])));
        }

        if (overrideTag.get("instrument") instanceof StringTag instrument) {
            final CompoundTag instrumentRegistry = (CompoundTag) BedrockProtocol.MAPPINGS.getJavaRegistries().get(RegistryKeys.INSTRUMENT);
            final int instrumentId = instrumentRegistry != null ? RegistryUtil.getRegistryIndex(instrumentRegistry, instrument.getValue()) : -1;
            if (instrumentId != -1) {
                data.set(StructuredDataKey.INSTRUMENT26_1, Holder.of(instrumentId));
            } else {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing java instrument: " + instrument.getValue());
            }
        }

        if (overrideTag.get("BlockStateTag") instanceof CompoundTag blockStateTag) {
            final Map<String, String> properties = new HashMap<>(blockStateTag.size());
            for (Map.Entry<String, Tag> entry : blockStateTag.entrySet()) {
                properties.put(entry.getKey(), entry.getValue().asRawString());
            }
            data.set(StructuredDataKey.BLOCK_STATE, new BlockStateProperties(properties));
        }
    }

    public static void toJava(final UserConnection user, final BedrockItem bedrockItem, final Item javaItem) {
        // Adventure mode restrictions are stored next to the item data instead of inside it
        rewriteAdventureModePredicate(bedrockItem.canPlace(), javaItem, StructuredDataKey.CAN_PLACE_ON1_20_5);
        rewriteAdventureModePredicate(bedrockItem.canBreak(), javaItem, StructuredDataKey.CAN_BREAK1_20_5);

        final CompoundTag bedrockTag = bedrockItem.tag();
        if (bedrockTag == null) {
            return;
        }

        rewriteDisplay(user, bedrockTag, javaItem);
        rewriteDurability(bedrockTag, javaItem);
        rewriteEnchantments(user, bedrockTag, javaItem);
        rewriteDyedColor(bedrockTag, javaItem);
        rewriteTrim(bedrockTag, javaItem);
        rewriteBook(user, bedrockTag, javaItem);
        rewriteFireworks(bedrockTag, javaItem);
        rewriteBanner(bedrockTag, javaItem);
    }

    /**
     * Translates the blocks an item may be placed on or used to break in adventure mode.
     */
    private static void rewriteAdventureModePredicate(final String[] bedrockBlocks, final Item javaItem, final StructuredDataKey<AdventureModePredicate> key) {
        if (bedrockBlocks == null || bedrockBlocks.length == 0) {
            return;
        }

        final IntList javaBlocks = new IntArrayList(bedrockBlocks.length);
        for (String bedrockBlock : bedrockBlocks) {
            final Integer javaBlock = BedrockProtocol.MAPPINGS.getJavaBlocks().get(Key.namespaced(bedrockBlock));
            if (javaBlock != null) {
                javaBlocks.add(javaBlock.intValue());
            } else {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing java block for adventure mode restriction: " + bedrockBlock);
            }
        }
        if (javaBlocks.isEmpty()) {
            return;
        }

        final BlockPredicate predicate = new BlockPredicate(HolderSet.of(javaBlocks.toIntArray()), null, null);
        javaItem.dataContainer().set(key, new AdventureModePredicate(new BlockPredicate[]{predicate}));
    }

    private static void rewriteDisplay(final UserConnection user, final CompoundTag bedrockTag, final Item javaItem) {
        if (!(bedrockTag.get("display") instanceof CompoundTag display)) {
            return;
        }

        if (display.contains("Name")) { // Bedrock client defaults to an empty string if the type is wrong
            javaItem.dataContainer().set(StructuredDataKey.CUSTOM_NAME, TextUtil.stringToNbt(display.getString("Name", "")));
        }
        if (display.get("Lore") instanceof ListTag<?> lore && !lore.isEmpty()) {
            final ResourcePackStorage resourcePackStorage = user.get(ResourcePackStorage.class);
            final List<Tag> javaLore = new ArrayList<>(lore.size());
            for (Tag line : lore) {
                if (!(line instanceof StringTag stringLine)) continue;
                javaLore.add(TextUtil.stringToNbt(resourcePackStorage.getTexts().translate(stringLine.getValue())));
            }
            if (!javaLore.isEmpty()) {
                javaItem.dataContainer().set(StructuredDataKey.LORE, javaLore.toArray(new Tag[0]));
            }
        }
    }

    private static void rewriteDurability(final CompoundTag bedrockTag, final Item javaItem) {
        if (bedrockTag.get("Damage") instanceof NumberTag damage && damage.asInt() > 0) {
            javaItem.dataContainer().set(StructuredDataKey.DAMAGE, damage.asInt());
        }
        if (bedrockTag.get("RepairCost") instanceof NumberTag repairCost && repairCost.asInt() > 0) {
            javaItem.dataContainer().set(StructuredDataKey.REPAIR_COST, repairCost.asInt());
        }
        if (bedrockTag.get("Unbreakable") instanceof NumberTag unbreakable && unbreakable.asByte() != 0) {
            javaItem.dataContainer().set(StructuredDataKey.UNBREAKABLE1_21_5, Unit.INSTANCE);
        }
    }

    private static void rewriteEnchantments(final UserConnection user, final CompoundTag bedrockTag, final Item javaItem) {
        if (!(bedrockTag.get("ench") instanceof ListTag<?> enchantments) || enchantments.isEmpty()) {
            return;
        }

        final CompoundTag enchantmentRegistry = (CompoundTag) BedrockProtocol.MAPPINGS.getJavaRegistries().get(RegistryKeys.ENCHANTMENT);
        final Enchantments javaEnchantments = new Enchantments(true);
        for (Tag enchantment : enchantments) {
            if (!(enchantment instanceof CompoundTag enchantmentTag)) continue;
            // The Bedrock client requires both values to be shorts and falls back to protection 0 otherwise
            if (!(enchantmentTag.get("id") instanceof NumberTag idTag) || !(enchantmentTag.get("lvl") instanceof NumberTag levelTag)) continue;

            final Enchant_Type bedrockEnchantment = Enchant_Type.getByValue(idTag.asInt());
            if (bedrockEnchantment == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock enchantment id: " + idTag.asInt());
                continue;
            }
            final String javaEnchantment = BedrockProtocol.MAPPINGS.getBedrockToJavaEnchantments().get(bedrockEnchantment);
            if (javaEnchantment == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing bedrock -> java enchantment mapping for " + bedrockEnchantment);
                continue;
            }
            final int javaId = RegistryUtil.getRegistryIndex(enchantmentRegistry, javaEnchantment);
            if (javaId == -1) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing java enchantment registry entry for " + javaEnchantment);
                continue;
            }
            javaEnchantments.add(javaId, levelTag.asInt());
        }

        if (javaEnchantments.size() == 0) {
            return;
        }
        // Enchanted books store their enchantments separately on Java so that they are not applied to the book itself
        if (javaItem.identifier() == BedrockProtocol.MAPPINGS.getJavaItems().getOrDefault("minecraft:enchanted_book", -1)) {
            javaItem.dataContainer().set(StructuredDataKey.STORED_ENCHANTMENTS1_21_5, javaEnchantments);
        } else {
            javaItem.dataContainer().set(StructuredDataKey.ENCHANTMENTS1_21_5, javaEnchantments);
        }
    }

    private static void rewriteDyedColor(final CompoundTag bedrockTag, final Item javaItem) {
        if (bedrockTag.get("customColor") instanceof NumberTag customColor) {
            javaItem.dataContainer().set(StructuredDataKey.DYED_COLOR1_21_5, new DyedColor(customColor.asInt() & 0xFFFFFF));
        }
    }

    private static void rewriteTrim(final CompoundTag bedrockTag, final Item javaItem) {
        if (!(bedrockTag.get("Trim") instanceof CompoundTag trim)) {
            return;
        }
        final String material = trim.getString("Material", null);
        final String pattern = trim.getString("Pattern", null);
        if (material == null || pattern == null) {
            return;
        }

        final CompoundTag materialRegistry = (CompoundTag) BedrockProtocol.MAPPINGS.getJavaRegistries().get(RegistryKeys.TRIM_MATERIAL);
        final CompoundTag patternRegistry = (CompoundTag) BedrockProtocol.MAPPINGS.getJavaRegistries().get(RegistryKeys.TRIM_PATTERN);
        if (materialRegistry == null || patternRegistry == null) {
            return;
        }

        final int materialId = RegistryUtil.getRegistryIndex(materialRegistry, namespaced(material));
        final int patternId = RegistryUtil.getRegistryIndex(patternRegistry, namespaced(pattern));
        if (materialId == -1 || patternId == -1) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing java armor trim for material " + material + " and pattern " + pattern);
            return;
        }
        javaItem.dataContainer().set(StructuredDataKey.TRIM1_21_5, new ArmorTrim(Holder.of(materialId), Holder.of(patternId)));
    }

    private static void rewriteBook(final UserConnection user, final CompoundTag bedrockTag, final Item javaItem) {
        if (!(bedrockTag.get("pages") instanceof ListTag<?> pages)) {
            return;
        }

        final List<String> pageTexts = new ArrayList<>();
        for (Tag page : pages) {
            if (pageTexts.size() >= MAX_BOOK_PAGES) break;
            if (page instanceof CompoundTag pageTag) {
                pageTexts.add(pageTag.getString("text", ""));
            } else if (page instanceof StringTag pageTag) {
                pageTexts.add(pageTag.getValue());
            }
        }
        if (pageTexts.isEmpty()) {
            return;
        }

        final String title = bedrockTag.getString("title", null);
        final String author = bedrockTag.getString("author", null);
        if (title != null || author != null) { // Written book
            final FilterableComponent[] javaPages = new FilterableComponent[pageTexts.size()];
            for (int i = 0; i < javaPages.length; i++) {
                javaPages[i] = new FilterableComponent(TextUtil.stringToNbt(pageTexts.get(i)), null);
            }
            final int generation = bedrockTag.get("generation") instanceof NumberTag generationTag ? generationTag.asInt() : 0;
            javaItem.dataContainer().set(StructuredDataKey.WRITTEN_BOOK_CONTENT, new WrittenBook(
                    new FilterableString(title != null ? title : "", null),
                    author != null ? author : "",
                    Math.max(0, Math.min(3, generation)),
                    javaPages,
                    false
            ));
        } else { // Book and quill
            final FilterableString[] javaPages = new FilterableString[pageTexts.size()];
            for (int i = 0; i < javaPages.length; i++) {
                javaPages[i] = new FilterableString(pageTexts.get(i), null);
            }
            javaItem.dataContainer().set(StructuredDataKey.WRITABLE_BOOK_CONTENT, new WritableBook(javaPages));
        }
    }

    private static void rewriteFireworks(final CompoundTag bedrockTag, final Item javaItem) {
        if (bedrockTag.get("Fireworks") instanceof CompoundTag fireworks) {
            final int flight = fireworks.get("Flight") instanceof NumberTag flightTag ? flightTag.asInt() : 1;
            final List<FireworkExplosion> explosions = new ArrayList<>();
            if (fireworks.get("Explosions") instanceof ListTag<?> explosionsTag) {
                for (Tag explosion : explosionsTag) {
                    if (!(explosion instanceof CompoundTag explosionTag)) continue;
                    explosions.add(convertExplosion(explosionTag));
                }
            }
            javaItem.dataContainer().set(StructuredDataKey.FIREWORKS, new Fireworks(flight, explosions.toArray(new FireworkExplosion[0])));
        } else if (bedrockTag.get("FireworksItem") instanceof CompoundTag fireworkStar) {
            javaItem.dataContainer().set(StructuredDataKey.FIREWORK_EXPLOSION, convertExplosion(fireworkStar));
        }
    }

    private static FireworkExplosion convertExplosion(final CompoundTag explosionTag) {
        final int shape = explosionTag.get("FireworkType") instanceof NumberTag shapeTag ? shapeTag.asInt() : 0;
        return new FireworkExplosion(
                Math.max(0, Math.min(FireworkExplosion.SHAPES.length - 1, shape)),
                readColors(explosionTag, "FireworkColor"),
                readColors(explosionTag, "FireworkFade"),
                explosionTag.get("FireworkTrail") instanceof NumberTag trail && trail.asByte() != 0,
                explosionTag.get("FireworkFlicker") instanceof NumberTag flicker && flicker.asByte() != 0
        );
    }

    /**
     * Bedrock stores firework colors as a byte array of dye color ids, while Java uses the actual rgb values.
     */
    private static int[] readColors(final CompoundTag explosionTag, final String key) {
        final Tag colorsTag = explosionTag.get(key);
        final byte[] bedrockColors;
        if (colorsTag instanceof com.viaversion.nbt.tag.ByteArrayTag byteArrayTag) {
            bedrockColors = byteArrayTag.getValue();
        } else if (colorsTag instanceof NumberTag numberTag) {
            bedrockColors = new byte[]{numberTag.asByte()};
        } else {
            return new int[0];
        }

        final int[] javaColors = new int[bedrockColors.length];
        for (int i = 0; i < bedrockColors.length; i++) {
            javaColors[i] = DyeColor.getByBedrockId(bedrockColors[i], DyeColor.BLACK).fireworkColor();
        }
        return javaColors;
    }

    private static void rewriteBanner(final CompoundTag bedrockTag, final Item javaItem) {
        if (bedrockTag.get("Base") instanceof NumberTag base) {
            javaItem.dataContainer().set(StructuredDataKey.BASE_COLOR, (int) DyeColor.getByBedrockId(base.asInt(), DyeColor.BLACK).javaId());
        }
        if (!(bedrockTag.get("Patterns") instanceof ListTag<?> patterns) || patterns.isEmpty()) {
            return;
        }

        final CompoundTag patternRegistry = (CompoundTag) BedrockProtocol.MAPPINGS.getJavaRegistries().get(RegistryKeys.BANNER_PATTERN);
        if (patternRegistry == null) {
            return;
        }

        final List<BannerPatternLayer> layers = new ArrayList<>(patterns.size());
        for (Tag pattern : patterns) {
            if (!(pattern instanceof CompoundTag patternTag) || !(patternTag.get("Pattern") instanceof StringTag patternName)) continue;

            final String javaPattern = BedrockProtocol.MAPPINGS.getBedrockToJavaBannerPatterns().get(patternName.getValue());
            if (javaPattern == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock banner pattern: " + patternName.getValue());
                continue;
            }
            final int javaPatternId = RegistryUtil.getRegistryIndex(patternRegistry, namespaced(javaPattern));
            if (javaPatternId == -1) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing java banner pattern registry entry for " + javaPattern);
                continue;
            }
            final DyeColor color = DyeColor.getByBedrockId(patternTag.getInt("Color", DyeColor.BLACK.bedrockId()), DyeColor.BLACK);
            layers.add(new BannerPatternLayer(Holder.of(javaPatternId), color.javaId()));
        }

        if (!layers.isEmpty()) {
            javaItem.dataContainer().set(StructuredDataKey.BANNER_PATTERNS, layers.toArray(new BannerPatternLayer[0]));
        }
    }

    private static String namespaced(final String identifier) {
        return identifier.contains(":") ? identifier : "minecraft:" + identifier;
    }

}
