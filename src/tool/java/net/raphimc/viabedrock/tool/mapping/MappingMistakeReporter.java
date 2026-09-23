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
package net.raphimc.viabedrock.tool.mapping;

import net.raphimc.viabedrock.tool.ToolArgs;
import net.raphimc.viabedrock.tool.ToolPaths;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Prints mappings which load fine but disagree with the rest of the data. Run it with
 * {@code ./gradlew reportMappingMistakes}.
 */
public class MappingMistakeReporter {

    public static void main(String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final int limit = Integer.parseInt(toolArgs.get("limit", "0"));

        final MappingAssets assets = new MappingAssets();
        final MappingAnalysis analysis = new MappingAnalysis(assets);
        analysis.run(MappingAnalysis.CATEGORIES);

        final List<MappingMistake> mistakes = new java.util.ArrayList<>(new MappingMistakeDetector(assets, analysis).run());
        final List<ItemMappingFix> itemFixes = new ItemMappingChecker(assets).run();
        for (ItemMappingFix fix : itemFixes) {
            mistakes.add(new MappingMistake(MappingAnalysis.ITEMS, MappingMistake.IDENTITY_AVAILABLE, fix.key(),
                    "maps to " + fix.currentJavaId() + ", suggest " + fix.suggestedJavaId() + " (" + fix.reason() + ")"));
        }
        mistakes.sort(java.util.Comparator.comparing(MappingMistake::category).thenComparing(MappingMistake::check).thenComparing(MappingMistake::key));

        final com.viaversion.viaversion.libs.gson.JsonObject itemFixJson = new com.viaversion.viaversion.libs.gson.JsonObject();
        final com.viaversion.viaversion.libs.gson.JsonObject expectedItemIds = new com.viaversion.viaversion.libs.gson.JsonObject();
        for (ItemMappingFix fix : itemFixes) {
            itemFixJson.addProperty(fix.key(), fix.suggestedJavaId());
            expectedItemIds.addProperty(fix.key(), fix.currentJavaId());
        }
        ToolPaths.writeJson(MappingProposer.OUTPUT_DIR.resolve("item_fixes.json"), itemFixJson);
        ToolPaths.writeJson(MappingProposer.OUTPUT_DIR.resolve("item_fixes_expected.json"), expectedItemIds);

        final StringBuilder report = new StringBuilder();
        report.append("Mappings which load fine but disagree with the rest of the data.\n");
        report.append("None of this is proof of a bug, it is the shortlist worth reading.\n\n");

        final Map<String, List<MappingMistake>> byCheck = new TreeMap<>();
        for (MappingMistake mistake : mistakes) {
            byCheck.computeIfAbsent(mistake.category() + " / " + mistake.check(), key -> new java.util.ArrayList<>()).add(mistake);
        }
        for (Map.Entry<String, List<MappingMistake>> entry : byCheck.entrySet()) {
            report.append("== ").append(entry.getKey()).append(" (").append(entry.getValue().size()).append(")\n");
            final List<MappingMistake> shown = limit > 0 && entry.getValue().size() > limit ? entry.getValue().subList(0, limit) : entry.getValue();
            for (MappingMistake mistake : shown) {
                report.append("   ").append(mistake.key()).append("\n     ").append(mistake.detail()).append("\n");
            }
            if (shown.size() < entry.getValue().size()) {
                report.append("   ... ").append(entry.getValue().size() - shown.size()).append(" more\n");
            }
            report.append("\n");
        }

        System.out.println();
        System.out.println(report);
        ToolPaths.writeString(MappingProposer.OUTPUT_DIR.resolve("mistakes.txt"), report.toString());
    }

}
