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
package net.raphimc.viabedrock.platform;

import com.google.common.collect.Range;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.ViaBedrockConfig;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.api.protocol.EmptyBaseProtocol;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.io.File;
import java.util.logging.Logger;

public interface ViaBedrockPlatform {

    default void init(final File dataFolder) {
        final ViaBedrockConfig config = new ViaBedrockConfig(new File(dataFolder, "viabedrock.yml"));
        config.reloadConfig();
        ViaBedrock.init(this, config);
        Via.getManager().getSubPlatforms().add(ViaBedrock.IMPL_VERSION);

        final ProtocolManager protocolManager = Via.getManager().getProtocolManager();

        protocolManager.registerProtocol(new BedrockProtocol(), ProtocolVersion.v1_20, BedrockProtocolVersion.bedrockLatest);
        protocolManager.registerBaseProtocol(new EmptyBaseProtocol(), Range.singleton(BedrockProtocolVersion.bedrockLatest.getVersion()));

        this.getServerPacksFolder().mkdirs();
    }

    Logger getLogger();

    File getDataFolder();

    default File getDataCacheFolder() {
        return new File(this.getDataFolder(), "viabedrock");
    }

    default File getServerPacksFolder() {
        return new File(this.getDataCacheFolder(), "server_packs");
    }

}
