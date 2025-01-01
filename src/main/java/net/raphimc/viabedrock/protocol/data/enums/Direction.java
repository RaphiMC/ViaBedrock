/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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

import com.viaversion.viaversion.api.minecraft.BlockFace;

public enum Direction {

    DOWN(0, -1, BlockFace.BOTTOM),
    UP(1, -1, BlockFace.TOP),
    NORTH(2, 2, BlockFace.NORTH),
    SOUTH(3, 0, BlockFace.SOUTH),
    WEST(4, 1, BlockFace.WEST),
    EAST(5, 3, BlockFace.EAST);

    private final int verticalId;
    private final int horizontalId;
    private final BlockFace blockFace;

    Direction(final int verticalId, final int horizontalId, final BlockFace blockFace) {
        this.verticalId = verticalId;
        this.horizontalId = horizontalId;
        this.blockFace = blockFace;
    }

    public static Direction getFromVerticalId(final int verticalId, final Direction fallback) {
        final Direction direction = getFromVerticalId(verticalId);
        return direction == null ? fallback : direction;
    }

    public static Direction getFromVerticalId(final int verticalId) {
        for (final Direction direction : values()) {
            if (direction.verticalId == verticalId) {
                return direction;
            }
        }

        return null;
    }

    public static Direction getFromHorizontalId(final int horizontalId, final Direction fallback) {
        final Direction direction = getFromHorizontalId(horizontalId);
        return direction == null ? fallback : direction;
    }

    public static Direction getFromHorizontalId(final int horizontalId) {
        if (horizontalId == -1) return null;

        for (final Direction direction : values()) {
            if (direction.horizontalId == horizontalId) {
                return direction;
            }
        }

        return null;
    }

    public boolean isHorizontal() {
        return this.horizontalId != -1;
    }

    public int verticalId() {
        return this.verticalId;
    }

    public int horizontalId() {
        return this.horizontalId;
    }

    public BlockFace blockFace() {
        return this.blockFace;
    }

}
