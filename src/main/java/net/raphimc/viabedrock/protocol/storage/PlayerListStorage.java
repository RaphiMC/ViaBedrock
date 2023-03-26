/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListStorage extends StoredObject {

    private final Map<UUID, String> playerList = new HashMap<>();

    public PlayerListStorage(final UserConnection user) {
        super(user);
    }

    public void addPlayer(final UUID uuid, final String name) {
        this.playerList.put(uuid, name);
    }

    public void removePlayer(final UUID uuid) {
        this.playerList.remove(uuid);
    }

    public boolean containsPlayer(final UUID uuid) {
        return this.playerList.containsKey(uuid);
    }

    public String getPlayerName(final UUID uuid) {
        return this.playerList.get(uuid);
    }

}
