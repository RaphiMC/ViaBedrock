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

import net.raphimc.viabedrock.protocol.data.enums.bedrock.CommandRegistry_HardNonTerminal;

import java.util.*;

public record CommandData(String name, String description, int flags, short permission, EnumData alias, OverloadData[] overloads) {

    public static class EnumData {

        private final String name;
        private final Map<String, Set<Short>> values;
        private final boolean dynamic;

        public EnumData(final String name, final Set<String> values, final boolean dynamic) {
            this.name = name;
            this.values = new HashMap<>();
            for (final String value : values) {
                this.values.put(value, new HashSet<>());
            }
            this.dynamic = dynamic;
        }

        public String name() {
            return this.name;
        }

        public Map<String, Set<Short>> values() {
            return this.values;
        }

        public void addValues(final Set<String> values) {
            for (final String value : values) {
                this.values.put(value, new HashSet<>());
            }
        }

        public void removeValues(final Set<String> values) {
            for (final String value : values) {
                this.values.remove(value);
            }
        }

        public boolean dynamic() {
            return this.dynamic;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EnumData enumData = (EnumData) o;
            return dynamic == enumData.dynamic && Objects.equals(name, enumData.name) && Objects.equals(values, enumData.values);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, values, dynamic);
        }

        @Override
        public String toString() {
            return "EnumData{" +
                    "name='" + name + '\'' +
                    ", values=" + values +
                    ", dynamic=" + dynamic +
                    '}';
        }

    }

    public record SubCommandData(String name, Map<String, Integer> values) {

        public void addValues(final Map<String, Integer> values) {
            this.values.putAll(values);
        }

    }

    public record OverloadData(boolean chaining, CommandData.OverloadData.ParamData[] parameters) {

        public record ParamData(String name, boolean optional, short flags, CommandRegistry_HardNonTerminal type, EnumData enumData, SubCommandData subCommandData,
                                String postfix) {
        }

    }

}
