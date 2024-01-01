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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.util.Pair;
import net.raphimc.viabedrock.api.model.scoreboard.ScoreboardEntry;
import net.raphimc.viabedrock.api.model.scoreboard.ScoreboardObjective;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardTracker implements StorableObject {

    private final Map<String, ScoreboardObjective> objectives = new HashMap<>();

    public boolean hasObjective(final String name) {
        return this.objectives.containsKey(name);
    }

    public ScoreboardObjective getObjective(final String name) {
        return this.objectives.get(name);
    }

    public void addObjective(final String name, final ScoreboardObjective objective) {
        this.objectives.put(name, objective);
    }

    public void removeObjective(final String name) {
        this.objectives.remove(name);
    }

    public Pair<ScoreboardObjective, ScoreboardEntry> getEntry(final long scoreboardId) {
        for (final ScoreboardObjective objective : this.objectives.values()) {
            final ScoreboardEntry entry = objective.getEntry(scoreboardId);
            if (entry != null) {
                return new Pair<>(objective, entry);
            }
        }

        return null;
    }

    public Pair<ScoreboardObjective, ScoreboardEntry> getEntryForPlayer(final long playerListId) {
        for (final ScoreboardObjective objective : this.objectives.values()) {
            final ScoreboardEntry entry = objective.getEntryForPlayer(playerListId);
            if (entry != null) {
                return new Pair<>(objective, entry);
            }
        }

        return null;
    }

}
