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
import net.raphimc.viabedrock.codegen.model.instruction.impl.block.Block;
import net.raphimc.viabedrock.codegen.model.member.Member;

import java.util.ArrayList;
import java.util.List;

public record Method(String name, String type, String access, List<Field> parameters, Block code, Javadoc javadoc) implements Member {

    public Method(final String access, final String type, final String name) {
        this(name, type, access, new ArrayList<>(), new Block(), new Javadoc());
    }

    @Override
    public List<String> generateCode() {
        final StringBuilder methodSignatureBuilder = new StringBuilder();
        if (this.access != null) {
            methodSignatureBuilder.append(this.access).append(' ');
        }
        if (this.type != null) {
            methodSignatureBuilder.append(this.type).append(' ');
        }
        methodSignatureBuilder.append(this.name).append('(');
        for (Field parameter : this.parameters) {
            if (parameter.access() != null) {
                methodSignatureBuilder.append(parameter.access()).append(' ');
            }
            methodSignatureBuilder.append(parameter.type()).append(' ').append(parameter.name()).append(", ");
        }
        if (!this.parameters.isEmpty()) {
            methodSignatureBuilder.setLength(methodSignatureBuilder.length() - 2);
        }
        methodSignatureBuilder.append(") ");

        final List<String> codeLines = this.code.generateCode();
        codeLines.set(0, methodSignatureBuilder + codeLines.get(0));

        final List<String> lines = new ArrayList<>();
        lines.addAll(this.javadoc.generateCode());
        lines.addAll(codeLines);
        return lines;
    }

}
