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
package net.raphimc.viabedrock.protocol.data.enums;

public enum DyeColor {

    WHITE(16777215, 15790320),
    ORANGE(16738335, 15435844),
    MAGENTA(16711935, 12801229),
    LIGHT_BLUE(10141901, 6719955),
    YELLOW(16776960, 14602026),
    LIME(12582656, 4312372),
    PINK(16738740, 14188952),
    GRAY(8421504, 4408131),
    LIGHT_GRAY(13882323, 11250603),
    CYAN(65535, 2651799),
    PURPLE(10494192, 8073150),
    BLUE(255, 2437522),
    BROWN(9127187, 5320730),
    GREEN(65280, 3887386),
    RED(16711680, 11743532),
    BLACK(0, 1973019);

    private static final DyeColor[] JAVA_VALUES = new DyeColor[values().length];
    private static final DyeColor[] BEDROCK_VALUES = new DyeColor[values().length];

    static {
        for (DyeColor color : values()) {
            JAVA_VALUES[color.javaId()] = color;
            BEDROCK_VALUES[color.bedrockId()] = color;
        }
    }

    private final int signColor;
    private final int fireworkColor;

    DyeColor(final int signColor, final int fireworkColor) {
        this.signColor = signColor;
        this.fireworkColor = fireworkColor;
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

    /**
     * @return The rgb value Java Edition uses to render firework explosions of this color
     */
    public int fireworkColor() {
        return this.fireworkColor;
    }

    public byte javaId() {
        return (byte) this.ordinal();
    }

    public byte bedrockId() {
        return (byte) (values().length - 1 - this.ordinal());
    }

}
