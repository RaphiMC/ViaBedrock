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
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.codegen.CodeGen;
import net.raphimc.viabedrock.codegen.model.Javadoc;
import net.raphimc.viabedrock.codegen.model.member.impl.Field;
import net.raphimc.viabedrock.codegen.model.type.impl.Enum;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class BedrockDataEnumGenerator {

    public static void main(String[] args) throws Throwable {
        // Clone https://github.com/EndstoneMC/protocol-docs/
        final File jsonDir =  new File("/home/exterminate/Projects/Minecraft/protocol-docs/enums/");
        final Gson gson = new Gson();

        final CodeGen codeGen = new CodeGen(new File("src/main/java"), "net.raphimc.viabedrock.protocol.data.enums.bedrock.generated");

        for (File file : jsonDir.listFiles()) {
            if(!file.getName().endsWith(".json")) continue;

            final JsonObject jsonObject = gson.fromJson(Files.readString(file.toPath()), JsonObject.class);
            final String enumName = jsonObject.get("name").getAsString().replace("::", "_");

            if (enumName.equalsIgnoreCase("SharedTypes_Legacy_LevelSoundEvent")) {
                // Skip this enum, we already have a custom implementation for it
                continue;
            }

            final Enum genEnum = new Enum(enumName);

            genEnum.imports().add("com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap");
            genEnum.imports().add("com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap");

            genEnum.members().add(new Field("private static final", "Int2ObjectMap<" + enumName + ">", "BY_VALUE", "new Int2ObjectOpenHashMap<>()"));
            genEnum.members().addStaticBlock(staticBlock -> {
                staticBlock.code().addForEach(enumName + " value", "values()", forEach -> {
                    forEach.code().addIf("!BY_VALUE.containsKey(value.value)", _if -> {
                        _if.code().add("BY_VALUE.put(value.value, value);");
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
                    forEach.code().addIf("value.name().equalsIgnoreCase(name)", _if -> {
                        _if.code().add("return value;");
                    });
                });
                method.code().add("return null;");
            });
            genEnum.members().addMethod("public static", enumName, "getByName", method -> {
                method.parameters().add(new Field("final", "String", "name"));
                method.parameters().add(new Field("final", enumName, "fallback"));
                method.code().addForEach(enumName + " value", "values()", forEach -> {
                    forEach.code().addIf("value.name().equalsIgnoreCase(name)", _if -> {
                        _if.code().add("return value;");
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

            for (JsonElement enumFieldElement : jsonObject.getAsJsonArray("values")) {
                JsonObject object = enumFieldElement.getAsJsonObject();
                String name  = object.get("name").getAsString().replace(" ", "_");
                genEnum.enumFields().add(new Field(name, null, null, object.get("value").getAsString(), new Javadoc()));
            }
            codeGen.addType(genEnum);
        }

        codeGen.generate();

    }

}
