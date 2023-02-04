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

public class Experiment {

    private final String name;
    private final boolean enabled;

    public Experiment(final String name, final boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public Experiment(final Experiment experiment) {
        this.name = experiment.name;
        this.enabled = experiment.enabled;
    }

    public String name() {
        return this.name;
    }

    public boolean enabled() {
        return this.enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Experiment that = (Experiment) o;
        return enabled == that.enabled && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, enabled);
    }

    @Override
    public String toString() {
        return "Experiment{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                '}';
    }

}
