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

import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.tool.ToolArgs;
import net.raphimc.viabedrock.tool.ToolPaths;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Measures how often the proposer reproduces a mapping that was written by hand.
 * <p>
 * For every block which is already mapped, its mappings are hidden, the block is proposed from scratch and the result
 * is compared against what is in the file. The accuracy per strategy is the number to look at before trusting a
 * proposal for a block nobody has mapped yet.
 */
public class MappingSelfTest {

    public static void main(String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final int limit = Integer.parseInt(toolArgs.get("limit", "0"));
        final int mismatchesToShow = Integer.parseInt(toolArgs.get("show", "25"));

        final MappingAnalysis analysis = new MappingAnalysis(new MappingAssets());
        analysis.run(List.of(MappingAnalysis.BLOCK_STATES));

        final Map<String, List<BedrockBlockState>> mappedBlocks = new TreeMap<>();
        for (BedrockBlockState bedrockBlockState : analysis.bedrockBlockStates()) {
            if (analysis.blockStateMappings().containsKey(bedrockBlockState)) {
                mappedBlocks.computeIfAbsent(bedrockBlockState.namespacedIdentifier(), key -> new ArrayList<>()).add(bedrockBlockState);
            }
        }

        final Map<String, int[]> results = new TreeMap<>();
        final Map<String, List<String>> mismatches = new TreeMap<>();
        int tested = 0;
        for (Map.Entry<String, List<BedrockBlockState>> block : mappedBlocks.entrySet()) {
            if (limit > 0 && tested >= limit) {
                break;
            }
            tested++;

            final Set<String> excluded = new LinkedHashSet<>();
            excluded.add(block.getKey());
            final BlockStateProposer proposer = new BlockStateProposer(analysis, excluded);

            for (BedrockBlockState bedrockBlockState : block.getValue()) {
                final BlockStateProposal proposal = proposer.propose(bedrockBlockState);
                final BlockState expected = analysis.blockStateMappings().get(bedrockBlockState);
                final int[] counts = results.computeIfAbsent(proposal.strategy(), key -> new int[2]);
                counts[1]++;
                if (proposal.resolved() && proposal.javaBlockState().equals(expected)) {
                    counts[0]++;
                } else if (mismatches.computeIfAbsent(proposal.strategy(), key -> new ArrayList<>()).size() < mismatchesToShow) {
                    mismatches.get(proposal.strategy()).add(bedrockBlockState.toBlockStateString(true)
                            + "\n    expected " + expected.toBlockStateString(true)
                            + "\n    proposed " + (proposal.resolved() ? proposal.javaBlockState().toBlockStateString(true) : "nothing")
                            + "  [" + proposal.strategy() + (proposal.template() == null ? "" : " from " + proposal.template()) + "]");
                }
            }
        }

        final StringBuilder report = new StringBuilder();
        report.append("Self test over ").append(tested).append(" already mapped blocks\n\n");
        int correct = 0;
        int total = 0;
        for (Map.Entry<String, int[]> entry : results.entrySet()) {
            correct += entry.getValue()[0];
            total += entry.getValue()[1];
            report.append(String.format("  %-20s %6d / %6d  %6.2f%%%n", entry.getKey(), entry.getValue()[0], entry.getValue()[1],
                    100D * entry.getValue()[0] / entry.getValue()[1]));
        }
        report.append(String.format("%n  %-20s %6d / %6d  %6.2f%%%n", "total", correct, total, 100D * correct / total));

        for (Map.Entry<String, List<String>> entry : mismatches.entrySet()) {
            report.append("\nFirst ").append(entry.getValue().size()).append(" ").append(entry.getKey()).append(" mismatches:\n");
            for (String mismatch : entry.getValue()) {
                report.append("  ").append(mismatch).append("\n");
            }
        }

        System.out.println();
        System.out.println(report);
        ToolPaths.writeString(MappingProposer.OUTPUT_DIR.resolve("self_test.txt"), report.toString());
    }

    private static Map<String, Integer> empty() {
        return new LinkedHashMap<>();
    }

}
