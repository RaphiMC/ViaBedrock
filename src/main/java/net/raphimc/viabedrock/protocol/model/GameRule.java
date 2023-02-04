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

import java.util.Objects;

public class GameRule<T> {

    private final String name;
    private final boolean editable;
    private final T value;

    public GameRule(final String name, final boolean editable, final T value) {
        this.name = name;
        this.editable = editable;
        this.value = value;
    }

    public GameRule(final GameRule<T> gameRule) {
        this.name = gameRule.name;
        this.editable = gameRule.editable;
        this.value = gameRule.value;
    }

    public String name() {
        return this.name;
    }

    public boolean editable() {
        return this.editable;
    }

    public T value() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameRule<?> gameRule = (GameRule<?>) o;
        return editable == gameRule.editable && Objects.equals(name, gameRule.name) && Objects.equals(value, gameRule.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, editable, value);
    }

    @Override
    public String toString() {
        return "GameRule{" +
                "name='" + name + '\'' +
                ", editable=" + editable +
                ", value=" + value +
                '}';
    }

}
