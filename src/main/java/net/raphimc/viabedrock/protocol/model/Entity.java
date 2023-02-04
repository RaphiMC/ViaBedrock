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
package net.raphimc.viabedrock.protocol.model;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_3Types;

import java.util.concurrent.atomic.AtomicInteger;

public class Entity {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final long uniqueId;
    private final long runtimeId;
    private final int javaId;
    private final Entity1_19_3Types type;

    public Entity(final long uniqueId, final long runtimeId, final Entity1_19_3Types type) {
        this.uniqueId = uniqueId;
        this.runtimeId = runtimeId;
        this.javaId = ID_COUNTER.getAndIncrement();
        this.type = type;
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

}
