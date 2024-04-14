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
package net.raphimc.viabedrock.protocol.model;

import java.util.Objects;

public class EntityLink {

    private final long from;
    private final long to;
    private final short type; // TODO: Enum: Use ActorLinkType
    private final boolean immediate;
    private final boolean riderInitiated;

    public EntityLink(final long from, final long to, final short type, final boolean immediate, final boolean riderInitiated) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.immediate = immediate;
        this.riderInitiated = riderInitiated;
    }

    public long from() {
        return this.from;
    }

    public long to() {
        return this.to;
    }

    public short type() {
        return this.type;
    }

    public boolean immediate() {
        return this.immediate;
    }

    public boolean riderInitiated() {
        return this.riderInitiated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityLink that = (EntityLink) o;
        return from == that.from && to == that.to && type == that.type && immediate == that.immediate && riderInitiated == that.riderInitiated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, type, immediate, riderInitiated);
    }

    @Override
    public String toString() {
        return "EntityLink{" +
                "from=" + from +
                ", to=" + to +
                ", type=" + type +
                ", immediate=" + immediate +
                ", riderInitiated=" + riderInitiated +
                '}';
    }

}
