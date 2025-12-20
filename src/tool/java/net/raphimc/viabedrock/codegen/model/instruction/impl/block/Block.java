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
package net.raphimc.viabedrock.codegen.model.instruction.impl.block;

import net.raphimc.viabedrock.codegen.model.instruction.Instruction;
import net.raphimc.viabedrock.codegen.model.instruction.impl.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.raphimc.viabedrock.codegen.util.StringUtil.indent;

public record Block(List<Instruction> instructions) implements Instruction {

    public Block() {
        this(new ArrayList<>());
    }

    public void add(final String content) {
        this.instructions.add(new Statement(content));
    }

    public void addBlock(final Consumer<Block> consumer) {
        final Block block = new Block();
        consumer.accept(block);
        this.instructions.add(block);
    }

    public void addFor(final String initialization, final String condition, final String update, final Consumer<For> consumer) {
        final For forInstruction = new For(initialization, condition, update);
        consumer.accept(forInstruction);
        this.instructions.add(forInstruction);
    }

    public void addForEach(final String variable, final String iterable, final Consumer<ForEach> consumer) {
        final ForEach forEachInstruction = new ForEach(variable, iterable);
        consumer.accept(forEachInstruction);
        this.instructions.add(forEachInstruction);
    }

    public void addIf(final String condition, final Consumer<If> consumer) {
        final If ifInstruction = new If(condition);
        consumer.accept(ifInstruction);
        this.instructions.add(ifInstruction);
    }

    @Override
    public List<String> generateCode() {
        final List<String> lines = new ArrayList<>();
        lines.add("{");
        for (Instruction instruction : this.instructions) {
            lines.addAll(indent(instruction.generateCode()));
        }
        lines.add("}");
        return lines;
    }

}
