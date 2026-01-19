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
package net.raphimc.viabedrock.experimental.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class JavaMapPaletteStorage extends StoredObject {

    private final int[] SHADE_MULTS = new int[] {180, 220, 255, 135};
    private final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private final Int2ObjectMap<Color> BASE_COLORS = new Int2ObjectOpenHashMap<>();
    private final HashMap<Color, Short> COLOR_MAP;

    public JavaMapPaletteStorage(UserConnection user) {
        super(user);
        initBaseColors();

        COLOR_MAP = new HashMap<>(BASE_COLORS.size() * SHADE_MULTS.length);
        for (int i = 0; i < BASE_COLORS.size(); i++) {
            Color baseColor = BASE_COLORS.get(i);
            for (int shadeMult : SHADE_MULTS) {
                if (baseColor.equals(TRANSPARENT)) {
                    COLOR_MAP.put(TRANSPARENT, (short) i);
                } else {
                    int r = Math.min((baseColor.getRed() * shadeMult) / 255, 255);
                    int g = Math.min((baseColor.getGreen() * shadeMult) / 255, 255);
                    int b = Math.min((baseColor.getBlue() * shadeMult) / 255, 255);
                    COLOR_MAP.put(new Color(r, g, b), (short) i);
                }
            }
        }
    }

    //TODO: Mapping file? Auto-gen?
    private void initBaseColors() {
        BASE_COLORS.put(0, TRANSPARENT);                 // 0 NONE / Transparent
        BASE_COLORS.put(1, new Color(127, 178, 56));     // 1 GRASS
        BASE_COLORS.put(2, new Color(247, 233, 163));    // 2 SAND
        BASE_COLORS.put(3, new Color(199, 199, 199));    // 3 WOOL
        BASE_COLORS.put(4, new Color(255, 0, 0));        // 4 FIRE
        BASE_COLORS.put(5, new Color(160, 160, 255));    // 5 ICE
        BASE_COLORS.put(6, new Color(167, 167, 167));    // 6 METAL
        BASE_COLORS.put(7, new Color(0, 124, 0));        // 7 PLANT
        BASE_COLORS.put(8, new Color(255, 255, 255));    // 8 SNOW
        BASE_COLORS.put(9, new Color(164, 168, 184));    // 9 CLAY
        BASE_COLORS.put(10, new Color(151, 109, 77));    // 10 DIRT
        BASE_COLORS.put(11, new Color(112, 112, 112));   // 11 STONE
        BASE_COLORS.put(12, new Color(64, 64, 255));     // 12 WATER
        BASE_COLORS.put(13, new Color(143, 119, 72));    // 13 WOOD
        BASE_COLORS.put(14, new Color(255, 252, 245));   // 14 QUARTZ
        BASE_COLORS.put(15, new Color(216, 127, 51));    // 15 COLOR_ORANGE
        BASE_COLORS.put(16, new Color(178, 76, 216));    // 16 COLOR_MAGENTA
        BASE_COLORS.put(17, new Color(102, 153, 216));   // 17 COLOR_LIGHT_BLUE
        BASE_COLORS.put(18, new Color(229, 229, 51));    // 18 COLOR_YELLOW
        BASE_COLORS.put(19, new Color(127, 204, 25));    // 19 COLOR_LIGHT_GREEN
        BASE_COLORS.put(20, new Color(242, 127, 165));   // 20 COLOR_PINK
        BASE_COLORS.put(21, new Color(76, 76, 76));      // 21 COLOR_GRAY
        BASE_COLORS.put(22, new Color(153, 153, 153));   // 22 COLOR_LIGHT_GRAY
        BASE_COLORS.put(23, new Color(76, 127, 153));    // 23 COLOR_CYAN
        BASE_COLORS.put(24, new Color(127, 63, 178));    // 24 COLOR_PURPLE
        BASE_COLORS.put(25, new Color(51, 76, 178));     // 25 COLOR_BLUE
        BASE_COLORS.put(26, new Color(102, 76, 51));     // 26 COLOR_BROWN
        BASE_COLORS.put(27, new Color(102, 127, 51));    // 27 COLOR_GREEN
        BASE_COLORS.put(28, new Color(153, 51, 51));     // 28 COLOR_RED
        BASE_COLORS.put(29, new Color(25, 25, 25));      // 29 COLOR_BLACK
        BASE_COLORS.put(30, new Color(250, 238, 77));    // 30 GOLD
        BASE_COLORS.put(31, new Color(92, 219, 213));    // 31 DIAMOND
        BASE_COLORS.put(32, new Color(74, 128, 255));    // 32 LAPIS
        BASE_COLORS.put(33, new Color(0, 217, 58));      // 33 EMERALD
        BASE_COLORS.put(34, new Color(129, 86, 49));     // 34 PODZOL
        BASE_COLORS.put(35, new Color(112, 2, 0));       // 35 NETHER
        BASE_COLORS.put(36, new Color(209, 177, 161));   // 36 TERRACOTTA_WHITE
        BASE_COLORS.put(37, new Color(159, 82, 36));     // 37 TERRACOTTA_ORANGE
        BASE_COLORS.put(38, new Color(149, 87, 108));    // 38 TERRACOTTA_MAGENTA
        BASE_COLORS.put(39, new Color(112, 108, 138));   // 39 TERRACOTTA_LIGHT_BLUE
        BASE_COLORS.put(40, new Color(186, 133, 36));    // 40 TERRACOTTA_YELLOW
        BASE_COLORS.put(41, new Color(103, 117, 53));    // 41 TERRACOTTA_LIGHT_GREEN
        BASE_COLORS.put(42, new Color(160, 77, 78));     // 42 TERRACOTTA_PINK
        BASE_COLORS.put(43, new Color(57, 41, 35));      // 43 TERRACOTTA_GRAY
        BASE_COLORS.put(44, new Color(135, 107, 98));    // 44 TERRACOTTA_LIGHT_GRAY
        BASE_COLORS.put(45, new Color(87, 92, 92));      // 45 TERRACOTTA_CYAN
        BASE_COLORS.put(46, new Color(122, 73, 88));     // 46 TERRACOTTA_PURPLE
        BASE_COLORS.put(47, new Color(76, 62, 92));      // 47 TERRACOTTA_BLUE
        BASE_COLORS.put(48, new Color(76, 50, 35));      // 48 TERRACOTTA_BROWN
        BASE_COLORS.put(49, new Color(76, 82, 42));      // 49 TERRACOTTA_GREEN
        BASE_COLORS.put(50, new Color(142, 60, 46));     // 50 TERRACOTTA_RED
        BASE_COLORS.put(51, new Color(37, 22, 16));      // 51 TERRACOTTA_BLACK
        BASE_COLORS.put(52, new Color(189, 48, 49));     // 52 CRIMSON_NYLIUM
        BASE_COLORS.put(53, new Color(148, 63, 97));     // 53 CRIMSON_STEM
        BASE_COLORS.put(54, new Color(92, 25, 29));      // 54 CRIMSON_HYPHAE
        BASE_COLORS.put(55, new Color(22, 126, 134));    // 55 WARPED_NYLIUM
        BASE_COLORS.put(56, new Color(58, 142, 140));    // 56 WARPED_STEM
        BASE_COLORS.put(57, new Color(86, 44, 62));      // 57 WARPED_HYPHAE
        BASE_COLORS.put(58, new Color(20, 180, 133));    // 58 WARPED_WART_BLOCK
        BASE_COLORS.put(59, new Color(100, 100, 100));   // 59 DEEPSLATE
        BASE_COLORS.put(60, new Color(216, 175, 147));   // 60 RAW_IRON
        BASE_COLORS.put(61, new Color(127, 167, 150));   // 61 GLOW_LICHEN
    }

    public Color getBaseColor(int id) {
        return BASE_COLORS.getOrDefault(id, TRANSPARENT);
    }

    public int[] getShadeMults() {
        return SHADE_MULTS.clone();
    }

    public short[] convertToJavaPalette(int[] bedrockColors) {
        short[] javaColors = new short[bedrockColors.length];
        for (int i = 0; i < bedrockColors.length; i++) {
            int bedrockColor = bedrockColors[i];
            int r = (bedrockColor >> 16) & 0xFF;
            int g = (bedrockColor >> 8) & 0xFF;
            int b = bedrockColor & 0xFF;
            int a = (bedrockColor >> 24) & 0xFF;
            if (a == 0) {
                javaColors[i] = 0; // Transparent
                continue;
            }

            short closestIndex = 0;
            double closestDistance = Double.MAX_VALUE;
            for (short j = 0; j < COLOR_MAP.size(); j++) {
                Map.Entry<Color, Short> e = (Map.Entry<Color, Short>) COLOR_MAP.entrySet().toArray()[j];
                Color javaColor = e.getKey();
                double distance = Math.pow(r - javaColor.getRed(), 2) +
                                  Math.pow(g - javaColor.getGreen(), 2) +
                                  Math.pow(b - javaColor.getBlue(), 2);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestIndex = e.getValue();
                }
            }

            javaColors[i] = closestIndex;
        }
        return javaColors;
    }
}

