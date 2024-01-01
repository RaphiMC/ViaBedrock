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
package net.raphimc.viabedrock.protocol.data.enums;

public enum DyeColor {

    WHITE(16777215),
    ORANGE(16738335),
    MAGENTA(16711935),
    LIGHT_BLUE(10141901),
    YELLOW(16776960),
    LIME(12582656),
    PINK(16738740),
    GRAY(8421504),
    LIGHT_GRAY(13882323),
    CYAN(65535),
    PURPLE(10494192),
    BLUE(255),
    BROWN(9127187),
    GREEN(65280),
    RED(16711680),
    BLACK(0);

    private static final DyeColor[] JAVA_VALUES = new DyeColor[values().length];
    private static final DyeColor[] BEDROCK_VALUES = new DyeColor[values().length];

    static {
        for (DyeColor color : values()) {
            JAVA_VALUES[color.javaId()] = color;
            BEDROCK_VALUES[color.bedrockId()] = color;
        }
    }

    private final int signColor;

    DyeColor(final int signColor) {
        this.signColor = signColor;
    }

    public static DyeColor getByJavaId(final int id, final DyeColor fallback) {
        final DyeColor color = getByJavaId(id);
        return color == null ? fallback : color;
    }

    public static DyeColor getByJavaId(final int id) {
        if (id < 0 || id >= JAVA_VALUES.length) return null;

        return JAVA_VALUES[id];
    }

    public static DyeColor getByBedrockId(final int id, final DyeColor fallback) {
        final DyeColor color = getByBedrockId(id);
        return color == null ? fallback : color;
    }

    public static DyeColor getByBedrockId(final int id) {
        if (id < 0 || id >= BEDROCK_VALUES.length) return null;

        return BEDROCK_VALUES[id];
    }

    public static DyeColor getClosestDyeColor(final int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        DyeColor closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (DyeColor color : values()) {
            int colorR = (color.signColor >> 16) & 0xFF;
            int colorG = (color.signColor >> 8) & 0xFF;
            int colorB = color.signColor & 0xFF;

            int distance = (r - colorR) * (r - colorR) + (g - colorG) * (g - colorG) + (b - colorB) * (b - colorB);
            if (distance < closestDistance) {
                closest = color;
                closestDistance = distance;
            }
        }

        return closest;
    }

    public int signColor() {
        return this.signColor | 0xFF000000;
    }

    public byte javaId() {
        return (byte) this.ordinal();
    }

    public byte bedrockId() {
        return (byte) (values().length - 1 - this.ordinal());
    }

}
