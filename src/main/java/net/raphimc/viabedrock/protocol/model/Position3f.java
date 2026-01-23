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
package net.raphimc.viabedrock.protocol.model;

import com.viaversion.viaversion.api.minecraft.BlockFace;

public record Position3f(float x, float y, float z) {

    public static final Position3f ZERO = new Position3f(0, 0, 0);

    public Position3f getRelative(final BlockFace face) {
        return new Position3f(this.x + face.modX(), this.y + face.modY(), this.z + face.modZ());
    }

    public Position3f add(final float x, final float y, final float z) {
        return new Position3f(this.x + x, this.y + y, this.z + z);
    }

    public Position3f add(final Position3f position) {
        return new Position3f(this.x + position.x, this.y + position.y, this.z + position.z);
    }

    public Position3f subtract(final float x, final float y, final float z) {
        return new Position3f(this.x - x, this.y - y, this.z - z);
    }

    public Position3f subtract(final Position3f position) {
        return new Position3f(this.x - position.x, this.y - position.y, this.z - position.z);
    }

    public float distanceTo(final Position3f position) {
        return (float) Math.sqrt(Math.pow(this.x - position.x, 2) + Math.pow(this.y - position.y, 2) + Math.pow(this.z - position.z, 2));
    }

}
