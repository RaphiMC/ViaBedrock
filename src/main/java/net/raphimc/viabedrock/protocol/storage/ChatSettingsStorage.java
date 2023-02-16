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

public class ChatSettingsStorage extends StoredObject {

    private final boolean chatRestricted;
    private final boolean commandsEnabled;

    public ChatSettingsStorage(final UserConnection user, final boolean chatRestricted, final boolean commandsEnabled) {
        super(user);

        this.chatRestricted = chatRestricted;
        this.commandsEnabled = commandsEnabled;
    }

    public boolean isChatRestricted() {
        return this.chatRestricted;
    }

    public boolean areCommandsEnabled() {
        return this.commandsEnabled;
    }

}
