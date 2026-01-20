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
import net.raphimc.viabedrock.experimental.model.map.MapColor;

import java.awt.Color;
import java.util.Arrays;

public class JavaMapPaletteStorage extends StoredObject {

    private final float[] JAVA_L;
    private final float[] JAVA_A;
    private final float[] JAVA_B;

    private final int CACHE_BITS = 5;
    private final int CACHE_SIZE = 1 << (CACHE_BITS * 3);
    private final short[] CACHE = new short[CACHE_SIZE];

    public JavaMapPaletteStorage(UserConnection user) {
        super(user);

        Arrays.fill(CACHE, (short) -1);

        MapColor[] colors = MapColor.values();
        JAVA_L = new float[colors.length];
        JAVA_A = new float[colors.length];
        JAVA_B = new float[colors.length];

        for (int i = 0; i < colors.length; i++) {
            Color c = colors[i].getColor();
            float[] lab = rgbToLab(c.getRed(), c.getGreen(), c.getBlue());
            JAVA_L[i] = lab[0];
            JAVA_A[i] = lab[1];
            JAVA_B[i] = lab[2];
        }
    }

    public short[] convertToJavaPalette(int[] bedrockColors) {
        //TODO: Check biome tinting for grass/foliage/water
        short[] javaColors = new short[bedrockColors.length];

        for (int i = 0; i < bedrockColors.length; i++) {
            int c = bedrockColors[i];

            int a = (c >>> 24);
            if (a == 0) {
                javaColors[i] = 0;
                continue;
            }

            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;

            int key = quantKey(r, g, b);
            short cached = CACHE[key];
            if (cached != -1) {
                javaColors[i] = cached;
                continue;
            }

            float[] lab = rgbToLab(r, g, b);

            float bestDist = Float.MAX_VALUE;
            short best = 0;

            for (short j = 0; j < JAVA_L.length; j++) {
                float dL = lab[0] - JAVA_L[j];
                float dA = lab[1] - JAVA_A[j];
                float dB = lab[2] - JAVA_B[j];

                float dist = dL * dL + dA * dA + dB * dB;
                if (dist < bestDist) {
                    bestDist = dist;
                    best = j;
                }
            }

            CACHE[key] = best;
            javaColors[i] = best;
        }

        return javaColors;
    }

    private int quantKey(int r, int g, int b) {
        int rq = r >> (8 - CACHE_BITS);
        int gq = g >> (8 - CACHE_BITS);
        int bq = b >> (8 - CACHE_BITS);
        return (rq << (CACHE_BITS * 2)) | (gq << CACHE_BITS) | bq;
    }

    private float[] rgbToLab(int r, int g, int b) {
        // sRGB → linear
        float rf = pivotRgb(r / 255f);
        float gf = pivotRgb(g / 255f);
        float bf = pivotRgb(b / 255f);

        // linear RGB → XYZ
        float x = rf * 0.4124f + gf * 0.3576f + bf * 0.1805f;
        float y = rf * 0.2126f + gf * 0.7152f + bf * 0.0722f;
        float z = rf * 0.0193f + gf * 0.1192f + bf * 0.9505f;

        // XYZ → LAB
        return xyzToLab(x, y, z);
    }

    private float pivotRgb(float n) {
        return n <= 0.04045f
                ? n / 12.92f
                : (float) Math.pow((n + 0.055f) / 1.055f, 2.4f);
    }

    private float[] xyzToLab(float x, float y, float z) {
        // D65 reference white
        float xr = x / 0.95047f;
        float yr = y / 1.00000f;
        float zr = z / 1.08883f;

        float fx = pivotXyz(xr);
        float fy = pivotXyz(yr);
        float fz = pivotXyz(zr);

        float L = 116f * fy - 16f;
        float A = 500f * (fx - fy);
        float B = 200f * (fy - fz);

        return new float[] { L, A, B };
    }

    private float pivotXyz(float n) {
        return n > 0.008856f
                ? (float) Math.cbrt(n)
                : (7.787f * n) + (16f / 116f);
    }
}