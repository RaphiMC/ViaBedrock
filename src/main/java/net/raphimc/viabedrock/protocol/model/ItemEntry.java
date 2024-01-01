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

public class ItemEntry {

    private final String identifier;
    private final int id;
    private final boolean componentBased;

    public ItemEntry(String identifier, int id, boolean componentBased) {
        this.identifier = identifier;
        this.id = id;
        this.componentBased = componentBased;
    }

    public ItemEntry(ItemEntry itemEntry) {
        this.identifier = itemEntry.identifier;
        this.id = itemEntry.id;
        this.componentBased = itemEntry.componentBased;
    }

    public String identifier() {
        return this.identifier;
    }

    public int id() {
        return this.id;
    }

    public boolean componentBased() {
        return this.componentBased;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemEntry itemEntry = (ItemEntry) o;
        return id == itemEntry.id && componentBased == itemEntry.componentBased && Objects.equals(identifier, itemEntry.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, id, componentBased);
    }

    @Override
    public String toString() {
        return "ItemEntry{" +
                "identifier='" + identifier + '\'' +
                ", id=" + id +
                ", componentBased=" + componentBased +
                '}';
    }

}
