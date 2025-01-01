/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
import net.raphimc.viabedrock.protocol.data.enums.bedrock.IdentityDefinition_Type;
import net.raphimc.viabedrock.protocol.storage.PlayerListStorage;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.Objects;
import java.util.UUID;

public class ScoreboardEntry {

    private IdentityDefinition_Type type;
    private Long uniqueEntityId;
    private String fakePlayerName;

    private int score;
    private String javaName;

    public ScoreboardEntry(final int score, final IdentityDefinition_Type type, final Long uniqueEntityId, final String fakePlayerName) {
        this.updateTarget(type, uniqueEntityId, fakePlayerName);
        this.score = score;
    }

    public boolean isSameTarget(final ScoreboardEntry entry) {
        return this.type == entry.type && Objects.equals(this.uniqueEntityId, entry.uniqueEntityId) && Objects.equals(this.fakePlayerName, entry.fakePlayerName);
    }

    public void updateTarget(final IdentityDefinition_Type type, final Long uniqueEntityId, final String fakePlayerName) {
        this.type = type;
        this.uniqueEntityId = uniqueEntityId;
        this.fakePlayerName = fakePlayerName;
    }

    public boolean isValid() {
        return this.uniqueEntityId != null || this.fakePlayerName != null;
    }

    public IdentityDefinition_Type type() {
        return this.type;
    }

    public Long uniqueEntityId() {
        return this.uniqueEntityId;
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
        switch (this.type) {
            case Player -> {
                final PlayerListStorage playerList = user.get(PlayerListStorage.class);
                final Pair<UUID, String> player = playerList.getPlayer(this.uniqueEntityId);
                if (player != null) {
                    this.javaName = player.value();
                } else {
                    this.javaName = StringUtil.encodeLong(this.uniqueEntityId) + user.get(ResourcePacksStorage.class).getTexts().get("commands.scoreboard.players.offlinePlayerName");
                }
            }
            case Entity -> this.javaName = String.valueOf(this.uniqueEntityId);
            case FakePlayer -> this.javaName = this.fakePlayerName;
        }
    }

}
