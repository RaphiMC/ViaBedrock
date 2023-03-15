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
package net.raphimc.viabedrock.protocol.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;
import net.raphimc.viabedrock.api.modinterface.BedrockSkinUtilityInterface;
import net.raphimc.viabedrock.protocol.model.SkinData;
import net.raphimc.viabedrock.protocol.storage.ChannelStorage;

import java.util.UUID;

public class SkinProvider implements Provider {

    public void setSkin(final UserConnection user, final UUID playerUuid, final SkinData skin) throws Exception {
        final ChannelStorage channelStorage = user.get(ChannelStorage.class);
        if (channelStorage.hasChannel(BedrockSkinUtilityInterface.CHANNEL)) {
            BedrockSkinUtilityInterface.sendSkin(user, playerUuid, skin);
        }
    }

}
