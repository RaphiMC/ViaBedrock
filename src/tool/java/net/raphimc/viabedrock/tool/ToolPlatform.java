/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.tool;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import com.viaversion.viaversion.platform.NoopInjector;
import com.viaversion.viaversion.platform.UserConnectionViaVersionPlatform;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.ViaBedrockConfig;
import net.raphimc.viabedrock.platform.ViaBedrockPlatform;

import java.io.File;
import java.util.logging.Logger;

/**
 * Brings ViaVersion and ViaBedrock up far enough for a tool to load the mapping data.
 * <p>
 * Loading the mappings logs warnings, and the logger comes from the platform, so without this a data problem turns
 * into a {@link NullPointerException} instead of the message it was supposed to print. Resource pack translation is
 * switched off so that no tool ends up opening an HTTP port.
 */
public class ToolPlatform implements ViaBedrockPlatform {

    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        ViaManagerImpl.initAndLoad(new ToolViaPlatform(), new NoopInjector(), new ViaCommandHandler(false), ViaPlatformLoader.NOOP);
        while (!Via.getManager().getProtocolManager().hasLoadedMappings()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for the mappings to load", e);
            }
        }

        final ToolPlatform platform = new ToolPlatform();
        ViaBedrock.init(platform, new ToolConfig(new File(platform.getDataFolder(), "viabedrock.yml"), platform.getLogger()));
    }

    public static void shutdown() {
        final ViaManagerImpl viaManager = (ViaManagerImpl) Via.getManager();
        viaManager.destroy();
    }

    @Override
    public Logger getLogger() {
        return Logger.getGlobal();
    }

    @Override
    public File getDataFolder() {
        final File dataFolder = ToolPaths.PROJECT_ROOT.resolve("run/tool").toFile();
        dataFolder.mkdirs();
        return dataFolder;
    }

    private static class ToolConfig extends ViaBedrockConfig {

        public ToolConfig(final File configFile, final Logger logger) {
            super(configFile, logger);
            this.reload();
        }

        @Override
        public boolean shouldTranslateResourcePacks() {
            return false;
        }

    }

    private static class ToolViaPlatform extends UserConnectionViaVersionPlatform {

        public ToolViaPlatform() {
            super(null);
        }

        @Override
        public String getPlatformName() {
            return "ViaBedrock tools";
        }

        @Override
        public String getPlatformVersion() {
            return "tool";
        }

        @Override
        public Logger createLogger(final String name) {
            return Logger.getGlobal();
        }

        @Override
        protected AbstractViaConfig createConfig() {
            return new AbstractViaConfig(null, null) {
                @Override
                public void reload() {
                }
            };
        }

    }

}
