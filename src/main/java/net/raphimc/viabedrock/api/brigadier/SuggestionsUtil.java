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
package net.raphimc.viabedrock.api.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.viaversion.viaversion.util.Pair;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class SuggestionsUtil {

    public static CompletableFuture<Suggestions> suggestMatching(final Iterable<String> candidates, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        for (String candidate : candidates) {
            if (shouldSuggest(remaining, candidate.toLowerCase(Locale.ROOT))) {
                builder.suggest(candidate);
            }
        }

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestMatching(final Stream<Pair<String, String>> candidates, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        candidates.forEach(candidate -> {
            if (shouldSuggest(remaining, candidate.key().toLowerCase(Locale.ROOT))) {
                builder.suggest(candidate.key(), candidate.value() != null ? new LiteralMessage(candidate.value()) : null);
            }
        });

        return builder.buildFuture();
    }

    private static boolean shouldSuggest(final String remaining, final String candidate) {
        for (int i = 0; !candidate.startsWith(remaining, i); i++) {
            i = candidate.indexOf('_', i);
            if (i < 0) {
                return false;
            }
        }

        return true;
    }

}
