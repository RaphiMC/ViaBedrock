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

import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;

import java.util.Objects;

public class BlockProperties {

    private final String name;
    private final CompoundTag properties;

    public BlockProperties(final String name, final CompoundTag properties) {
        this.name = name;
        this.properties = properties;
    }

    public BlockProperties(final BlockProperties blockProperties) {
        this.name = blockProperties.name;
        this.properties = blockProperties.properties.clone();
    }

    public String name() {
        return this.name;
    }

    public CompoundTag properties() {
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockProperties that = (BlockProperties) o;
        return Objects.equals(name, that.name) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, properties);
    }

    @Override
    public String toString() {
        return "BlockProperties{" +
                "name='" + name + '\'' +
                ", properties=" + properties +
                '}';
    }

}
