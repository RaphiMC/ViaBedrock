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
package net.raphimc.viabedrock.codegen.model;

import java.util.ArrayList;
import java.util.List;

public record Javadoc(List<String> lines) {

    public Javadoc() {
        this(new ArrayList<>());
    }

    public List<String> generateCode() {
        if (this.lines.isEmpty()) {
            return new ArrayList<>();
        }

        final List<String> lines = new ArrayList<>();
        lines.add("/**");
        for (String line : this.lines) {
            lines.add(" * " + line + "<br>");
        }
        lines.add(" */");

        // Remove the <br> from the last line
        final String lastLine = lines.get(lines.size() - 2);
        lines.set(lines.size() - 2, lastLine.substring(0, lastLine.length() - 4));

        return lines;
    }

}
