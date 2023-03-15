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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChannelStorage extends StoredObject {

    private final Set<String> channels = new HashSet<>();

    public ChannelStorage(final UserConnection user) {
        super(user);
    }

    public void addChannels(final String[] channels) {
        this.channels.addAll(Arrays.asList(channels));
    }

    public boolean hasChannel(final String channel) {
        return this.channels.contains(channel);
    }

}
