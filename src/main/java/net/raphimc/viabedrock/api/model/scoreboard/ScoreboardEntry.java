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
package net.raphimc.viabedrock.api.model.scoreboard;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.util.Pair;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.storage.PlayerListStorage;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.Objects;
import java.util.UUID;

public class ScoreboardEntry {

    private boolean isPlayerId;
    private Long entityId;
    private String fakePlayerName;

    private int score;
    private String javaName;

    public ScoreboardEntry(final int score, final boolean isPlayerId, final Long entityId, final String fakePlayerName) {
        this.updateTarget(isPlayerId, entityId, fakePlayerName);
        this.score = score;
    }

    public boolean isSameTarget(final ScoreboardEntry entry) {
        return this.isPlayerId == entry.isPlayerId && Objects.equals(this.entityId, entry.entityId) && Objects.equals(this.fakePlayerName, entry.fakePlayerName);
    }

    public void updateTarget(final boolean isPlayerId, final Long entityId, final String fakePlayerName) {
        this.isPlayerId = isPlayerId;
        this.entityId = entityId;
        this.fakePlayerName = fakePlayerName;
    }

    public boolean isValid() {
        return this.entityId != null || this.fakePlayerName != null;
    }

    public boolean isPlayerId() {
        return this.isPlayerId;
    }

    public Long entityId() {
        return this.entityId;
    }

    public String fakePlayerName() {
        return this.fakePlayerName;
    }

    public int score() {
        return this.score;
    }

    public void setScore(final int score) {
        this.score = score;
    }

    public String javaName() {
        return this.javaName;
    }

    public void updateJavaName(final UserConnection user) {
        if (this.entityId != null && this.isPlayerId) {
            final PlayerListStorage playerList = user.get(PlayerListStorage.class);
            final Pair<UUID, String> player = playerList.getPlayer(this.entityId);
            if (player != null) {
                this.javaName = player.value();
            } else {
                this.javaName = StringUtil.encodeLong(this.entityId) + user.get(ResourcePacksStorage.class).getTranslations().get("commands.scoreboard.players.offlinePlayerName");
            }
        } else if (this.entityId != null) {
            this.javaName = String.valueOf(this.entityId);
        } else if (this.fakePlayerName != null) {
            this.javaName = this.fakePlayerName;
        }
    }

}
