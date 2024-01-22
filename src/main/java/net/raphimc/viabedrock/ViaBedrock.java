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
package net.raphimc.viabedrock;

import net.raphimc.viabedrock.api.http.ResourcePackHttpServer;
import net.raphimc.viabedrock.api.io.LevelDB;
import net.raphimc.viabedrock.platform.ViaBedrockPlatform;

import java.net.InetSocketAddress;
import java.util.logging.Level;

public class ViaBedrock {

    public static final String VERSION = "${version}";
    public static final String IMPL_VERSION = "${impl_version}";

    private static ViaBedrockPlatform platform;
    private static ViaBedrockConfig config;
    private static ResourcePackHttpServer resourcePackServer;
    private static LevelDB blobCache;

    private ViaBedrock() {
    }

    public static void init(final ViaBedrockPlatform platform, final ViaBedrockConfig config) {
        if (ViaBedrock.platform != null) throw new IllegalStateException("ViaBedrock is already initialized");

        ViaBedrock.platform = platform;
        ViaBedrock.config = config;

        if (config.shouldTranslateResourcePacks()) {
            try {
                ViaBedrock.resourcePackServer = new ResourcePackHttpServer(new InetSocketAddress(config.getResourcePackHost(), config.getResourcePackPort()));
                platform.getLogger().log(Level.INFO, "Started resource pack HTTP server on " + resourcePackServer.getUrl());
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to start resource pack HTTP server", e);
            }
        }
        try {
            ViaBedrock.blobCache = new LevelDB(platform.getBlobCacheFolder());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    ViaBedrock.blobCache.close();
                } catch (Throwable e) {
                    ViaBedrock.platform.getLogger().log(Level.WARNING, "Failed to close blob cache", e);
                }
            }));
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to open or create blob cache", e);
        }
    }

    public static ViaBedrockPlatform getPlatform() {
        return ViaBedrock.platform;
    }

    public static ViaBedrockConfig getConfig() {
        return ViaBedrock.config;
    }

    public static ResourcePackHttpServer getResourcePackServer() {
        return ViaBedrock.resourcePackServer;
    }

    public static LevelDB getBlobCache() {
        return ViaBedrock.blobCache;
    }

}
