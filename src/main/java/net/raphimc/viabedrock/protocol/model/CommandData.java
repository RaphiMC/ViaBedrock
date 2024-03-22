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

import java.util.*;

public class CommandData {

    private final String name;
    private final String description;
    private final int flags;
    private final short permission;
    private final EnumData alias;
    private final OverloadData[] overloads;

    public CommandData(final String name, final String description, final int flags, final short permission, final EnumData alias, final OverloadData[] overloads) {
        this.name = name;
        this.description = description;
        this.flags = flags;
        this.permission = permission;
        this.alias = alias;
        this.overloads = overloads;
    }

    public String name() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    public int flags() {
        return this.flags;
    }

    public short permission() {
        return this.permission;
    }

    public EnumData alias() {
        return this.alias;
    }

    public OverloadData[] overloads() {
        return this.overloads;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandData that = (CommandData) o;
        return flags == that.flags && permission == that.permission && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(alias, that.alias) && Arrays.deepEquals(overloads, that.overloads);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, description, flags, permission, alias);
        result = 31 * result + Arrays.deepHashCode(overloads);
        return result;
    }

    @Override
    public String toString() {
        return "CommandData{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", flags=" + flags +
                ", permission=" + permission +
                ", alias=" + alias +
                ", overloads=" + Arrays.toString(overloads) +
                '}';
    }


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

    public static class SubCommandData {

        private final String name;
        private final Map<String, Integer> values;

        public SubCommandData(final String name, final Map<String, Integer> values) {
            this.name = name;
            this.values = values;
        }

        public String name() {
            return this.name;
        }

        public Map<String, Integer> values() {
            return this.values;
        }

        public void addValues(final Map<String, Integer> values) {
            this.values.putAll(values);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubCommandData that = (SubCommandData) o;
            return Objects.equals(name, that.name) && Objects.equals(values, that.values);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, values);
        }

        @Override
        public String toString() {
            return "SubCommandData{" +
                    "name='" + name + '\'' +
                    ", values=" + values +
                    '}';
        }

    }

    public static class OverloadData {

        private final boolean chaining;
        private final ParamData[] parameters;

        public OverloadData(final boolean chaining, final ParamData[] parameters) {
            this.chaining = chaining;
            this.parameters = parameters;
        }

        public boolean chaining() {
            return this.chaining;
        }

        public ParamData[] parameters() {
            return this.parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OverloadData that = (OverloadData) o;
            return chaining == that.chaining && Arrays.equals(parameters, that.parameters);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(chaining);
            result = 31 * result + Arrays.hashCode(parameters);
            return result;
        }

        @Override
        public String toString() {
            return "OverloadData{" +
                    "chaining=" + chaining +
                    ", parameters=" + Arrays.toString(parameters) +
                    '}';
        }

        public static class ParamData {

            private final String name;
            private final boolean optional;
            private final short flags;
            private final Integer type;
            private final EnumData enumData;
            private final SubCommandData subCommandData;
            private final String postfix;

            public ParamData(final String name, final boolean optional, final short flags, final Integer type, final EnumData enumData, final SubCommandData subCommandData, final String postfix) {
                this.name = name;
                this.optional = optional;
                this.flags = flags;
                this.type = type;
                this.enumData = enumData;
                this.subCommandData = subCommandData;
                this.postfix = postfix;
            }

            public String name() {
                return this.name;
            }

            public boolean optional() {
                return this.optional;
            }

            public short flags() {
                return this.flags;
            }

            public Integer type() {
                return this.type;
            }

            public EnumData enumData() {
                return this.enumData;
            }

            public SubCommandData subCommandData() {
                return this.subCommandData;
            }

            public String postfix() {
                return this.postfix;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ParamData paramData = (ParamData) o;
                return optional == paramData.optional && flags == paramData.flags && Objects.equals(name, paramData.name) && Objects.equals(type, paramData.type) && Objects.equals(enumData, paramData.enumData) && Objects.equals(subCommandData, paramData.subCommandData) && Objects.equals(postfix, paramData.postfix);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, optional, flags, type, enumData, subCommandData, postfix);
            }

            @Override
            public String toString() {
                return "ParamData{" +
                        "name='" + name + '\'' +
                        ", optional=" + optional +
                        ", flags=" + flags +
                        ", type=" + type +
                        ", enumData=" + enumData +
                        ", subCommandData=" + subCommandData +
                        ", postfix='" + postfix + '\'' +
                        '}';
            }

        }

    }

}
