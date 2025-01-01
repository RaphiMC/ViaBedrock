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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListStorage implements StorableObject {

    private final Map<UUID, Pair<Long, String>> playerList = new HashMap<>();

    public Pair<Long, String> addPlayer(final UUID uuid, final long uniqueEntityId, final String name) {
        return this.playerList.put(uuid, new Pair<>(uniqueEntityId, name));
    }

    public Pair<Long, String> removePlayer(final UUID uuid) {
        return this.playerList.remove(uuid);
    }

    public boolean containsPlayer(final UUID uuid) {
        return this.playerList.containsKey(uuid);
    }

    public Pair<Long, String> getPlayer(final UUID uuid) {
        return this.playerList.get(uuid);
    }

    public Pair<UUID, String> getPlayer(final long uniqueEntityId) {
        for (final Map.Entry<UUID, Pair<Long, String>> entry : this.playerList.entrySet()) {
            if (entry.getValue().key() == uniqueEntityId) {
                return new Pair<>(entry.getKey(), entry.getValue().value());
            }
        }

        return null;
    }

}
