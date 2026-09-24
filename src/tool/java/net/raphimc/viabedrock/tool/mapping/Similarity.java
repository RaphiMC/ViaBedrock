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

/**
 * String similarity helpers used to pick the best template for a mapping proposal.
 */
public final class Similarity {

    /**
     * Levenshtein distance with a cutoff. Stops as soon as the distance is known to exceed {@code max}, which keeps
     * scanning one identifier against a few thousand candidates cheap.
     */
    public static int levenshtein(final String a, final String b, final int max) {
        if (a.equals(b)) {
            return 0;
        }
        if (Math.abs(a.length() - b.length()) > max) {
            return max + 1;
        }

        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            int rowMinimum = current[0];
            for (int j = 1; j <= b.length(); j++) {
                final int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
                rowMinimum = Math.min(rowMinimum, current[j]);
            }
            if (rowMinimum > max) {
                return max + 1;
            }
            final int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }

    /**
     * The number of characters two identifiers share at the end, counted on word boundaries.
     * <p>
     * This is what makes {@code lime_wool_stairs} pick {@code oak_stairs} as its template instead of some block which
     * merely has a similar name.
     */
    public static int commonSuffixLength(final String a, final String b) {
        int length = 0;
        while (length < a.length() && length < b.length() && a.charAt(a.length() - 1 - length) == b.charAt(b.length() - 1 - length)) {
            length++;
        }
        // Only count whole words, so "oak_stairs" and "oak_trapdoor" don't look related through a shared "a"
        while (length > 0 && length < a.length() && length < b.length() && a.charAt(a.length() - length) != '_') {
            length--;
        }
        return length;
    }

    /**
     * Rewrites a java identifier from a template block to the target block.
     * <p>
     * The template {@code oak_hanging_sign} maps to {@code oak_wall_hanging_sign}, so {@code poplar_hanging_sign}
     * should map to {@code poplar_wall_hanging_sign}. The part which differs between the two bedrock identifiers is
     * applied to the java identifier the same way.
     */
    public static String rewriteIdentifier(final String templateBedrock, final String targetBedrock, final String templateJava) {
        final int suffix = commonSuffixLength(templateBedrock, targetBedrock);
        if (suffix == 0) {
            return null;
        }
        final String templatePrefix = templateBedrock.substring(0, templateBedrock.length() - suffix);
        final String targetPrefix = targetBedrock.substring(0, targetBedrock.length() - suffix);
        if (templatePrefix.isEmpty()) {
            return null;
        }
        if (!templateJava.startsWith(templatePrefix)) {
            return null;
        }
        return targetPrefix + templateJava.substring(templatePrefix.length());
    }

    private Similarity() {
    }

}
