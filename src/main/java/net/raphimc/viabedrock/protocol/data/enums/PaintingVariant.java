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

import net.raphimc.viabedrock.protocol.model.Position3f;

public enum PaintingVariant {

    KEBAB("Kebab", 16, 16),
    AZTEC("Aztec", 16, 16),
    ALBAN("Alban", 16, 16),
    AZTEC2("Aztec2", 16, 16),
    BOMB("Bomb", 16, 16),
    PLANT("Plant", 16, 16),
    WASTELAND("Wasteland", 16, 16),
    POOL("Pool", 32, 16),
    COURBET("Courbet", 32, 16),
    SEA("Sea", 32, 16),
    SUNSET("Sunset", 32, 16),
    CREEBET("Creebet", 32, 16),
    WANDERER("Wanderer", 16, 32),
    GRAHAM("Graham", 16, 32),
    MATCH("Match", 32, 32),
    BUST("Bust", 32, 32),
    STAGE("Stage", 32, 32),
    VOID("Void", 32, 32),
    SKULL_AND_ROSES("SkullAndRoses", 32, 32),
    WITHER("Wither", 32, 32),
    FIGHTERS("Fighters", 64, 32),
    POINTER("Pointer", 64, 64),
    PIGSCENE("Pigscene", 64, 64),
    BURNING_SKULL("BurningSkull", 64, 64),
    SKELETON("Skeleton", 64, 48),
    EARTH("Earth", 32, 32),
    WIND("Wind", 32, 32),
    WATER("Water", 32, 32),
    FIRE("Fire", 32, 32),
    DONKEY_KONG("DonkeyKong", 64, 48);

    private static final float OFFSET = -0.46875F;

    private final String name;
    private final int width;
    private final int height;

    PaintingVariant(final String name, final int width, final int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public static PaintingVariant getByName(final String name) {
        for (PaintingVariant variant : values()) {
            if (variant.name.equals(name)) {
                return variant;
            }
        }

        return KEBAB;
    }

    public Position3f getJavaPositionOffset(final Direction direction) {
        final float widthOffset = this.width % 32 == 0 ? 0.5F : 0;
        final float heightOffset = this.height % 32 == 0 ? 0.5F : 0;

        final Position3f position = new Position3f(-0.5F, -0.5F, -0.5F);
        switch (direction) {
            case NORTH:
                return position.subtract(-widthOffset, heightOffset, -OFFSET);
            case EAST:
                return position.subtract(OFFSET, heightOffset, -widthOffset);
            case SOUTH:
                return position.subtract(widthOffset, heightOffset, OFFSET);
            case WEST:
                return position.subtract(-OFFSET, heightOffset, widthOffset);
        }
        return position;
    }

}
