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

import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.tool.ToolArgs;
import net.raphimc.viabedrock.tool.ToolPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Writes a mapping proposal for every gap into {@code run/mapping-proposals}, so the proposals can be read before
 * anything touches the data assets.
 * <p>
 * Run it with {@code ./gradlew proposeMappings}, review {@code block_states.txt}, then apply the result with
 * {@code ./gradlew applyMappingProposals}.
 */
public class MappingProposer {

    public static final Path OUTPUT_DIR = ToolPaths.PROJECT_ROOT.resolve("run/mapping-proposals");

    public static void main(String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final double minScore = Double.parseDouble(toolArgs.get("min-score", "0.5"));

        final MappingAnalysis analysis = new MappingAnalysis(new MappingAssets());
        final List<MappingGap> gaps = analysis.run(List.of(MappingAnalysis.BLOCK_STATES));
        final List<BlockStateProposal> proposals = new BlockStateProposer(analysis).proposeAll();

        final JsonObject accepted = new JsonObject();
        final List<BlockStateProposal> rejected = new ArrayList<>();
        final Map<String, List<BlockStateProposal>> byBlock = new TreeMap<>();
        for (BlockStateProposal proposal : proposals) {
            byBlock.computeIfAbsent(proposal.bedrockBlockState().namespacedIdentifier(), key -> new ArrayList<>()).add(proposal);
            if (proposal.resolved() && proposal.score() >= minScore) {
                accepted.addProperty(proposal.bedrockBlockState().toBlockStateString(true), proposal.javaBlockState().toBlockStateString(true));
            } else {
                rejected.add(proposal);
            }
        }

        final List<String> stale = gaps.stream()
                .filter(gap -> gap.kind() == MappingGap.Kind.STALE)
                .map(MappingGap::key)
                .sorted()
                .toList();

        ToolPaths.writeJson(OUTPUT_DIR.resolve("block_states.json"), GsonUtil.sort(accepted));
        ToolPaths.writeString(OUTPUT_DIR.resolve("block_states_stale.json"), GsonUtil.getGson().toJson(stale));
        ToolPaths.writeString(OUTPUT_DIR.resolve("block_states.txt"), report(byBlock, stale, minScore));

        System.out.println();
        System.out.println("proposed  " + accepted.size() + " of " + proposals.size() + " missing block states");
        System.out.println("unresolved " + rejected.size());
        System.out.println("stale      " + stale.size() + " mappings bedrock no longer has");
        System.out.println();
        for (Map.Entry<String, Integer> entry : strategyCounts(proposals).entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }

    private static Map<String, Integer> strategyCounts(final List<BlockStateProposal> proposals) {
        final Map<String, Integer> counts = new TreeMap<>();
        for (BlockStateProposal proposal : proposals) {
            counts.merge(proposal.strategy(), 1, Integer::sum);
        }
        return counts;
    }

    private static String report(final Map<String, List<BlockStateProposal>> byBlock, final List<String> stale, final double minScore) {
        final StringBuilder report = new StringBuilder();
        report.append("Block state mapping proposals\n");
        report.append("Proposals scoring below ").append(minScore).append(" are listed but not written to block_states.json\n\n");

        for (Map.Entry<String, List<BlockStateProposal>> entry : byBlock.entrySet()) {
            final List<BlockStateProposal> proposals = entry.getValue();
            final Map<String, Integer> strategies = strategyCounts(proposals);
            final BlockStateProposal first = proposals.get(0);

            report.append("== ").append(entry.getKey()).append("  (").append(proposals.size()).append(" states, ").append(strategies).append(")\n");
            if (first.template() != null) {
                report.append("   template: ").append(first.template()).append("\n");
            }
            if (first.note() != null) {
                report.append("   note: ").append(first.note()).append("\n");
            }
            final int shown = Math.min(proposals.size(), 4);
            for (int i = 0; i < shown; i++) {
                final BlockStateProposal proposal = proposals.get(i);
                report.append("   ").append(proposal.bedrockBlockState().toBlockStateString(true))
                        .append("\n     -> ").append(proposal.resolved() ? proposal.javaBlockState().toBlockStateString(true) : "UNRESOLVED")
                        .append("  [").append(proposal.strategy()).append(" ").append(String.format("%.2f", proposal.score())).append("]\n");
            }
            if (proposals.size() > shown) {
                report.append("   ... ").append(proposals.size() - shown).append(" more states of this block\n");
            }
            report.append("\n");
        }

        if (!stale.isEmpty()) {
            report.append("== stale mappings, bedrock no longer has these states (").append(stale.size()).append(")\n");
            for (String key : stale) {
                report.append("   ").append(key).append("\n");
            }
        }
        return report.toString();
    }

    static Map<String, String> readProposals(final Path file) {
        final Map<String, String> proposals = new LinkedHashMap<>();
        final JsonObject json = GsonUtil.getGson().fromJson(readString(file), JsonObject.class);
        json.entrySet().forEach(entry -> proposals.put(entry.getKey(), entry.getValue().getAsString()));
        return proposals;
    }

    private static String readString(final Path file) {
        try {
            return java.nio.file.Files.readString(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + ToolPaths.describe(file) + ". Run ./gradlew proposeMappings first.", e);
        }
    }

}
