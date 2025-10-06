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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.engine.resolver.MapResolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

public class BedrockDataEnumGenerator {

    private static final String ENUMS_URL = "https://raw.githubusercontent.com/Mojang/bedrock-protocol-docs/2a42b85946a308d830984f54ea20349aa2beaeed/html/enums.html";
    private static final String ENUMS_PACKAGE = "net.raphimc.viabedrock.protocol.data.enums.bedrock.generated";
    private static final List<String> IGNORED_FIELDS = Arrays.asList("count", "_count", "total", "all", "numenchantments", "numtagtypes", "abilitycount", "nummodes", "input_num", "9800", "total_operations", "total_operands");
    private static final Map<String, String> VALUE_REPLACEMENTS = new HashMap<>();

    static {
        VALUE_REPLACEMENTS.put("std::numeric_limits::max()", "Integer.MAX_VALUE");
        VALUE_REPLACEMENTS.put("std::numeric_limits<uint32_t>::max()", "Integer.MAX_VALUE");
        VALUE_REPLACEMENTS.put("std::numeric_limits::min()", "Integer.MIN_VALUE");
        VALUE_REPLACEMENTS.put("std::numeric_limits<uint32_t>::min()", "Integer.MIN_VALUE");
        VALUE_REPLACEMENTS.put("NonTerminalBit", "0");
    }

    public static void main(String[] args) throws Throwable {
        final File sourceDir = new File("../src/main/java");
        final File enumsDir = new File(sourceDir, ENUMS_PACKAGE.replace(".", "/"));
        if (enumsDir.isDirectory()) {
            Files.walkFileTree(enumsDir.toPath(), new SimpleFileVisitor<>() {
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
        enumsDir.mkdirs();

        final Map<String, List<EnumField>> enums = new HashMap<>();
        final Document doc = Jsoup.parse(new URL(ENUMS_URL), 10_000);
        for (Element element : doc.selectXpath("/html/body/table/tbody/tr")) {
            final Elements tableElements = element.select("td");
            if (tableElements.isEmpty()) continue;

            final String enumName = tableElements.get(0).ownText();
            final List<EnumField> enumFields = new ArrayList<>();
            final Set<String> enumFieldNames = new HashSet<>();
            final Map<String, String> enumValues = new HashMap<>();
            for (Element fieldTableRowElement : tableElements.get(1).selectXpath("table/tbody/tr")) {
                final Elements valueElements = fieldTableRowElement.select("td");
                final String fieldName = valueElements.get(0).ownText();
                final String fieldValue = valueElements.get(1).ownText();
                final List<String> fieldComments = Arrays.stream(valueElements.get(2).wholeOwnText().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
                enumFields.add(new EnumField(fieldName, fieldValue, fieldComments));
                enumFieldNames.add(fieldName);
                enumValues.put(fieldName, fieldValue);
            }
            enums.put(enumName, enumFields);

            enumFields.removeIf(field -> IGNORED_FIELDS.contains(field.name.toLowerCase(Locale.ROOT)));
            enumFields.removeIf(field -> field.name.toLowerCase(Locale.ROOT).contains("deprecated"));
            for (EnumField field : enumFields) {
                if (VALUE_REPLACEMENTS.containsKey(field.value)) {
                    field.value = VALUE_REPLACEMENTS.get(field.value);
                }

                final String[] splitChars = {"|", "-"};
                for (String splitChar : splitChars) {
                    if (field.value.contains(splitChar) && !field.value.startsWith(splitChar)) {
                        final String[] split = field.value.split(Pattern.quote(splitChar));
                        for (int i = 0; i < split.length; i++) {
                            String part = split[i].trim();
                            if (enumValues.containsKey(part)) {
                                if (enumFieldNames.contains(part) && !IGNORED_FIELDS.contains(part.toLowerCase(Locale.ROOT))) {
                                    part += ".getValue()";
                                } else {
                                    part = enumValues.get(part);
                                }
                            }
                            split[i] = part;
                        }
                        field.value = String.join(" " + splitChar + " ", split);
                    }
                }
                if (enumValues.containsKey(field.value)) {
                    if (enumFieldNames.contains(field.value) && !IGNORED_FIELDS.contains(field.value.toLowerCase(Locale.ROOT))) {
                        field.value += ".getValue()";
                    } else {
                        field.value = enumValues.get(field.value);
                    }
                }
            }
        }

        // Mojang seems to have pushed an outdated version of this enum. Those values exist in 1.21.50, but not in 1.21.60
        final List<EnumField> actorDamageCauseEnum = enums.get("ActorDamageCause");
        actorDamageCauseEnum.add(new EnumField("MaceSmash", "34"));

        // Mojang seems to have pushed an outdated version of this enum. Those values exist in 1.21.50, but not in 1.21.60
        final List<EnumField> levelEventEnum = enums.get("LevelEvent");
        levelEventEnum.add(new EnumField("ParticleCreakingHeartTrail", "9816"));

        // Fix wrong values
        final List<EnumField> packetCompressionAlgorithmEnum = enums.get("PacketCompressionAlgorithm");
        packetCompressionAlgorithmEnum.removeIf(field -> field.name.equals("None"));
        packetCompressionAlgorithmEnum.add(new EnumField("None", "0xFF"));

        final MustacheEngine mustacheEngine = MustacheEngineBuilder.newBuilder()
                .addTemplateLocator(ClassPathTemplateLocator.builder().setSuffix("mustache").build())
                .setProperty(EngineConfigurationKey.SKIP_VALUE_ESCAPING, true)
                .addResolver(new MapResolver())
                .build();
        final Mustache enumTemplate = mustacheEngine.getMustache("enum");

        for (Map.Entry<String, List<EnumField>> entry : enums.entrySet()) {
            final String enumPackage;
            final String enumName;
            if (Character.isLowerCase(entry.getKey().charAt(0)) && entry.getKey().contains("::")) {
                enumPackage = ENUMS_PACKAGE + "." + entry.getKey().substring(0, entry.getKey().indexOf("::"));
                enumName = entry.getKey().substring(entry.getKey().indexOf("::") + 2);
            } else {
                enumPackage = ENUMS_PACKAGE;
                enumName = entry.getKey().replace("::", "_");
            }
            final File enumDir = new File(sourceDir, enumPackage.replace(".", "/"));
            enumDir.mkdirs();
            final File enumFile = new File(enumDir, enumName + ".java");

            final Map<String, Object> variables = new HashMap<>();
            variables.put("packageName", enumPackage);
            variables.put("enumName", enumName);
            variables.put("values", entry.getValue());
            try (final FileWriter writer = new FileWriter(enumFile)) {
                enumTemplate.render(writer, variables);
            }
        }
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
