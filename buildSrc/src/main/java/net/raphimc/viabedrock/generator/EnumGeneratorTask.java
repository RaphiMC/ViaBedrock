/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.generator;

import net.raphimc.viabedrock.generator.util.Pair;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public abstract class EnumGeneratorTask extends DefaultTask {

    private static final String ENUMS_URL = "https://raw.githubusercontent.com/Mojang/bedrock-protocol-docs/%s/html/enums.html";
    private static final String ENUMS_PACKAGE = "net.raphimc.viabedrock.protocol.data.enums.bedrock";
    private static final String FILE_HEADER = "// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!\n";

    @Input
    public abstract Property<String> getCommitHash();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void run() throws Throwable {
        System.out.println("Generating bedrock enums...");
        final File outputDir = this.getOutputDirectory().get().getAsFile();
        FileUtils.deleteDirectory(outputDir);
        outputDir.mkdirs();

        final Map<String, List<Pair<String, String>>> enums = new HashMap<>();
        final Document doc = Jsoup.parse(new URL(String.format(ENUMS_URL, this.getCommitHash().get())), 10_000);
        for (Element element : doc.selectXpath("/html/body/table/tbody/tr")) {
            final Elements tds = element.getElementsByTag("td");
            if (tds.isEmpty()) continue;
            final String enumName = tds.get(0).text();
            final String valuesString = tds.get(1).wholeText();
            final List<Pair<String, String>> values = new ArrayList<>();
            final Set<String> usedNames = new HashSet<>();
            for (String value : valuesString.split("\n")) {
                String fieldName = value.split(" = ")[0];
                if (fieldName.equalsIgnoreCase("Count")) continue;
                if (fieldName.equalsIgnoreCase("_count")) continue;
                if (fieldName.equalsIgnoreCase("total")) continue;
                if (fieldName.equalsIgnoreCase("All")) continue;
                if (fieldName.equalsIgnoreCase("NumEnchantments")) continue;
                if (fieldName.equalsIgnoreCase("NumTagTypes")) continue;
                if (fieldName.equalsIgnoreCase("AbilityCount")) continue;
                if (fieldName.equalsIgnoreCase("NumModes")) continue;
                if (fieldName.equalsIgnoreCase("9800")) continue;

                String fieldValue = value.split(" = ")[1];
                switch (fieldValue) {
                    case "std::numeric_limits::max()":
                    case "std::numeric_limits<uint32_t>::max()":
                        fieldValue = "Integer.MAX_VALUE";
                        break;
                    case "std::numeric_limits::min()":
                    case "std::numeric_limits<uint32_t>::min()":
                        fieldValue = "Integer.MIN_VALUE";
                        break;
                    case "NonTerminalBit":
                        fieldValue = "0";
                        break;
                }

                if (enumName.equals("PacketCompressionAlgorithm") && fieldName.equals("None")) {
                    fieldValue = "0xFF";
                }

                final String[] splitChars = {"|", "-"};
                for (String splitChar : splitChars) {
                    if (fieldValue.contains(splitChar) && !fieldValue.startsWith(splitChar)) {
                        final String[] split = fieldValue.split(Pattern.quote(splitChar));
                        for (int i = 0; i < split.length; i++) {
                            String part = split[i].trim();
                            for (String usedName : usedNames) {
                                if (part.equals(usedName)) {
                                    part = usedName + ".getValue()";
                                    break;
                                }
                            }
                            split[i] = part;
                        }
                        fieldValue = String.join(" " + splitChar + " ", split);
                    }
                }
                for (String usedName : usedNames) {
                    if (fieldValue.equals(usedName)) {
                        fieldValue = usedName + ".getValue()";
                        break;
                    }
                }

                usedNames.add(fieldName);
                values.add(new Pair<>(fieldName, fieldValue));
            }
            enums.put(enumName, values);
        }

        for (Map.Entry<String, List<Pair<String, String>>> entry : enums.entrySet()) {
            final String enumPackage;
            final String enumName;
            if (Character.isLowerCase(entry.getKey().charAt(0)) && entry.getKey().contains("::")) {
                enumPackage = ENUMS_PACKAGE + "." + entry.getKey().substring(0, entry.getKey().indexOf("::"));
                enumName = entry.getKey().substring(entry.getKey().indexOf("::") + 2);
            } else {
                enumPackage = ENUMS_PACKAGE;
                enumName = entry.getKey().replace("::", "_");
            }
            final List<Pair<String, String>> values = entry.getValue();
            final File enumDir = new File(outputDir, enumPackage.replace(".", "/"));
            enumDir.mkdirs();
            final File enumFile = new File(enumDir, enumName + ".java");
            try (final PrintWriter writer = new PrintWriter(enumFile)) {
                writer.println(FILE_HEADER);
                writer.println("package " + enumPackage + ";");
                writer.println();
                writer.println("import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;");
                writer.println("import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;");
                writer.println();
                writer.println("public enum " + enumName + " {");
                writer.println();
                for (int i = 0; i < values.size(); i++) {
                    final Pair<String, String> value = values.get(i);
                    writer.print("    " + value.getKey());
                    writer.print("(" + value.getValue() + ")");
                    if (i != values.size() - 1) {
                        writer.println(",");
                    } else {
                        writer.println(";");
                    }
                }
                writer.println();
                writer.println("    private static final Int2ObjectMap<" + enumName + "> BY_VALUE = new Int2ObjectOpenHashMap<>();");
                writer.println();
                writer.println("    static {");
                writer.println("        for (" + enumName + " value : values()) {");
                writer.println("            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);");
                writer.println("        }");
                writer.println("    }");
                writer.println();
                writer.println("    public static " + enumName + " getByValue(final int value) {");
                writer.println("        return BY_VALUE.get(value);");
                writer.println("    }");
                writer.println();
                writer.println("    public static " + enumName + " getByValue(final int value, final " + enumName + " fallback) {");
                writer.println("        return BY_VALUE.getOrDefault(value, fallback);");
                writer.println("    }");
                writer.println();
                writer.println("    private final int value;");
                writer.println();
                writer.println("    " + enumName + "(final int value) {");
                writer.println("        this.value = value;");
                writer.println("    }");
                writer.println();
                writer.println("    public int getValue() {");
                writer.println("        return this.value;");
                writer.println("    }");
                writer.println();
                writer.println("}");
            }
        }
    }

}
