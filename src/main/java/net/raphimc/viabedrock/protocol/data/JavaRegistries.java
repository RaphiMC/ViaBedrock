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
package net.raphimc.viabedrock.protocol.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntPair;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class JavaRegistries {

    public static CompoundTag createJavaRegistries(final GameSessionStorage gameSession) {
        final CompoundTag registries = BedrockProtocol.MAPPINGS.getJavaRegistries().copy();

        registries.put("minecraft:worldgen/biome", buildJavaBiomeRegistry(gameSession.getBedrockBiomeDefinitions()));
        modifyDimensionRegistry(gameSession, registries.getCompoundTag("minecraft:dimension_type"));
        modifyWolfVariantRegistry(registries.getCompoundTag("minecraft:wolf_variant"));
        registries.remove("minecraft:chat_type"); // Not needed

        return registries;
    }

    private static void modifyDimensionRegistry(final GameSessionStorage gameSession, final CompoundTag dimensionRegistry) {
        dimensionRegistry.remove("minecraft:overworld_caves");
        if (gameSession.getBedrockVanillaVersion().isLowerThan("1.18.0")) {
            dimensionRegistry.getCompoundTag("minecraft:overworld").putInt("min_y", 0);
            dimensionRegistry.getCompoundTag("minecraft:overworld").putInt("height", 256);
            dimensionRegistry.getCompoundTag("minecraft:overworld").putInt("logical_height", 256);
        }
        for (Map.Entry<String, IntIntPair> entry : gameSession.getBedrockDimensionDefinitions().entrySet()) {
            final CompoundTag dimensionTag = new CompoundTag();
            final int height = entry.getValue().rightInt() - entry.getValue().leftInt();
            dimensionTag.putInt("min_y", entry.getValue().leftInt());
            dimensionTag.putInt("height", height);
            dimensionTag.putInt("logical_height", height);
            dimensionRegistry.getCompoundTag(entry.getKey()).putAll(dimensionTag);
        }
    }

    private static CompoundTag buildJavaBiomeRegistry(final CompoundTag biomeDefinitions) {
        final CompoundTag javaBiomes = new CompoundTag();
        javaBiomes.put("minecraft:the_void", getTheVoidBiome());

        final Map<String, Object> fogColor = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("fog_color");
        final Map<String, Object> waterFogColor = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("water_fog_color");
        final Map<String, Object> foliageColor = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("foliage_color");
        final Map<String, Object> grassColor = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("grass_color");
        final Map<String, Object> grassColorModifier = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("grass_color_modifier");
        final Map<String, Object> moodSound = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("mood_sound");

        for (String bedrockBiomeName : BedrockProtocol.MAPPINGS.getBedrockBiomes().keySet()) {
            final CompoundTag bedrockBiome = biomeDefinitions.getCompoundTag(bedrockBiomeName);
            if (bedrockBiome == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing biome definition for " + bedrockBiomeName);
                continue;
            }

            final String javaIdentifier = "minecraft:" + bedrockBiomeName;
            final CompoundTag javaBiome = new CompoundTag();
            javaBiome.put("temperature", bedrockBiome.get("temperature"));
            javaBiome.put("downfall", bedrockBiome.get("downfall"));
            javaBiome.put("has_precipitation", bedrockBiome.get("rain"));

            final List<String> tags = bedrockBiome.getListTag("tags").stream().map(StringTag.class::cast).map(StringTag::getValue).toList();

            final CompoundTag effects = new CompoundTag();
            javaBiome.put("effects", effects);

            final float blue_spores = bedrockBiome.getFloat("blue_spores");
            final float white_ash = bedrockBiome.getFloat("white_ash");
            final float red_spores = bedrockBiome.getFloat("red_spores");
            final float ash = bedrockBiome.getFloat("ash");
            if (blue_spores > 0) {
                effects.put("particle", createParticle("minecraft:warped_spore", blue_spores / 10F));
            } else if (white_ash > 0) {
                effects.put("particle", createParticle("minecraft:white_ash", white_ash / 10F));
            } else if (red_spores > 0) {
                effects.put("particle", createParticle("minecraft:crimson_spore", red_spores / 10F));
            } else if (ash > 0) {
                effects.put("particle", createParticle("minecraft:ash", ash / 10F));
            }

            final int waterColorR = (int) (bedrockBiome.getFloatTag("waterColorR").asFloat() * 255);
            final int waterColorG = (int) (bedrockBiome.getFloatTag("waterColorG").asFloat() * 255);
            final int waterColorB = (int) (bedrockBiome.getFloatTag("waterColorB").asFloat() * 255);
            final int waterColorA = (int) (bedrockBiome.getFloatTag("waterColorA").asFloat() * 255);
            final int waterTransparency = (int) (bedrockBiome.getFloatTag("waterTransparency").asFloat() * 255);
            final int waterColor = (waterColorR << 16) + (waterColorG << 8) + waterColorB;
            effects.putInt("water_color", waterColor);

            for (String tag : tags) {
                if (fogColor.containsKey(tag)) {
                    effects.putInt("fog_color", (Integer) fogColor.get(tag));
                }
                if (foliageColor.containsKey(tag)) {
                    effects.putInt("foliage_color", (Integer) foliageColor.get(tag));
                }
                if (grassColor.containsKey(tag)) {
                    effects.putInt("grass_color", (Integer) grassColor.get(tag));
                }
                if (grassColorModifier.containsKey(tag)) {
                    effects.putString("grass_color_modifier", (String) grassColorModifier.get(tag));
                }
                if (moodSound.containsKey(tag)) {
                    effects.put("mood_sound", createMoodSound((String) moodSound.get(tag)));
                }
            }

            if (waterFogColor.containsKey(bedrockBiomeName)) {
                effects.putInt("water_fog_color", (Integer) waterFogColor.get(bedrockBiomeName));
            }

            // One warning is enough
            if (!effects.contains("fog_color")) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing fog color for " + bedrockBiomeName + ": " + bedrockBiome);
            } else if (!effects.contains("water_fog_color")) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing water fog color for " + bedrockBiomeName + ": " + bedrockBiome);
            } else if (!effects.contains("mood_sound")) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing mood sound for " + bedrockBiomeName + ": " + bedrockBiome);
            }

            if (tags.contains("the_end")) {
                effects.putInt("sky_color", 0);
            } else {
                effects.putInt("sky_color", getSkyColor(bedrockBiome.getFloatTag("temperature").asFloat()));
            }

            // TODO: Enhancement: Biome sounds

            javaBiomes.put(javaIdentifier, javaBiome);
        }

        return javaBiomes;
    }

    private static void modifyWolfVariantRegistry(final CompoundTag wolfVariantRegistry) {
        // Biomes are not used client-side, so clearing it is fine
        for (Map.Entry<String, Tag> entry : wolfVariantRegistry.entrySet()) {
            ((CompoundTag) entry.getValue()).put("biomes", new ListTag<>(StringTag.class));
        }
    }

    private static CompoundTag getTheVoidBiome() {
        final CompoundTag biome = new CompoundTag();
        biome.putFloat("temperature", 0.5F);
        biome.putFloat("downfall", 0.5F);
        biome.putBoolean("has_precipitation", false);

        final CompoundTag effects = new CompoundTag();
        biome.put("effects", effects);
        effects.putInt("sky_color", 8103167);
        effects.putInt("water_fog_color", 329011);
        effects.putInt("fog_color", 12638463);
        effects.putInt("water_color", 4159204);
        effects.put("mood_sound", createMoodSound("minecraft:ambient.cave"));
        return biome;
    }

    private static CompoundTag createParticle(final String name, final float probability) {
        final CompoundTag particle = new CompoundTag();
        particle.putFloat("probability", probability);
        final CompoundTag options = new CompoundTag();
        particle.put("options", options);
        options.putString("type", name);
        return particle;
    }

    private static CompoundTag createMoodSound(final String soundId) {
        final CompoundTag moodSound = new CompoundTag();
        moodSound.putInt("tick_delay", 6000);
        moodSound.putFloat("offset", 2F);
        moodSound.putString("sound", soundId);
        moodSound.putInt("block_search_extent", 8);
        return moodSound;
    }

    private static int getSkyColor(final float temperature) {
        float f = temperature / 3F;
        f = MathUtil.clamp(f, -1F, 1F);
        return Color.HSBtoRGB(0.62222224F - f * 0.05F, 0.5F + f * 0.1F, 1F) & 0xFFFFFF;
    }

}
