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
package net.raphimc.viabedrock.tool.generator;

import com.viaversion.viaversion.libs.gson.Gson;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.codegen.CodeGen;
import net.raphimc.viabedrock.codegen.model.Javadoc;
import net.raphimc.viabedrock.codegen.model.member.impl.Field;
import net.raphimc.viabedrock.codegen.model.type.impl.Enum;
import net.raphimc.viabedrock.tool.ToolArgs;
import net.raphimc.viabedrock.tool.ToolPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class BedrockDataEnumGenerator {

    public static void main(final String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final Path jsonDir = ToolPaths.protocolDocsDir(toolArgs);
        final Gson gson = new Gson();

        final CodeGen codeGen = new CodeGen(ToolPaths.MAIN_JAVA.toFile(), "net.raphimc.viabedrock.protocol.data.enums.bedrock.generated");

        final List<Path> jsonFiles;
        try (Stream<Path> files = Files.list(jsonDir)) {
            jsonFiles = files.filter(file -> file.getFileName().toString().endsWith(".json")).sorted().toList();
        }
        if (jsonFiles.isEmpty()) {
            throw new IllegalStateException("No enum definitions found in " + jsonDir);
        }

        for (Path file : jsonFiles) {
            final JsonObject jsonObject = gson.fromJson(Files.readString(file), JsonObject.class);

            if (!jsonObject.has("enum")) {
                continue;
            }

            final String enumName = jsonObject.get("title").getAsString()
                .replace("::", "_").replace(" ", "_").replace("-", "_");

            if (enumName.equalsIgnoreCase("LevelSoundEvent")) {
                // Skip this enum, we already have a custom implementation for it
                continue;
            }

            final Enum genEnum = new Enum(enumName);

            genEnum.imports().add("com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap");
            genEnum.imports().add("com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap");

            genEnum.members().add(new Field("private static final", "Int2ObjectMap<" + enumName + ">", "BY_VALUE", "new Int2ObjectOpenHashMap<>()"));
            genEnum.members().addStaticBlock(staticBlock -> {
                staticBlock.code().addForEach(enumName + " value", "values()", forEach -> {
                    forEach.code().addIf("!BY_VALUE.containsKey(value.value)", ifBlock -> {
                        ifBlock.code().add("BY_VALUE.put(value.value, value);");
                    });
                });
            });

            genEnum.members().addMethod("public static", enumName, "getByValue", method -> {
                method.parameters().add(new Field("final", "int", "value"));
                method.code().add("return BY_VALUE.get(value);");
            });
            genEnum.members().addMethod("public static", enumName, "getByValue", method -> {
                method.parameters().add(new Field("final", "int", "value"));
                method.parameters().add(new Field("final", enumName, "fallback"));
                method.code().add("return BY_VALUE.getOrDefault(value, fallback);");
            });
            genEnum.members().addMethod("public static", enumName, "getByName", method -> {
                method.parameters().add(new Field("final", "String", "name"));
                method.code().addForEach(enumName + " value", "values()", forEach -> {
                    forEach.code().addIf("value.name().equalsIgnoreCase(name)", ifBlock -> {
                        ifBlock.code().add("return value;");
                    });
                });
                method.code().add("return null;");
            });
            genEnum.members().addMethod("public static", enumName, "getByName", method -> {
                method.parameters().add(new Field("final", "String", "name"));
                method.parameters().add(new Field("final", enumName, "fallback"));
                method.code().addForEach(enumName + " value", "values()", forEach -> {
                    forEach.code().addIf("value.name().equalsIgnoreCase(name)", ifBlock -> {
                        ifBlock.code().add("return value;");
                    });
                });
                method.code().add("return fallback;");
            });

            genEnum.members().add(new Field("private final", "int", "value"));

            genEnum.members().addMethod(null, null, enumName, constructor -> {
                constructor.parameters().add(new Field("final", enumName, "value"));
                constructor.code().add("this(value.value);");
            });
            genEnum.members().addMethod(null, null, enumName, constructor -> {
                constructor.parameters().add(new Field("final", "int", "value"));
                constructor.code().add("this.value = value;");
            });

            genEnum.members().addMethod("public", "int", "getValue", method -> method.code().add("return this.value;"));

            final JsonArray enumFields = jsonObject.getAsJsonArray("enum");
            for (int i = 0; i < enumFields.size(); i++) {
                final JsonElement enumFieldElement = enumFields.get(i);
                final String name = enumFieldElement.getAsString().replace(" ", "_");
                String value = null;
                if (jsonObject.has("x-enum-binary-value")) {
                    final JsonArray binaryValues = jsonObject.getAsJsonArray("x-enum-binary-value");
                    if (binaryValues.size() > i) {
                        value = binaryValues.get(i).getAsString();
                    }
                }

                genEnum.enumFields().add(new Field(name, null, null, value, new Javadoc()));
            }
            codeGen.addType(genEnum);
        }

        codeGen.generate();
        System.out.println("Generated " + jsonFiles.size() + " enums from " + jsonDir);
    }

    private BedrockDataEnumGenerator() {
    }

}
