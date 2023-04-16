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

import java.util.*;

public class CommandData {

    public static final int FLAG_TEST_USAGE = 1;
    public static final int FLAG_HIDDEN_FROM_COMMAND_BLOCK = 2;
    public static final int FLAG_HIDDEN_FROM_PLAYER = 4;
    public static final int FLAG_HIDDEN_FROM_AUTOMATION = 8;
    public static final int FLAG_LOCAL_SYNC = 16;
    public static final int FLAG_EXECUTE_DISALLOWED = 32;
    public static final int FLAG_MESSAGE_TYPE = 64;
    public static final int FLAG_NOT_CHEAT = 128;
    public static final int FLAG_ASYNC = 256;

    private final String name;
    private final String description;
    private final int flags;
    private final short permission;
    private final EnumData alias;
    private final ParamData[][] parameters;

    public CommandData(final String name, final String description, final int flags, final short permission, final EnumData alias, final ParamData[][] parameters) {
        this.name = name;
        this.description = description;
        this.flags = flags;
        this.permission = permission;
        this.alias = alias;
        this.parameters = parameters;
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

    public ParamData[][] parameters() {
        return this.parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandData that = (CommandData) o;
        return flags == that.flags && permission == that.permission && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(alias, that.alias) && Arrays.deepEquals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, description, flags, permission, alias);
        result = 31 * result + Arrays.deepHashCode(parameters);
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
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }


    public static class EnumData {

        public static final short FLAG_CHEATS_ENABLED = 0;
        public static final short FLAG_OPERATOR_PERMISSIONS = 1;
        public static final short FLAG_HOST_PERMISSIONS = 2;
        public static final short FLAG_ALLOW_ALIASES = 3;

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

    public static class ParamData {

        public static final int TYPE_INT = 1;
        public static final int TYPE_FLOAT1 = 2;
        public static final int TYPE_FLOAT2 = 3;
        public static final int TYPE_VALUE = 4;
        public static final int TYPE_WILDCARD_INT = 5;
        public static final int TYPE_OPERATOR = 6;
        public static final int TYPE_COMPARE_OPERATOR = 7;
        public static final int TYPE_TARGET1 = 8;
        public static final int TYPE_TARGET2 = 10;
        public static final int TYPE_FILE_PATH = 17;
        public static final int TYPE_INT_RANGE = 23;
        public static final int TYPE_EQUIPMENT_SLOT = 38;
        public static final int TYPE_STRING = 39;
        public static final int TYPE_BLOCK_POSITION = 47;
        public static final int TYPE_POSITION = 48;
        public static final int TYPE_MESSAGE = 51;
        public static final int TYPE_TEXT = 53;
        public static final int TYPE_JSON = 57;
        public static final int TYPE_BLOCK_STATES = 67;
        public static final int TYPE_COMMAND = 70;

        public static final short FLAG_SUPPRESS_ENUM_AUTOCOMPLETION = 1;
        public static final short FLAG_HAS_SEMANTIC_CONSTRAINT = 2;
        public static final short FLAG_ENUM_AS_CHAINED_COMMAND = 4;

        private final String name;
        private final boolean optional;
        private final short flags;
        private final Integer type;
        private final EnumData enumData;
        private final String postfix;

        public ParamData(final String name, final boolean optional, final short flags, final Integer type, final EnumData enumData, final String postfix) {
            this.name = name;
            this.optional = optional;
            this.flags = flags;
            this.type = type;
            this.enumData = enumData;
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

        public String postfix() {
            return this.postfix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParamData paramData = (ParamData) o;
            return optional == paramData.optional && flags == paramData.flags && Objects.equals(name, paramData.name) && Objects.equals(type, paramData.type) && Objects.equals(enumData, paramData.enumData) && Objects.equals(postfix, paramData.postfix);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, optional, flags, type, enumData, postfix);
        }

        @Override
        public String toString() {
            return "ParamData{" +
                    "name='" + name + '\'' +
                    ", optional=" + optional +
                    ", flags=" + flags +
                    ", type=" + type +
                    ", enumData=" + enumData +
                    ", postfix='" + postfix + '\'' +
                    '}';
        }

    }

}
