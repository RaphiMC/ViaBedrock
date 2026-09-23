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

import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.tool.ToolPlatform;

import java.util.List;

/**
 * Loads the mapping data the same way a real connection does, which is the final word on whether the data assets fit
 * together. {@link MappingGapReporter} finds problems without stopping, this proves that nothing is left.
 */
public class MappingValidator {

    public static void main(String[] args) throws Throwable {
        final List<MappingGap> gaps = new MappingAnalysis(new MappingAssets()).run(MappingAnalysis.CATEGORIES);
        if (!gaps.isEmpty()) {
            gaps.stream().limit(20).forEach(gap -> System.err.println(gap.category() + " " + gap.kind() + " " + gap.key()
                    + (gap.detail() == null ? "" : ": " + gap.detail())));
            throw new IllegalStateException("Found " + gaps.size() + " mapping gaps. Run ./gradlew reportMappingGaps for the full list.");
        }

        ToolPlatform.init();
        try {
            final long start = System.currentTimeMillis();
            BedrockProtocol.MAPPINGS.load();
            System.out.println();
            System.out.println("Mapping data loaded in " + (System.currentTimeMillis() - start) + "ms");
        } finally {
            ToolPlatform.shutdown();
        }
    }

}
