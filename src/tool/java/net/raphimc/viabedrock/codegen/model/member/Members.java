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
package net.raphimc.viabedrock.codegen.model.member;

import net.raphimc.viabedrock.codegen.model.member.impl.Method;
import net.raphimc.viabedrock.codegen.model.member.impl.StaticBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record Members(List<Member> members) {

    public Members() {
        this(new ArrayList<>());
    }

    public void add(final Member member) {
        this.members.add(member);
    }

    public void addMethod(final String access, final String type, final String name, final Consumer<Method> consumer) {
        final Method method = new Method(access, type, name);
        consumer.accept(method);
        this.members.add(method);
    }

    public void addStaticBlock(final Consumer<StaticBlock> consumer) {
        final StaticBlock staticBlock = new StaticBlock();
        consumer.accept(staticBlock);
        this.members.add(staticBlock);
    }

    public List<String> generateCode() {
        final List<String> lines = new ArrayList<>();
        for (Member member : this.members) {
            lines.addAll(member.generateCode());
            lines.add("");
        }
        if (!lines.isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

}
