/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.codegen.model.instruction.impl.block;

import net.raphimc.viabedrock.codegen.model.instruction.Instruction;

import java.util.List;

public record For(String initialization, String condition, String update, Block code) implements Instruction {

    public For(final String initialization, final String condition, final String update) {
        this(initialization, condition, update, new Block());
    }

    @Override
    public List<String> generateCode() {
        final StringBuilder forBuilder = new StringBuilder();
        forBuilder.append("for ");
        if (this.initialization != null) {
            forBuilder.append(this.initialization);
        }
        forBuilder.append("; ");
        if (this.condition != null) {
            forBuilder.append(this.condition);
        }
        forBuilder.append("; ");
        if (this.update != null) {
            forBuilder.append(this.update);
        }
        forBuilder.append(") ");

        final List<String> lines = this.code.generateCode();
        lines.set(0, forBuilder + lines.get(0));
        return lines;
    }

}
