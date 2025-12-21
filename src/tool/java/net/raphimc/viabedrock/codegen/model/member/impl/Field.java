/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.codegen.model.member.impl;

import net.raphimc.viabedrock.codegen.model.Javadoc;
import net.raphimc.viabedrock.codegen.model.member.Member;

import java.util.ArrayList;
import java.util.List;

public record Field(String name, String type, String access, String value, Javadoc javadoc) implements Member {

    public Field(final String name) {
        this(name, null);
    }

    public Field(final String name, final String value) {
        this(name, null, null, value, new Javadoc());
    }

    public Field(final String access, final String type, final String name) {
        this(access, type, name, null);
    }

    public Field(final String access, final String type, final String name, final String value) {
        this(name, type, access, value, new Javadoc());
    }

    @Override
    public List<String> generateCode() {
        final List<String> lines = new ArrayList<>();
        lines.addAll(this.javadoc.generateCode());
        lines.add(this.access + " " + this.type + " " + this.name + (this.value != null ? " = " + this.value : "") + ";");
        return lines;
    }

}
