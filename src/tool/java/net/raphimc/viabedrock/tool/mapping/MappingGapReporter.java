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

import java.util.List;
import java.util.Map;

/**
 * Prints every bedrock identifier which has no java mapping, every mapping bedrock no longer has, and every mapping
 * which points at something java no longer has.
 * <p>
 * Run it with {@code ./gradlew reportMappingGaps}. Use {@code --category=items,entities} to narrow it down and
 * {@code --limit=0} to print every entry instead of the first few per kind.
 */
public final class MappingGapReporter {

    public static void main(final String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final List<String> categories = categories(toolArgs);
        final int limit = Integer.parseInt(toolArgs.get("limit", "20"));

        final MappingAnalysis analysis = new MappingAnalysis(new MappingAssets());
        final List<MappingGap> gaps = analysis.run(categories);

        System.out.println();
        if (gaps.isEmpty()) {
            System.out.println("No gaps found in " + String.join(", ", categories));
            return;
        }

        for (Map.Entry<String, Map<MappingGap.Kind, Integer>> entry : MappingAnalysis.summarize(gaps).entrySet()) {
            final StringBuilder line = new StringBuilder(entry.getKey()).append(": ");
            boolean first = true;
            for (Map.Entry<MappingGap.Kind, Integer> kindEntry : entry.getValue().entrySet()) {
                if (!first) {
                    line.append(", ");
                }
                line.append(kindEntry.getValue()).append(" ").append(kindEntry.getKey().name().toLowerCase());
                first = false;
            }
            System.out.println(line);
        }

        for (String category : categories) {
            for (MappingGap.Kind kind : MappingGap.Kind.values()) {
                final List<MappingGap> matching = gaps.stream().filter(gap -> gap.category().equals(category) && gap.kind() == kind).toList();
                if (matching.isEmpty()) {
                    continue;
                }
                System.out.println();
                System.out.println("== " + kind + " " + category + " (" + matching.size() + ")");
                final List<MappingGap> shown = limit > 0 && matching.size() > limit ? matching.subList(0, limit) : matching;
                for (MappingGap gap : shown) {
                    System.out.println("  " + gap.key() + (gap.detail() == null ? "" : "  " + gap.detail()));
                }
                if (shown.size() < matching.size()) {
                    System.out.println("  ... " + (matching.size() - shown.size()) + " more, pass --limit=0 to see all");
                }
            }
        }
    }

    static List<String> categories(final ToolArgs toolArgs) {
        final List<String> categories = toolArgs.list("category");
        if (categories.isEmpty()) {
            return MappingAnalysis.CATEGORIES;
        }
        for (String category : categories) {
            if (!MappingAnalysis.CATEGORIES.contains(category)) {
                throw new IllegalArgumentException("Unknown category '" + category + "'. Known categories: " + String.join(", ", MappingAnalysis.CATEGORIES));
            }
        }
        return categories;
    }

    private MappingGapReporter() {
    }

}
