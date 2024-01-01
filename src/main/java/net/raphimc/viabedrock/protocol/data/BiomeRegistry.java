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

import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BiomeRegistry {

    public static ListTag buildJavaBiomeRegistry(final Map<String, CompoundTag> biomeDefinitions) {
        final ListTag javaBiomes = new ListTag();

        javaBiomes.add(getTheVoidBiome());

        final Map<String, Object> fogColor = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("fog_color");
        final Map<String, Object> waterFogColor = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("water_fog_color");
        final Map<String, Object> foliageColor = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("foliage_color");
        final Map<String, Object> grassColor = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("grass_color");
        final Map<String, Object> grassColorModifier = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("grass_color_modifier");
        final Map<String, Object> moodSound = BedrockProtocol.MAPPINGS.getBedrockToJavaBiomeExtraData().get("mood_sound");

        for (Map.Entry<String, CompoundTag> entry : biomeDefinitions.entrySet()) {
            final String bedrockIdentifier = entry.getKey();
            final String javaIdentifier = "minecraft:" + bedrockIdentifier;
            final CompoundTag bedrockBiome = entry.getValue();
            final CompoundTag javaBiome = new CompoundTag();
            final int bedrockId = BedrockProtocol.MAPPINGS.getBedrockBiomes().getOrDefault(bedrockIdentifier, -1);
            final int javaId = bedrockId + 1;

            if (bedrockId == -1) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing biome mapping for " + bedrockIdentifier);
                continue;
            }

            javaBiome.put("name", new StringTag(javaIdentifier));
            javaBiome.put("id", new IntTag(javaId));
            final CompoundTag element = new CompoundTag();
            javaBiome.put("element", element);
            element.put("temperature", bedrockBiome.get("temperature"));
            element.put("downfall", bedrockBiome.get("downfall"));
            element.put("has_precipitation", bedrockBiome.get("rain"));

            final List<String> tags = bedrockBiome.<ListTag>get("tags").getValue().stream().map(StringTag.class::cast).map(StringTag::getValue).collect(Collectors.toList());

            final CompoundTag effects = new CompoundTag();
            element.put("effects", effects);

            final float blue_spores = bedrockBiome.<FloatTag>get("blue_spores").asFloat();
            final float white_ash = bedrockBiome.<FloatTag>get("white_ash").asFloat();
            final float red_spores = bedrockBiome.<FloatTag>get("red_spores").asFloat();
            final float ash = bedrockBiome.<FloatTag>get("ash").asFloat();
            if (blue_spores > 0) {
                effects.put("particle", createParticle("minecraft:warped_spore", blue_spores / 10F));
            } else if (white_ash > 0) {
                effects.put("particle", createParticle("minecraft:white_ash", white_ash / 10F));
            } else if (red_spores > 0) {
                effects.put("particle", createParticle("minecraft:crimson_spore", red_spores / 10F));
            } else if (ash > 0) {
                effects.put("particle", createParticle("minecraft:ash", ash / 10F));
            }

            final int waterColorR = (int) (bedrockBiome.<FloatTag>get("waterColorR").asFloat() * 255);
            final int waterColorG = (int) (bedrockBiome.<FloatTag>get("waterColorG").asFloat() * 255);
            final int waterColorB = (int) (bedrockBiome.<FloatTag>get("waterColorB").asFloat() * 255);
            final int waterColorA = (int) (bedrockBiome.<FloatTag>get("waterColorA").asFloat() * 255);
            final int waterTransparency = (int) (bedrockBiome.<FloatTag>get("waterTransparency").asFloat() * 255);
            final int waterColor = (waterColorR << 16) + (waterColorG << 8) + waterColorB;
            effects.put("water_color", new IntTag(waterColor));

            for (String tag : tags) {
                if (fogColor.containsKey(tag)) {
                    effects.put("fog_color", new IntTag((Integer) fogColor.get(tag)));
                }
                if (foliageColor.containsKey(tag)) {
                    effects.put("foliage_color", new IntTag((Integer) foliageColor.get(tag)));
                }
                if (grassColor.containsKey(tag)) {
                    effects.put("grass_color", new IntTag((Integer) grassColor.get(tag)));
                }
                if (grassColorModifier.containsKey(tag)) {
                    effects.put("grass_color_modifier", new StringTag((String) grassColorModifier.get(tag)));
                }
                if (moodSound.containsKey(tag)) {
                    effects.put("mood_sound", createMoodSound((String) moodSound.get(tag)));
                }
            }

            if (waterFogColor.containsKey(bedrockIdentifier)) {
                effects.put("water_fog_color", new IntTag((Integer) waterFogColor.get(bedrockIdentifier)));
            }

            // One warning is enough
            if (!effects.contains("fog_color")) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing fog color for " + bedrockIdentifier + ": " + bedrockBiome);
            } else if (!effects.contains("water_fog_color")) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing water fog color for " + bedrockIdentifier + ": " + bedrockBiome);
            } else if (!effects.contains("mood_sound")) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing mood sound for " + bedrockIdentifier + ": " + bedrockBiome);
            }

            if (tags.contains("the_end")) {
                effects.put("sky_color", new IntTag(0));
            } else {
                effects.put("sky_color", new IntTag(getSkyColor(bedrockBiome.<FloatTag>get("temperature").asFloat())));
            }

            // TODO: Enhancement: Biome sounds

            javaBiomes.add(javaBiome);
        }

        return javaBiomes;
    }

    private static CompoundTag getTheVoidBiome() {
        final CompoundTag biome = new CompoundTag();
        biome.put("name", new StringTag("minecraft:the_void"));
        biome.put("id", new IntTag(0));

        final CompoundTag element = new CompoundTag();
        biome.put("element", element);
        element.put("temperature", new FloatTag(0.5F));
        element.put("downfall", new FloatTag(0.5F));
        element.put("has_precipitation", new ByteTag((byte) 0));

        final CompoundTag effects = new CompoundTag();
        element.put("effects", effects);
        effects.put("sky_color", new IntTag(8103167));
        effects.put("water_fog_color", new IntTag(329011));
        effects.put("fog_color", new IntTag(12638463));
        effects.put("water_color", new IntTag(4159204));
        effects.put("mood_sound", createMoodSound("minecraft:ambient.cave"));
        return biome;
    }

    private static CompoundTag createParticle(final String name, final float probability) {
        final CompoundTag particle = new CompoundTag();
        particle.put("probability", new FloatTag(probability));
        final CompoundTag options = new CompoundTag();
        particle.put("options", options);
        options.put("type", new StringTag(name));
        return particle;
    }

    private static CompoundTag createMoodSound(final String soundId) {
        final CompoundTag moodSound = new CompoundTag();
        moodSound.put("tick_delay", new IntTag(6000));
        moodSound.put("offset", new FloatTag(2F));
        moodSound.put("sound", new StringTag(soundId));
        moodSound.put("block_search_extent", new IntTag(8));
        return moodSound;
    }

    private static int getSkyColor(final float temperature) {
        float f = temperature / 3F;
        f = MathUtil.clamp(f, -1F, 1F);
        return Color.HSBtoRGB(0.62222224F - f * 0.05F, 0.5F + f * 0.1F, 1F) & 0xFFFFFF;
    }

}
