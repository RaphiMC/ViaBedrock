/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol.model.entity;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_3Types;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;

public class Entity {

    protected final long uniqueId;
    protected final long runtimeId;
    protected final int javaId;
    protected final Entity1_19_3Types type;

    /**
     * x, y, z
     */
    protected Position3f position;

    /**
     * pitch, yaw, headYaw
     */
    protected Position3f rotation;

    protected boolean onGround;

    public Entity(final long uniqueId, final long runtimeId, final int javaId, final Entity1_19_3Types type) {
        this.uniqueId = uniqueId;
        this.runtimeId = runtimeId;
        this.javaId = javaId;
        this.type = type;
    }

    public void tick(final EntityTracker entityTracker) throws Exception {
    }

    public long uniqueId() {
        return this.uniqueId;
    }

    public long runtimeId() {
        return this.runtimeId;
    }

    public int javaId() {
        return this.javaId;
    }

    public Entity1_19_3Types type() {
        return this.type;
    }

    public Position3f position() {
        return this.position;
    }

    public void setPosition(final Position3f position) {
        this.position = position;
    }

    public Position3f rotation() {
        return this.rotation;
    }

    public void setRotation(final Position3f rotation) {
        this.rotation = rotation;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setOnGround(final boolean onGround) {
        this.onGround = onGround;
    }

}