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
package net.raphimc.viabedrock.codegen;

import net.raphimc.viabedrock.codegen.model.type.Type;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class CodeGen {

    private static final String HEADER = "// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!";

    private final File outputDirectory;
    private final String packageName;
    private final List<Type> types;

    public CodeGen(final File sourceSetDirectory, final String packageName) {
        this.outputDirectory = new File(sourceSetDirectory, packageName.replace('.', '/'));
        this.packageName = packageName;
        this.types = new ArrayList<>();
    }

    public void addType(final Type type) {
        this.types.add(type);
    }

    public void generate() throws IOException {
        if (this.outputDirectory.isDirectory()) {
            Files.walkFileTree(this.outputDirectory.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        this.outputDirectory.mkdirs();

        for (Type type : this.types) {
            final List<String> code = new ArrayList<>();
            code.add(HEADER);
            code.add("package " + this.packageName + ";");
            code.add("");
            code.addAll(type.generateCode());
            code.add("");
            Files.writeString(new File(this.outputDirectory, type.name() + ".java").toPath(), String.join("\n", code));
        }
    }

}
