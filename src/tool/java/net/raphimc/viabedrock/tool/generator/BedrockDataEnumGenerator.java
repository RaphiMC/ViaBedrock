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

import net.raphimc.viabedrock.codegen.CodeGen;
import net.raphimc.viabedrock.codegen.model.Javadoc;
import net.raphimc.viabedrock.codegen.model.member.impl.Field;
import net.raphimc.viabedrock.codegen.model.type.impl.Enum;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class BedrockDataEnumGenerator {

    private static final String ENUMS_URL = "https://raw.githubusercontent.com/Mojang/bedrock-protocol-docs/2842b5c80249dc3a125b6892031cfc2fb1e4c1bd/html/enums.html";
    private static final List<String> IGNORED_FIELDS = Arrays.asList("deprecated", "count", "_count", "total", "all", "numenchantments", "numtagtypes", "abilitycount", "nummodes", "input_num", "total_operations", "total_operands", "numvalidversions", "num_categories");
    private static final Map<String, String> VALUE_REPLACEMENTS = new HashMap<>();
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");

    static {
        VALUE_REPLACEMENTS.put("std::numeric_limits::max()", "Integer.MAX_VALUE");
        VALUE_REPLACEMENTS.put("std::numeric_limits<uint32_t>::max()", "Integer.MAX_VALUE");
        VALUE_REPLACEMENTS.put("std::numeric_limits::min()", "Integer.MIN_VALUE");
        VALUE_REPLACEMENTS.put("std::numeric_limits<uint32_t>::min()", "Integer.MIN_VALUE");
        VALUE_REPLACEMENTS.put("NonTerminalBit", "0");
    }

    public static void main(String[] args) throws Throwable {
        final Map<String, List<EnumField>> enums = new HashMap<>();
        final Document doc = Jsoup.parse(new URL(ENUMS_URL), 10_000);
        for (Element element : doc.selectXpath("/html/body/table/tbody/tr")) {
            final Elements tableElements = element.select("td");
            if (tableElements.isEmpty()) continue;

            final String enumName = tableElements.get(0).ownText();
            final Map<String, EnumField> enumFields = new LinkedHashMap<>();
            for (Element fieldTableRowElement : tableElements.get(1).selectXpath("table/tbody/tr")) {
                final Elements valueElements = fieldTableRowElement.select("td");
                final String fieldName = valueElements.get(0).ownText();
                final String fieldValue = valueElements.get(1).ownText();
                final List<String> fieldComments = Arrays.stream(valueElements.get(2).wholeOwnText().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
                enumFields.put(fieldName, new EnumField(fieldName, fieldValue, fieldComments));
            }

            final Map<String, EnumField> originalEnumFields = new LinkedHashMap<>(enumFields);
            enumFields.values().removeIf(field -> IGNORED_FIELDS.contains(field.name.toLowerCase(Locale.ROOT)));
            enumFields.values().removeIf(field -> NUMBER_PATTERN.matcher(field.name).matches());
            for (EnumField field : enumFields.values()) {
                if (VALUE_REPLACEMENTS.containsKey(field.value)) {
                    field.value = VALUE_REPLACEMENTS.get(field.value);
                }
            }
            final Map<String, EnumField> valueToFieldMap = new HashMap<>();
            for (EnumField field : enumFields.values()) {
                if (valueToFieldMap.containsKey(field.value)) {
                    field.value = valueToFieldMap.get(field.value).name;
                } else {
                    valueToFieldMap.put(field.value, field);
                }
            }
            for (EnumField field : enumFields.values()) {
                if (!enumFields.containsKey(field.value) && originalEnumFields.containsKey(field.value) && !NUMBER_PATTERN.matcher(field.value).matches()) {
                    field.value = originalEnumFields.get(field.value).value;
                }
            }
            enums.put(enumName, new ArrayList<>(enumFields.values()));
        }

        // Mojang seems to have pushed an outdated version of this enum. Those values exist in 1.21.50, but not in 1.21.60
        final List<EnumField> levelEventEnum = enums.get("LevelEvent");
        levelEventEnum.add(new EnumField("ParticleCreakingHeartTrail", "9816"));

        // Fix wrong values
        final List<EnumField> packetCompressionAlgorithmEnum = enums.get("PacketCompressionAlgorithm");
        packetCompressionAlgorithmEnum.removeIf(field -> field.name.equals("None"));
        packetCompressionAlgorithmEnum.add(new EnumField("None", "0xFF"));

        final CodeGen codeGen = new CodeGen(new File("../src/main/java"), "net.raphimc.viabedrock.protocol.data.enums.bedrock.generated");

        for (Map.Entry<String, List<EnumField>> enumEntry : enums.entrySet()) {
            String rawEnumName = enumEntry.getKey().replace("::", "_");
            if (Character.isLowerCase(rawEnumName.charAt(0))) {
                rawEnumName = Character.toUpperCase(rawEnumName.charAt(0)) + rawEnumName.substring(1);
            }
            final String enumName = rawEnumName;
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

            for (EnumField enumField : enumEntry.getValue()) {
                genEnum.enumFields().add(new Field(enumField.getName(), null, null, enumField.getValue(), new Javadoc(enumField.getComments())));
            }
            codeGen.addType(genEnum);
        }

        codeGen.generate();
    }

    private static class EnumField {

        private final String name;
        private String value;
        private final List<String> comments;

        public EnumField(final String name, final String value) {
            this(name, value, Collections.emptyList());
        }

        public EnumField(final String name, final String value, final List<String> comments) {
            this.name = name;
            this.value = value;
            this.comments = comments;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public List<String> getComments() {
            return this.comments;
        }

    }

}
