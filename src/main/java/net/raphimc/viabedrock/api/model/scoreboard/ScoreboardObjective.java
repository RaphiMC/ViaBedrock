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
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.IdentityDefinition_Type;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ObjectiveSortOrder;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardObjective {

    private final String name;
    private final Map<Long, ScoreboardEntry> entries;
    private final ObjectiveSortOrder sortOrder;

    public ScoreboardObjective(final String name, final ObjectiveSortOrder sortOrder) {
        this.name = name;
        this.entries = new HashMap<>();
        this.sortOrder = sortOrder;
    }

    public ScoreboardEntry getEntry(final long scoreboardId) {
        return this.entries.get(scoreboardId);
    }

    public ScoreboardEntry getEntryWithSameTarget(final ScoreboardEntry entry) {
        for (ScoreboardEntry value : this.entries.values()) {
            if (value.isSameTarget(entry)) {
                return value;
            }
        }

        return null;
    }

    public ScoreboardEntry getEntryForPlayer(final long playerListId) {
        for (ScoreboardEntry value : this.entries.values()) {
            if (value.entityId() != null && value.type() == IdentityDefinition_Type.Player && playerListId == value.entityId()) {
                return value;
            }
        }

        return null;
    }

    public void addEntry(final UserConnection user, final long scoreboardId, final ScoreboardEntry entry) {
        this.entries.put(scoreboardId, entry);

        entry.updateJavaName(user);
        this.updateEntry0(user, entry);
    }

    public void updateEntry(final UserConnection user, final ScoreboardEntry entry) {
        this.removeEntry0(user, entry);
        entry.updateJavaName(user);
        this.updateEntry0(user, entry);
    }

    public void removeEntry(final UserConnection user, final long scoreboardId) {
        final ScoreboardEntry entry = this.entries.remove(scoreboardId);

        this.removeEntry0(user, entry);
    }

    public void updateEntry0(final UserConnection user, final ScoreboardEntry entry) {
        final PacketWrapper updateScore = PacketWrapper.create(ClientboundPackets1_21.SET_SCORE, user);
        updateScore.write(Types.STRING, entry.javaName()); // player name
        updateScore.write(Types.STRING, this.name); // objective name
        updateScore.write(Types.VAR_INT, this.sortOrder == ObjectiveSortOrder.Ascending ? -entry.score() : entry.score()); // score
        updateScore.write(Types.OPTIONAL_TAG, null); // display name
        updateScore.write(Types.BOOLEAN, false); // has number format
        updateScore.send(BedrockProtocol.class);
    }

    public void removeEntry0(final UserConnection user, final ScoreboardEntry entry) {
        final PacketWrapper updateScore = PacketWrapper.create(ClientboundPackets1_21.RESET_SCORE, user);
        updateScore.write(Types.STRING, entry.javaName()); // player name
        updateScore.write(Types.OPTIONAL_STRING, this.name); // objective name
        updateScore.send(BedrockProtocol.class);
    }

}
